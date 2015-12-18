package guerrilla.pics;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class encapsulates an SQLite database and provides a convenient way to
 * interact one in asynchronous manner. Follows the actor model by using a
 * single worker thread and a queue where all API calls are inserted into.
 *
 * @author teemuk
 */
public class DatabaseController
    extends Service {

  /** Tag for log lines */
  private final String TAG = this.getClass().getSimpleName();


  //==========================================================================//
  // Database Definitions
  //==========================================================================//
  /** Database name */
  private static final String DB_NAME
      = "picsdb";
  /**
   * Database version. Increment this is the schema changes, and implement the
   * schema upgrade in DbHelper.onUpgrade().
   */
  private static final int DB_VERSION
      = 7;

  //--------------------------------------------------------------------------//
  // Names for tables and columns
  //--------------------------------------------------------------------------//
  private static final String PIC_TABLE_NAME
      = "pics";
  /** ID for the row, required by ListView etc. */
  private static final String PIC_COL_ID
      = "_id";
  /** Path to the picture on the filesystem */
  private static final String PIC_COL_PATH
      = "path";
  /** Title for the picture. */
  private static final String PIC_COL_TITLE
      = "title";
  /** Timestamp when the item was created */
  private static final String PIC_COL_TIMESTAMP
      = "timestamp";
  /**
   * Unique ID for the item (combination of timestamp + uniqueid is unique)
   */
  private static final String PIC_COL_UNIQUE_ID
      = "uniqueid";
  /** Whether the message has been seen by the SCAMPI router. */
  private static final String PIC_COL_ROUTED
      = "routed";
  /**
   * Whether the user has deleted the item. Deleted items are kept in the
   * database so that copies of them are not shown to the user later.
   */
  private static final String PIC_COL_DELETED
      = "deleted";

  //--------------------------------------------------------------------------//
  // Queries
  //--------------------------------------------------------------------------//
  /** SQLite query to create the database table: {@value} */
  private static final String SQL_CREATE_PIC_TABLE
      = "CREATE TABLE " + PIC_TABLE_NAME + " (" +
        PIC_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        PIC_COL_PATH + " TEXT, " +
        PIC_COL_TITLE + " TEXT, " +
        PIC_COL_UNIQUE_ID + " INTEGER, " +
        PIC_COL_TIMESTAMP + " INTEGER, " +
        PIC_COL_ROUTED + " INTEGER, " +
        PIC_COL_DELETED + " INTEGER, " +
        "CONSTRAINT unq UNIQUE (" + PIC_COL_TIMESTAMP + ", " +
        PIC_COL_UNIQUE_ID + ") " +
        "ON CONFLICT IGNORE" +
        ");";

  /** SQLite query to select all entries from the database */
  private static final String SQL_SELECT_ALL
      = "SELECT " +
        PIC_COL_ID + ", " +
        PIC_COL_PATH + ", " +
        PIC_COL_TITLE + ", " +
        PIC_COL_UNIQUE_ID + ", " +
        PIC_COL_TIMESTAMP + ", " +
        PIC_COL_ROUTED + ", " +
        PIC_COL_DELETED + " " +
        "FROM " +
        PIC_TABLE_NAME + " " +
        "WHERE " +
        PIC_COL_DELETED + "=0";

  /** SQLite query to select unrouted entries from the database */
  private static final String SQL_SELECT_UNROUTED
      = "SELECT " +
        PIC_COL_ID + ", " +
        PIC_COL_PATH + ", " +
        PIC_COL_TITLE + ", " +
        PIC_COL_UNIQUE_ID + ", " +
        PIC_COL_TIMESTAMP + ", " +
        PIC_COL_ROUTED + ", " +
        PIC_COL_DELETED + " " +
        "FROM " +
        PIC_TABLE_NAME + " " +
        "WHERE " +
        PIC_COL_ROUTED + " = 0";

  /** SQLite query for updating the routed status of a message:<br>{@value} */
  protected static final String SQL_UPDATE_ROUTED =
      "UPDATE " +
      PIC_TABLE_NAME + " " +
      "SET " +
      PIC_COL_ROUTED + " = 1 " +
      "WHERE " +
      PIC_COL_ID + " = ";

  /**
   * SQLite query for updating the deleted status of a message:<br>{@value}
   */
  protected static final String SQL_UPDATE_DELETED =
      "UPDATE " +
      PIC_TABLE_NAME + " " +
      "SET " +
      PIC_COL_DELETED + " = 1 " +
      "WHERE " +
      PIC_COL_ID + " = ";

  /** SQLite query for selecting a specific item. */
  private static final String SQL_SELECT_ITEM
      = "SELECT " +
        PIC_COL_ID + ", " +
        PIC_COL_PATH + ", " +
        PIC_COL_TITLE + ", " +
        PIC_COL_UNIQUE_ID + ", " +
        PIC_COL_TIMESTAMP + ", " +
        PIC_COL_ROUTED + ", " +
        PIC_COL_DELETED + " " +
        "FROM " +
        PIC_TABLE_NAME + " " +
        "WHERE " +
        PIC_COL_TIMESTAMP + " = [TIMESTAMP] " +
        "AND " +
        PIC_COL_UNIQUE_ID + " = [UNIQUE]";
  //==========================================================================//


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  /** Binder for activities */
  private final IBinder binder
      = new DatabaseController.DatabaseBinder();
  /** Task executor */
  private ExecutorService executor;
  /** The database encapsulated by this controller */
  private SQLiteDatabase database;

  /** Callbacks for database updates. */
  private final Collection<OnUpdated> onUpdatedCallbacks
      = new CopyOnWriteArraySet<OnUpdated>();
  /** Callbacks for database insertions. */
  private final Collection<OnInserted> onInsertedCallbacks
      = new CopyOnWriteArraySet<OnInserted>();
  //==========================================================================//


  //==========================================================================//
  // API
  //--------------------------------------------------------------------------//
  // Public API for the service. Since database operations can take
  // arbitrary time to execute, the API is asynchronous. All commands are
  // executed in FIFO order by the controller thread,
  // which also invokes any appropriate callbacks.
  //==========================================================================//

  /**
   * Add a callback that will be invoked whenever the database updates. In
   * addition the callback will be invoked immediately after being added with
   * the current state of the database.
   *
   * @param callback
   *     the callback to invoke
   */
  public void addUpdateCallback( OnUpdated callback ) {
    // Add the callback
    this.onUpdatedCallbacks.add( callback );

    // Ask the database for immediate update so the client gets the
    // current state
    this.executor.submit( new RunQueryForCallbackTask( callback ) );
  }

  /**
   * Add a callback that will be invoked whenever an item is inserted into the
   * database.
   *
   * @param callback
   *     the callback to invoke
   */
  public void addInsertedCallback( OnInserted callback ) {
    this.onInsertedCallbacks.add( callback );
  }

  /**
   * Removes a previously added update callback.
   *
   * @param callback
   *     the callback to remove
   *
   * @see #addUpdateCallback(OnUpdated)
   */
  public void removeUpdateCallback( OnUpdated callback ) {
    this.onUpdatedCallbacks.remove( callback );
  }

  /**
   * Removes a previously added insertion callback.
   *
   * @param callback
   *     the callback to remove
   */
  public void removeInsertedCallback( OnInserted callback ) {
    this.onInsertedCallbacks.remove( callback );
  }

  /**
   * Inserts an item into the database. If the insertion is successful, all
   * update callbacks will be invoked. The insertion operation is
   * asynchronous,
   * the callback can be used to get a notification once the operation
   * finishes.
   *
   * @param path
   *     path to the image on the filesystem
   * @param title
   *     title for the image
   * @param timestamp
   *     creation timestamp
   * @param unique
   *     unique identifier
   * @param routed
   *     whether the SCAMPI router has seen this entry
   * @param callback
   *     callback to invoke once this insertion operation has been completed
   */
  public void insertItem( String path, String title, long timestamp,
                          long unique, boolean routed, boolean deleted,
                          OnInserted callback ) {
    this.executor.submit( new DatabaseInsertTask(
        path, title, timestamp, unique, routed, deleted, callback ) );
  }

  /**
   * Inserts an item into the database. If the insertion is successful, all
   * update callbacks will be invoked. The insertion operation is
   * asynchronous,
   * the callback can be used to get a notification once the operation
   * finishes.
   *
   * @param item
   *     item to insert
   * @param callback
   *     callback to invoke once this operation has completed
   */
  public void insertItem( ItemDescriptor item, OnInserted callback ) {
    this.insertItem( item.path, item.title,
        item.timestamp, item.uniqueid,
        item.routed, item.deleted,
        callback );
  }

  /**
   * Asynchronously queries the database for all unrouted messages.
   *
   * @param callback
   *     callback to invoke with the results.
   */
  public void getUnrouted( QueryFinished callback ) {
    this.executor.submit( new QueryForCallbackTask(
        callback, SQL_SELECT_UNROUTED ) );
  }

  /**
   * Sets the routed status of the given message. Will not trigger onUpdated
   * callbacks.
   *
   * @param id
   *     id of the message whose status to set
   * @param callback
   *     callback to invoke once the query is finished
   */
  public void setRouted( long id, ExecuteFinished callback ) {
    String query = SQL_UPDATE_ROUTED + id + ";";
    this.executor.submit( new ExecuteForCallbackTask( query, callback ) );
  }

  /**
   * Sets the deleted status of the given message. Will trigger onUpdated
   * callbacks.
   *
   * @param id
   *     id of the message whose deleted state to set
   * @param callback
   *     callback to invoke once the query is finished
   */
  public void setDeleted( long id, QueryFinished callback ) {
    String query = SQL_UPDATE_DELETED + id;
    this.executor.submit( new QueryForCallbackTask( callback, query ) );

    // We need to invoke all onUpdated listeners since the change will
    // remove an item from the list
    this.updateListeners();
  }

  public void getItem( long timestamp, long unique, QueryFinished callback ) {
    String query = SQL_SELECT_ITEM;
    query = query.replace( "[TIMESTAMP]", "" + timestamp );
    query = query.replace( "[UNIQUE]", "" + unique );
    Log.d( TAG, "Item selection query: " + query );
    this.executor.submit( new QueryForCallbackTask( callback, query ) );
  }

  /**
   * Blocking variant of {@link #getItem(long, long, QueryFinished)}. Will
   * block
   * for at most 1000 ms.
   *
   * @param timestamp
   *     timestamp of the item
   * @param unique
   *     unique ID of the item
   *
   * @return list containing all matching items from the database
   */
  public List<ItemDescriptor> getItem( long timestamp, long unique ) {
    // Lock to synchronize on
    final Object lock = new Object();
    // The executor thread callback will add the results into this list
    final List<ItemDescriptor> result = new LinkedList<ItemDescriptor>();

    // Fire off the task and block on the lock. The task will wake us up
    // once it has finished and added its results into the above list.
    synchronized ( lock ) {
      this.getItem( timestamp, unique,
          new QueryFinished() {
            @Override
            public void queryFinished( List<ItemDescriptor> results ) {
              synchronized ( lock ) {
                result.addAll( results );
                lock.notifyAll();
              }
            }
          }
      );
      try {
        lock.wait( 1000 );
      } catch ( InterruptedException e ) {
        Log.d( TAG, "getItem() interrupted while blocking (" +
                    e.getMessage() + ")" );
      }
    }
    return result;
  }

  //--------------------------------------------------------------------------//
  // Callback definitions
  //--------------------------------------------------------------------------//

  /** Interface for receiving database updates. */
  public static interface OnUpdated {
    /**
     * The database has updated.
     *
     * @param items
     *     Full contents of the database after the update. This list is
     *     unmodifiable and will throw an exception on any attempt to modify
     *     it.
     */
    public void onUpdated( List<ItemDescriptor> items );
  }

  /** Interface for receiving a callback when item is inserted. */
  public static interface OnInserted {
    /**
     * A row insertion operation has finished. The operation might have
     * succeeded or failed, which can be determined from the ID.
     *
     * @param id
     *     id of the inserted row, or -1 if insertion failed
     * @param item
     *     descriptor of the item that was inserted
     */
    public void onInserted( long id, ItemDescriptor item );
  }

  /** Interface for callbacks when query has finished. */
  public static interface QueryFinished {
    /**
     * A query has finished.
     *
     * @param results
     *     Results of the query if the query had results. This list is
     *     unmodifiable and will throw an exception on any attempt to modify
     *     it.
     */
    public void queryFinished( List<ItemDescriptor> results );
  }

  /** Interface for callbacks when an execute operation finishes */
  public static interface ExecuteFinished {
    public void executeFinished();
  }

  //--------------------------------------------------------------------------//
  // Class definitions
  //--------------------------------------------------------------------------//

  /** A database item. */
  public static final class ItemDescriptor {
    public final long _id;
    public final String path, title;
    public final long timestamp, uniqueid;
    public final boolean routed, deleted;

    public ItemDescriptor( long _id, String path, String title,
                           long timestamp, long uniqueid,
                           boolean routed, boolean deleted ) {
      this._id = _id;
      this.path = path;
      this.title = title;
      this.timestamp = timestamp;
      this.uniqueid = uniqueid;
      this.routed = routed;
      this.deleted = deleted;
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Service Lifecycle
  //==========================================================================//
  @Override
  public IBinder onBind( Intent intent ) {
    Log.d( TAG, "onBind()" );

    return this.binder;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Create the executor
    this.executor = Executors.newSingleThreadExecutor();

    // Initialize the database
    this.executor.submit( new InitDatabaseTask() );

    Log.d( TAG, "onCreate()" );
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Shut down the executor
    this.executor.shutdownNow();

    // Close the database
    if ( this.database != null ) {
      this.database.close();
    }

    Log.d( TAG, "onDestroy()" );
  }

  @Override
  public void onRebind( Intent intent ) {
    super.onRebind( intent );

    Log.d( TAG, "onRebind()" );
  }

  @Override
  public int onStartCommand( Intent intent, int flags, int startId ) {
    super.onStartCommand( intent, flags, startId );

    Log.d( TAG, "onStartCommand()" );

    return START_STICKY;
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void updateListeners() {
    this.executor.submit( new QueryForCallbackTask(
        // Callback
        new QueryFinished() {
          @Override
          public void queryFinished( List<ItemDescriptor> results ) {
            // Wrap in unmodifiable list
            results = Collections.unmodifiableList( results );
            for ( OnUpdated cb : onUpdatedCallbacks ) {
              cb.onUpdated( results );
            }
          }
        },

        // Query
        SQL_SELECT_ALL
    ) );

    // We run a separate query for each callback since the cursor is not
    // thread safe.
    for ( OnUpdated cb : this.onUpdatedCallbacks ) {
      this.executor.submit( new RunQueryForCallbackTask( cb ) );
    }
  }

  private List<ItemDescriptor> cursorToList( Cursor cursor ) {
    // Precondition check
    if ( cursor == null ) {
      throw new IllegalArgumentException( "Cursor cannot be null" );
    }

    // Create the list
    List<ItemDescriptor> items =
        new ArrayList<ItemDescriptor>( cursor.getCount() );

    // Add the items from the cursor to the list
    cursor.moveToPosition( -1 );
    while ( cursor.moveToNext() ) {
      // Pull out the fields
      long id = cursor.getLong( 0 );
      String path = cursor.getString( 1 );
      String title = cursor.getString( 2 );
      long unique = cursor.getLong( 3 );
      long timestamp = cursor.getLong( 4 );
      boolean routed = ( cursor.getInt( 5 ) != 0 );
      boolean deleted = ( cursor.getInt( 6 ) != 0 );

      // Create a new item
      ItemDescriptor item = new ItemDescriptor( id, path, title,
          timestamp, unique,
          routed, deleted );
      items.add( item );
    }
    cursor.close();

    return items;
  }
  //==========================================================================//


  //==========================================================================//
  // Tasks
  //--------------------------------------------------------------------------//
  // Executor tasks for the controller
  //==========================================================================//

  /** Initializes the database using a {@code DbHelper}. */
  private class InitDatabaseTask
      implements Runnable {
    @Override
    public void run() {
      // Get an SQLite database instance using a helper,
      // which takes care of creating and upgrading the database.
      // XXX: Can the helper be abandoned without closing since
      // database.close() is called by the service?
      DbHelper helper = new DbHelper( DatabaseController.this );
      SQLiteDatabase database = helper.getWritableDatabase();

      // Store the reference
      if ( database != null ) {
        DatabaseController.this.database = database;
      } else {
        Log.e( TAG, "Failed to get database instance." );
      }
    }
  }

  /**
   * Runs a query and invokes the callback with the results. Used to guarantee
   * that new callbacks get invoked with the current database state
   * immediately.
   */
  private class RunQueryForCallbackTask
      implements Runnable {
    // TODO:
    // - Use QueryForCallbackTask instead

    private final OnUpdated callback;

    public RunQueryForCallbackTask( OnUpdated callback ) {
      this.callback = callback;
    }

    @Override
    public void run() {
      // Run the query
      SQLiteDatabase db = DatabaseController.this.database;
      if ( db == null ) {
        Log.e( TAG, "No database found. Cannot run query." );
        return;
      }
      Cursor cursor = db.rawQuery( SQL_SELECT_ALL, null );

      // Invoke the callback
      if ( this.callback != null ) {
        this.callback.onUpdated( cursorToList( cursor ) );
      } else {
        cursor.close();
      }
    }
  }

  /** Runs a query and invokes the callback with the results. */
  private class QueryForCallbackTask
      implements Runnable {
    private final QueryFinished callback;
    private final String query;

    public QueryForCallbackTask( QueryFinished callback, String query ) {
      if ( query == null || query.length() == 0 ) {
        throw new IllegalArgumentException( "Query cannot be empty." );
      }
      this.callback = callback;
      this.query = query;
    }

    @Override
    public void run() {
      // Run the query
      SQLiteDatabase db = DatabaseController.this.database;
      if ( db == null ) {
        Log.e( TAG, "No database found. Cannot run query." );
        return;
      }

      Log.d( TAG, "Executing query: " + this.query );
      Cursor cursor = db.rawQuery( this.query, null );

      // Invoke the callback
      if ( this.callback != null ) {
        this.callback.queryFinished( cursorToList( cursor ) );
      } else {
        cursor.close();
      }
    }
  }

  private class ExecuteForCallbackTask
      implements Runnable {
    private final ExecuteFinished callback;
    private final String query;

    public ExecuteForCallbackTask( String query, ExecuteFinished callback ) {
      this.callback = callback;
      this.query = query;
    }

    @Override
    public void run() {
      // Execute query
      database.execSQL( this.query );

      // Callback
      if ( this.callback != null ) {
        this.callback.executeFinished();
      }
    }
  }

  /** Attempts to insert an entry into the database. */
  private class DatabaseInsertTask
      implements Runnable {
    private final ItemDescriptor item;
    private final OnInserted onInserted;

    public DatabaseInsertTask( String path, String title,
                               long timestamp,
                               long unique, boolean routed,
                               boolean deleted,
                               OnInserted onInserted ) {
      this.item =
          new ItemDescriptor( 0, path, title, timestamp, unique,
              routed, deleted );
      this.onInserted = onInserted;

      Log.d( TAG, "Queue insert task: timestamp = " +
                  this.item.timestamp + ", uniqueID = " +
                  this.item.uniqueid );
    }

    @Override
    public void run() {
      // Content values to insert
      ContentValues values = new ContentValues( 6 );
      values.put( PIC_COL_PATH, this.item.path );
      values.put( PIC_COL_TITLE, this.item.title );
      values.put( PIC_COL_UNIQUE_ID, this.item.uniqueid );
      values.put( PIC_COL_TIMESTAMP, this.item.timestamp );
      values.put( PIC_COL_ROUTED, ( this.item.routed ? 1 : 0 ) );
      values.put( PIC_COL_DELETED, ( this.item.deleted ? 1 : 0 ) );

      // Try to insert
      long result;
      result = database.insert( PIC_TABLE_NAME, null, values );

      // Invoke callback
      if ( this.onInserted != null ) {
        this.onInserted.onInserted( result, this.item );
      }

      // Invoke other callbacks
      for ( OnInserted cb : onInsertedCallbacks ) {
        cb.onInserted( result, this.item );
      }

      // Update all listeners
      if ( result >= 0 ) {
        DatabaseController.this.updateListeners();
      }
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Database helper
  //--------------------------------------------------------------------------//
  // Code for managing the database versioning by using a database helper
  //==========================================================================//
  // TODO:
  // - Add migration between schemas if the schema changes after release
  private static class DbHelper
      extends SQLiteOpenHelper {

    public DbHelper( Context context ) {
      super( context, DB_NAME, null, DB_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
      // Generate the table
      db.execSQL( SQL_CREATE_PIC_TABLE );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion,
                           int newVersion ) {
      // Just drop the old tables for now
      db.execSQL( "DROP TABLE IF EXISTS " + PIC_TABLE_NAME );

      onCreate( db );
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Service Binder
  //==========================================================================//
  public class DatabaseBinder
      extends Binder {
    DatabaseController getService() {
      return DatabaseController.this;
    }
  }
  //==========================================================================//

}
