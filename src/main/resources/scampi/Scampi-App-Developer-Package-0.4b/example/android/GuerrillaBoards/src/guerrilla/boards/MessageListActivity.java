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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Activity that displays the message list.
 *
 * @author teemuk
 */
public class MessageListActivity
    extends Activity
    implements MsgDatabaseHelper.UpdateCallback {
  private static String TAG = MessageListActivity.class.getSimpleName();

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private MsgDatabaseHelper dbHelper;
  private SimpleCursorAdapter dbAdapter;
  private Cursor dbCursor;
  //==========================================================================//


  //==========================================================================//
  // State vars
  //==========================================================================//
  private enum SortState {
    ASC, DESC
  }

  private SortState sortState;
  private String tag;
  private View selectedItem;
  //==========================================================================//


  //==========================================================================//
  // Private vars
  //==========================================================================//
  // Formatting of the timestamp in list items
  private String datePattern = "HH:mm:ss d.M.yyyy";
  private SimpleDateFormat timeformatter = new SimpleDateFormat( datePattern );
  //==========================================================================//


  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private ListView msgListView;
  private TextView titleText;
  private Button postButton;
  private Button sortButton;
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void setupGuiReferences() {
    this.msgListView = ( ListView ) super.findViewById( R.id.MessageList );
    this.titleText = ( TextView ) super.findViewById( R.id.TagTitle );
    this.postButton = ( Button ) super.findViewById( R.id.MsgListPostButton );
    this.sortButton = ( Button ) super.findViewById( R.id.MsgListSortButton );
  }

  private void setupGuiCallbacks() {
    // Post button
    this.postButton.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick( View view ) {
        MessageListActivity.this.postButtonPushed();
      }
    } );

    // Sort button
    this.sortButton.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick( View view ) {
        MessageListActivity.this.sortButtonPushed();
      }
    } );

    // List view
    this.msgListView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick( AdapterView<?> adapterView, View view,
                                   int i, long l ) {
            MessageListActivity.this.listElementPushed( adapterView,
                view, i, l );
          }
        } );
  }

  private void setupListView() {
    // Populate the list
    this.dbHelper = new MsgDatabaseHelper( this );
    this.dbCursor = this.dbHelper.getMessagesForTagSortDesc( this.tag );
    this.sortState = SortState.DESC;

    String[] columns = new String[] {
        MsgDatabaseHelper.MSG_COL_CONTENT,
        MsgDatabaseHelper.MSG_COL_TIMESTAMP };
    int[] viewFields = new int[] {
        R.id.MsgText,
        R.id.TimeText };

    this.dbAdapter = new SimpleCursorAdapter(
        this,
        R.layout.msg_list_item,
        this.dbCursor,
        columns,
        viewFields
    );

    // Create binder to transform timestamp to a formatted date
    this.dbAdapter.setViewBinder( new SimpleCursorAdapter.ViewBinder() {
      @Override
      public boolean setViewValue( View view, Cursor cursor, int i ) {
        if ( i == 2 ) {
          if ( view instanceof TextView ) {
            String timestamp = cursor.getString( i );
            Date date = new Date( Long.valueOf( timestamp ) );
            TextView textView = ( TextView ) view;
            textView.setText(
                MessageListActivity.this
                    .timeformatter.format( date ) );
            return true;
          }
        }

        return false;
      }
    } );

    this.msgListView.setAdapter( this.dbAdapter );
  }
  //==========================================================================//


  //==========================================================================//
  // Callbacks
  //==========================================================================//
  protected void postButtonPushed() {
    Intent intent = new Intent();
    intent.setClass( this, NewPostActivity.class );

    intent.putExtra( "tag", this.tag );

    super.startActivity( intent );

    super.overridePendingTransition( R.anim.in_from_right,
        R.anim.out_to_left );
  }

  protected void sortButtonPushed() {
    if ( this.sortState == SortState.ASC ) {
      this.dbCursor = this.dbHelper.getMessagesForTagSortDesc( this.tag );
      this.dbAdapter.changeCursor( this.dbCursor );
      this.sortState = SortState.DESC;
    } else if ( this.sortState == SortState.DESC ) {
      this.dbCursor =
          this.dbHelper.getMessagesForTagSortAsc( this.tag );
      this.dbAdapter.changeCursor( this.dbCursor );
      this.sortState = SortState.ASC;
    }
  }

  protected void listElementPushed( AdapterView<?> adapterView, View view,
                                    int position, long id ) {
    // Highlight the clicked view
    MessageListActivity.this.selectedItem = view;
    view.setSelected( true );

    // Get the clicked a message
    Cursor cursor = ( Cursor ) adapterView.
        getItemAtPosition( position );
    String msg =
        cursor.getString( cursor.getColumnIndexOrThrow
            ( MsgDatabaseHelper.MSG_COL_CONTENT ) );


    // Start the message list activity
    Intent intent = new Intent();
    intent.setClass( this, DetailViewActivity.class );
    intent.putExtra( "msg", msg );
    super.startActivity( intent );

    super.overridePendingTransition( R.anim.in_from_right,
        R.anim.out_to_left );
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    super.overridePendingTransition( R.anim.in_from_left,
        R.anim.out_to_right );
  }

  @Override
  public void databaseUpdated() {
    super.runOnUiThread( new Runnable() {
      @Override
      public void run() {
        MessageListActivity.this.dbCursor.requery();
      }
    } );

  }
  //==========================================================================//


  //==========================================================================//
  // Life cycle callbacks
  //==========================================================================//
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    Log.d( TAG, "onCreate()" );

    setContentView( R.layout.message_list );

    // Get the tag
    Intent intent = super.getIntent();
    this.tag = intent.getStringExtra( "tag" );

    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupListView();

    // Setup title
    if ( this.tag != null && this.titleText != null ) {
      this.titleText.setText( "#" + this.tag );
    }

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

    // Remove highlighting from the previously selected row
    if ( this.selectedItem != null ) {
      this.selectedItem.setSelected( false );
    }

    // Update the list
    this.dbCursor.requery();

    Log.d( TAG, "onResume()" );

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

}
