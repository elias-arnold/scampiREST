package guerrilla.pics;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity
    extends Activity
    implements DatabaseController.OnUpdated {

  /** Logging tag */
  private static final String TAG = MainActivity.class.getSimpleName();
  /** Request code for selecting a picture. */
  private static final int SELECT_PICTURE = 1;

  //==========================================================================//
  // GUI elements
  //==========================================================================//
  private ListView mainList;
  private ImageButton publishButton;
  //==========================================================================//


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private final Object adapterLock
      = new Object();
  private PicListAdapter adapter;
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  private final Random RNG
      = new Random();
  private PicStorage picStorage;
  //==========================================================================//


  //==========================================================================//
  // Lifecycle
  //==========================================================================//

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" );
    setContentView( R.layout.main );

    // Setup pic storage
    // TODO: Fail cleanly when storage is not available
    try {
      this.picStorage = new PicStorage( this );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Failed to setup picture storage. (" +
                  e.getMessage() + ")" );
      return;
    }

    // Setup the GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupListView();

    // Start the API and the database
    this.startServices();

    // Bind to the database
    this.doBindDatabaseService();
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d( TAG, "onStart()" );
  }

  @Override
  public void onRestart() {
    super.onRestart();

    Log.d( TAG, "onRestart()" );
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d( TAG, "onResume()" );

    // Update the list
    this.adapter.notifyDataSetChanged();
  }

  @Override
  public void onPause() {
    super.onPause();

    Log.d( TAG, "onPause()" );
  }

  @Override
  public void onStop() {
    super.onStop();

    Log.d( TAG, "onStop()" );
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d( TAG, "onDestroy()" );

    // Remove db callback
    if ( this.db != null ) {
      this.db.removeUpdateCallback( this );
    }

    // Unbind from the database
    this.doUnbindDatabaseService();
  }
  //==========================================================================//


  //==========================================================================//
  // GUI callbacks
  //==========================================================================//
  protected void listElementPushed( AdapterView<?> adapterView, View view,
                                    int position, long id ) {
    DatabaseController.ItemDescriptor item
        = this.adapter.getItemAtPosition( position );

    Intent intent = new Intent();
    intent.setClass( this, DetailViewActivity.class );
    intent.putExtra( "path", item.path );

    super.startActivity( intent );
  }

  protected void publishButtonPushed() {
    Intent i = new Intent(
        Intent.ACTION_PICK,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );

    startActivityForResult( i, SELECT_PICTURE );
  }
  //==========================================================================//


  //==========================================================================//
  // Activity results
  //--------------------------------------------------------------------------//
  // Invoked when, e.g., the user selects a photo from the gallery
  //==========================================================================//
  @Override
  public void onActivityResult( int requestCode, int resultCode,
                                Intent data ) {
    if ( requestCode == SELECT_PICTURE &&
         resultCode == RESULT_OK &&
         data != null ) {
      try {
        Uri selectedImageUri = data.getData();
        String selectedImagePath = getPathForUri( selectedImageUri );
        String name = this.picStorage
            .addPicture( new File( selectedImagePath ),
                800, 800 );
        this.insertIntoDatabase( name, "Test" );
      } catch ( IOException e ) {
        Log.d( TAG, "Failed to insert picture from gallery: " +
                    e.getMessage() );
      }
    }
  }
  //==========================================================================//


  //==========================================================================//
  // DatabaseController callback
  //==========================================================================//
  @Override
  public void onUpdated(
      final List<DatabaseController.ItemDescriptor> items ) {
    // Swap the new items into the adapter
    synchronized ( this.adapterLock ) {
      Log.d( TAG, "Posting adapter update with " + items.size() +
                  " item(s) (" + this.adapter + ")" );
      super.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              adapter.setItems( items );
              Log.d( TAG, "Set adapter items (" + adapter + ")" );
              Log.d( TAG, "ListView adapter: " +
                          mainList.getAdapter() );
            }
          }
      );

    }
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void setupGuiReferences() {
    this.mainList = ( ListView ) super.findViewById( R.id.mainListView );
    this.publishButton = ( ImageButton ) super
        .findViewById( R.id.publishButton );
  }

  private void setupGuiCallbacks() {
    // Publish button
    this.publishButton.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick( View view ) {
        MainActivity.this.publishButtonPushed();
      }
    } );

    // list view
    this.mainList.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick( AdapterView<?> adapterView,
                                   View view, int position, long id ) {
            MainActivity.this.listElementPushed(
                adapterView, view, position, id );
          }
        } );
  }

  private void setupListView() {
    // Create adapter for the list
    this.adapter =
        new PicListAdapter( this, R.layout.pic_list_row,
            new ArrayList<DatabaseController.ItemDescriptor>( 0 ) );

    // Set the adapter
    this.mainList.setAdapter( this.adapter );
    Log.d( TAG, "Set listview adapter. (" + this.adapter + ")" );
    this.adapter.notifyDataSetChanged();
  }

  private void startServices() {
    super.startService( new Intent( this, DatabaseController.class ) );
    super.startService( new Intent( this, AppLibService.class ) );
  }

  private String getPathForUri( Uri uri ) {
    String[] filePathColumn = { MediaStore.Images.Media.DATA };

    Cursor cursor = getContentResolver()
        .query( uri, filePathColumn, null, null, null );
    cursor.moveToFirst();
    int columnIndex = cursor.getColumnIndex( filePathColumn[ 0 ] );
    String picturePath = cursor.getString( columnIndex );
    cursor.close();

    return picturePath;
  }

  private void insertIntoDatabase( String path, String title ) {
    if ( this.db != null ) {
      this.db.insertItem( path, title, System.currentTimeMillis(),
          RNG.nextLong(), false, false, null );
    } else {
      // TODO: Add the pre-connect buffer and insert after the database
      // connects
      Log.e( TAG, "Failed to publish the image, database is not " +
                  "connected." );
    }
  }
  //==========================================================================//


  //==========================================================================//
  // Binding to DatabaseController
  //--------------------------------------------------------------------------//
  // The AppLib service binds to the database controller in order to insert
  // received messages and to publish newly generated messages.
  //==========================================================================//
  private void doBindDatabaseService() {
    this.databaseConnection = this.getServiceConnection();
    super.bindService( new Intent( this, DatabaseController.class ),
        this.databaseConnection, Context.BIND_AUTO_CREATE );
  }

  private void doUnbindDatabaseService() {
    super.unbindService( this.databaseConnection );
  }

  private ServiceConnection getServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected( ComponentName componentName,
                                      IBinder iBinder ) {
        if ( !( iBinder instanceof DatabaseController.DatabaseBinder ) ) {
          Log.e( TAG, "Wrong type of binder in onServiceConnected()" );
          return;
        }

        Log.d( TAG, "Database connected" );

        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        MainActivity.this.db = binder.getService();
        MainActivity.this.db.addUpdateCallback( MainActivity.this );
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        MainActivity.this.db = null;
      }
    };
  }
  //==========================================================================//

}
