package guerrilla.boards;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class GuerrillaBoardsActivity
    extends Activity
    implements MsgDatabaseHelper.UpdateCallback {
  private static String TAG = GuerrillaBoardsActivity.class.getSimpleName();

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private MsgDatabaseHelper dbHelper;
  private SimpleCursorAdapter dbAdapter;
  private Cursor dbCursor;

  // List row that was selected previously
  private View selectedItem;
  //==========================================================================//


  //==========================================================================//
  // State vars
  //==========================================================================//
  private enum SortState {
    COUNT_DESC, NAME_ASC, NAME_DESC
  }

  private SortState sortState;
  //==========================================================================//


  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private ListView boardListView;
  private Button sortButton;
  private Button postButton;
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void setupGuiReferences() {
    this.boardListView = ( ListView ) super.findViewById( R.id.BoardList );
    this.sortButton = ( Button ) super.findViewById( R.id.TagsSortButton );
    this.postButton = ( Button ) super.findViewById( R.id.TagsPostButton );
  }

  private void setupGuiCallbacks() {
    // Sort button
    this.sortButton.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick( View view ) {
        GuerrillaBoardsActivity.this.sortButtonPushed();
      }
    } );

    // Post button
    this.postButton.setOnClickListener( new View.OnClickListener() {

      @Override
      public void onClick( View view ) {
        GuerrillaBoardsActivity.this.postButtonPushed();
      }
    } );

    // list view
    this.boardListView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick( AdapterView<?> adapterView, View view,
                                   int position, long id ) {
            GuerrillaBoardsActivity.this.listElementPushed(
                adapterView, view, position, id );
          }
        } );
  }

  private void setupListView() {
    this.dbHelper = new MsgDatabaseHelper( this );
    this.dbCursor = this.dbHelper.getCursorForAllTagsSortByCountDesc();
    this.sortState = SortState.COUNT_DESC;

    // Tie the columns to the views
    String[] columns = new String[] {
        MsgDatabaseHelper.MSG_COL_TAG,
        MsgDatabaseHelper.MSG_COL_COUNT };
    int[] viewFields = new int[] {
        R.id.TagText,
        R.id.CountText };

    // Create adapter
    this.dbAdapter = new SimpleCursorAdapter(
        this,
        R.layout.tag_list_item,
        this.dbCursor,
        columns,
        viewFields
    );

    // Set up the view
    this.boardListView.setAdapter( this.dbAdapter );
  }
  //==========================================================================//


  //==========================================================================//
  // Callbacks
  //==========================================================================//
  protected void sortButtonPushed() {
    if ( this.sortState == SortState.COUNT_DESC ) {
      this.dbCursor = this.dbHelper.getCursorForAllTagsSortedByNameAsc();
      this.dbAdapter.changeCursor( this.dbCursor );
      this.sortState = SortState.NAME_ASC;
    } else if ( this.sortState == SortState.NAME_ASC ) {
      this.dbCursor =
          this.dbHelper.getCursorForAllTagsSortedByNameDesc();
      this.dbAdapter.changeCursor( this.dbCursor );
      this.sortState = SortState.NAME_DESC;
    } else if ( this.sortState == SortState.NAME_DESC ) {
      this.dbCursor = this.dbHelper
          .getCursorForAllTagsSortByCountDesc();
      this.dbAdapter.changeCursor( this.dbCursor );
      this.sortState = SortState.COUNT_DESC;
    }
  }

  protected void postButtonPushed() {
    Intent intent = new Intent();
    intent.setClass( this, NewPostActivity.class );
    super.startActivity( intent );

    super.overridePendingTransition( R.anim.in_from_right,
        R.anim.out_to_left );
  }

  protected void listElementPushed( AdapterView<?> adapterView, View view,
                                    int position, long id ) {
    // Highlight the clicked view
    GuerrillaBoardsActivity.this.selectedItem = view;
    view.setSelected( true );

    // Get the clicked tag
    Cursor cursor = ( Cursor ) adapterView.
        getItemAtPosition( position );
    String tag =
        cursor.getString( cursor.getColumnIndexOrThrow
            ( MsgDatabaseHelper.MSG_COL_TAG ) );


    // Start the message list activity
    Intent intent = new Intent();
    intent.setClass( this, MessageListActivity.class );
    intent.putExtra( "tag", tag );
    startActivity( intent );

    super.overridePendingTransition( R.anim.in_from_right,
        R.anim.out_to_left );
  }

  @Override
  public void databaseUpdated() {
    this.runOnUiThread( new Runnable() {
      @Override
      public void run() {
        GuerrillaBoardsActivity.this.dbCursor.requery();
      }
    } );

  }
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

    // Start the API connectivity to the router
    this.startAppLibService();

    // Setup the GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupListView();

    // Setup db callback
    MsgDatabaseHelper.addUpdateCallback( this );
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

    // Remove highlighting from the previously selected row
    if ( this.selectedItem != null ) {
      this.selectedItem.setSelected( false );
    }

    // Update the list
    this.dbCursor.requery();
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
    MsgDatabaseHelper.removeUpdateCallback( this );
  }
  //==========================================================================//


  //==========================================================================//
  // Service Handling
  //==========================================================================//
  private void startAppLibService() {
    super.startService( new Intent( this, AppLibService.class ) );
  }
  //==========================================================================//
}
