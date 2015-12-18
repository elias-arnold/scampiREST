package guerrilla.boards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for the message database. Allows messages to be inserted,
 * queried and updated in the database. Update listeners can be added to get
 * callbacks when the database is updated.
 *
 * @author teemuk
 */
public class MsgDatabaseHelper
    extends SQLiteOpenHelper {


  //==========================================================================//
  // Database definitions
  //==========================================================================//
  // Schema version
  private static final int DB_VERSION = 9;
  // Database name
  private static final String DB_NAME = "messagedb";

  // Name for the message table
  private static final String MSG_TABLE_NAME = "msgs";
  // Message tag ID
  public static final String MSG_COL_TAG = "tag";
  // Message content
  public static final String MSG_COL_CONTENT = "content";
  // Message timestamp
  public static final String MSG_COL_TIMESTAMP = "timestamp";
  // Unique ID
  public static final String MSG_COL_UNIQUE_ID = "uniqueid";
  // Router has this message
  public static final String MSG_COL_ROUTED = "routed";
  // Required by android for some reason
  private static final String MSG_COL_ID = "_id";

  // Name for generated count field
  public static final String MSG_COL_COUNT = "count";


  /** SQLite query for creating the message table:<br>{@value} */
  protected static final String CREATE_MSG_TABLE =
      "CREATE TABLE " + MSG_TABLE_NAME + " (" +
      MSG_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
      MSG_COL_TAG + " TEXT, " +
      MSG_COL_CONTENT + " TEXT, " +
      MSG_COL_TIMESTAMP + " INTEGER, " +
      MSG_COL_UNIQUE_ID + " INTEGER, " +
      MSG_COL_ROUTED + " INTEGER, " +
      "CONSTRAINT unq UNIQUE (" + MSG_COL_TIMESTAMP + ", " +
      MSG_COL_UNIQUE_ID + ", " + MSG_COL_TAG + ") " +
      "ON CONFLICT IGNORE" +
      ");";

  /** SQLite query for selecting all messages:<br>{@value} */
  protected static final String SELECT_MSGS =
      "SELECT " +
      MSG_COL_ID + ", " +
      MSG_COL_CONTENT + ", " +
      MSG_COL_TIMESTAMP + " " +
      "FROM " +
      MSG_TABLE_NAME + " " +
      "WHERE " +
      MSG_COL_TAG +
      " = ";

  /** SQLite query for selecting all tags:<br>{@value} */
  protected static final String SELECT_TAGS_COUNTS =
      "SELECT " +
      MSG_COL_ID + ", " +
      MSG_COL_TAG + ", " +
      "COUNT(" + MSG_COL_TAG + ") AS '" + MSG_COL_COUNT + "' " +
      "FROM " +
      MSG_TABLE_NAME + " " +
      "GROUP BY " +
      MSG_COL_TAG;

  /** SQLite query to select all unrouted messages:<br>{@value} */
  protected static final String SELECT_UNROUTED =
      "SELECT " +
      MSG_COL_ID + ", " +
      MSG_COL_TAG + ", " +
      MSG_COL_CONTENT + ", " +
      MSG_COL_TIMESTAMP + ", " +
      MSG_COL_UNIQUE_ID + " " +
      "FROM " +
      MSG_TABLE_NAME + " " +
      "WHERE " +
      MSG_COL_ROUTED + " = 0";

  /**
   * SQLite query for updating the routed status of a message:<br>{@value}
   */
  protected static final String UPDATE_ROUTED =
      "UPDATE " +
      MSG_TABLE_NAME + " " +
      "SET " +
      MSG_COL_ROUTED + " = 1 " +
      "WHERE " +
      MSG_COL_ID + " = ";
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//

  /**
   * Creates a new instance of the database helper.
   *
   * @param context
   *     context to use for the database
   */
  public MsgDatabaseHelper( Context context ) {
    super( context, DB_NAME, null, DB_VERSION );
  }

  /**
   * <p> Returns a cursor to a list of all tags sorted by the message count in
   * ascending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) tag id, 1) tag,
   * 2) message count for
   * the tag. </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_TAGS_COUNTS
   */
  public Cursor getCursorForAllTagsSortByCountAsc() {
    return this.getReadableDatabase().rawQuery( SELECT_TAGS_COUNTS +
                                                " ORDER BY " + MSG_COL_COUNT,
        null );
  }

  /**
   * <p> Returns a cursor to a list of all tags sorted by the message count in
   * descending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) tag id, 1) tag,
   * 2) message count for
   * the tag. </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_TAGS_COUNTS
   */
  public Cursor getCursorForAllTagsSortByCountDesc() {
    return this.getReadableDatabase().rawQuery( SELECT_TAGS_COUNTS +
                                                " ORDER BY " + MSG_COL_COUNT +
                                                " DESC",
        null );
  }

  /**
   * <p> Returns a cursor to a list of all tags sorted by the tag name in
   * ascending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) tag id, 1) tag,
   * 2) message count for
   * the tag. </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_TAGS_COUNTS
   */
  public Cursor getCursorForAllTagsSortedByNameAsc() {
    return this.getReadableDatabase().rawQuery( SELECT_TAGS_COUNTS +
                                                " ORDER BY " + MSG_COL_TAG,
        null );
  }

  /**
   * <p> Returns a cursor to a list of all tags sorted by the tag name in
   * descending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) tag id, 1) tag,
   * 2) message count for
   * the tag. </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_TAGS_COUNTS
   */
  public Cursor getCursorForAllTagsSortedByNameDesc() {
    return this.getReadableDatabase().rawQuery( SELECT_TAGS_COUNTS +
                                                " ORDER BY " + MSG_COL_TAG +
                                                " DESC",
        null );
  }

  /**
   * <p> Returns a cursor to a list of all messages for a given tag sorted by
   * the message creation time in ascending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) message id, 1) message, 2) timestamp.
   * </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_MSGS
   */
  public Cursor getMessagesForTagSortAsc( String tag ) {
    return this.getReadableDatabase().rawQuery( SELECT_MSGS + "'" + tag +
                                                "' ORDER BY " +
                                                MSG_COL_TIMESTAMP, null );
  }

  /**
   * <p> Returns a cursor to a list of all messages for a given tag sorted by
   * the message creation time in descending order. </p>
   * <p/>
   * <p> The columns in the cursor are: 0) message id, 1) message, 2) timestamp.
   * </p>
   *
   * @return <code>Cursor</code> to all tags.
   *
   * @see #SELECT_MSGS
   */
  public Cursor getMessagesForTagSortDesc( String tag ) {
    return this.getReadableDatabase().rawQuery( SELECT_MSGS + "'" + tag +
                                                "' ORDER BY " +
                                                MSG_COL_TIMESTAMP + " DESC",
        null );
  }

  /**
   * Returns a cursor to all messages marked as unrouted, i.e., messages that
   * have not been seen by the router.
   *
   * @return <code>Cursor</code> to the set of unrouted messages
   */
  public Cursor getUnroutedMessages() {
    return this.getReadableDatabase().rawQuery( SELECT_UNROUTED, null );
  }

  /**
   * Sets the message identified by <code>id</code> as routed, i.e., that the
   * router has seen the message (either published or received from the
   * router).
   *
   * @param id
   *     identifier of th message to mark as routed
   */
  public void setRouted( long id ) {
    SQLiteDatabase db = super.getWritableDatabase();
    db.execSQL( UPDATE_ROUTED + id );
    db.close();
  }

  /**
   * Inserts a message into the database.
   *
   * @param tag
   *     Tag for the message.
   * @param message
   *     Message content.
   * @param time
   *     Time the message was created.
   *
   * @return the id of the inserted row or -1
   */
  public long insertMessage( String tag, String message, long time,
                             long unique, boolean routed ) {
    ContentValues values = new ContentValues( 5 );
    values.put( MSG_COL_TAG, tag );
    values.put( MSG_COL_CONTENT, message );
    values.put( MSG_COL_TIMESTAMP, time );
    values.put( MSG_COL_UNIQUE_ID, unique );
    values.put( MSG_COL_ROUTED, ( routed ? 1 : 0 ) );

    SQLiteDatabase db = super.getWritableDatabase();
    long result;
    result = db.insert( MSG_TABLE_NAME, null, values );
    db.close();

    if ( result >= 0 ) {
      MsgDatabaseHelper.invokeUpdateCallbacks();
    }

    return result;
  }

  /**
   * Adds a callback for database updates.
   *
   * @param callback
   *     callback to be invoked when the database updates.
   */
  public static void addUpdateCallback( UpdateCallback callback ) {
    synchronized ( UPDATE_CALLBACKS ) {
      if ( !UPDATE_CALLBACKS.contains( callback ) ) {
        UPDATE_CALLBACKS.add( callback );
      }
    }
  }

  /**
   * Removes a callback for database updates.
   *
   * @param callback
   *     callback to remove
   */
  public static void removeUpdateCallback( UpdateCallback callback ) {
    synchronized ( UPDATE_CALLBACKS ) {
      UPDATE_CALLBACKS.remove( callback );
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Callback
  //==========================================================================//
  private static final List<UpdateCallback> UPDATE_CALLBACKS =
      new ArrayList<UpdateCallback>();

  /**
   * Interface for callbacks that are invoked when the database is updated.
   */
  public static interface UpdateCallback {
    public void databaseUpdated();
  }

  /**
   * Invokes all update callbacks.
   */
  protected static void invokeUpdateCallbacks() {
    synchronized ( UPDATE_CALLBACKS ) {
      for ( UpdateCallback cb : UPDATE_CALLBACKS ) {
        cb.databaseUpdated();
      }
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Overrides
  //==========================================================================//
  @Override
  public void onCreate( SQLiteDatabase db ) {
    // Generate the table
    //db.execSQL(CREATE_TAG_TABLE);
    db.execSQL( CREATE_MSG_TABLE );


  }

  @Override
  public void onUpgrade( SQLiteDatabase db, int oldVersion,
                         int newVersion ) {
    // Just drop the old tables for now
    db.execSQL( "DROP TABLE IF EXISTS " + MSG_TABLE_NAME );
    db.execSQL( "DROP TABLE IF EXISTS tags" );

    onCreate( db );
  }
  //==========================================================================//
}
