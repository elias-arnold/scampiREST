package guerrilla.boards;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

/**
 * Creates a new post.
 */
public class NewPostActivity
    extends Activity {
  private static final String TAG = NewPostActivity.class.getSimpleName();

  private static final Random RNG = new Random();

  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private EditText tagField;
  private EditText msgField;
  private Button submitButton;
  //==========================================================================//


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private ServiceConnection serviceConnection;
  private AppLibService appLibService;
  //==========================================================================//


  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private void setupGuiReferences() {
    this.tagField = ( EditText ) super.findViewById( R.id.PostTagField );
    this.msgField = ( EditText ) super.findViewById( R.id.PostMsgField );
    this.submitButton = ( Button ) super.findViewById( R.id.PostSubmitButton );
  }

  private void setupGuiCallbacks() {
    // Submit
    this.submitButton.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick( View view ) {
        NewPostActivity.this.submitButtonPushed();
      }
    } );
  }
  //==========================================================================//


  //==========================================================================//
  // GUI Callbacks
  //==========================================================================//
  protected void submitButtonPushed() {
    String tag = this.tagField.getText().toString();
    String msg = this.msgField.getText().toString();
    long timestamp = System.currentTimeMillis();
    long uniqueid = RNG.nextLong();
    if ( ( tag != null && tag.length() > 0 ) &&
         ( msg != null && msg.length() > 0 ) ) {
      // Update the database
      MsgDatabaseHelper dbh = new MsgDatabaseHelper( this );
      long id = dbh.insertMessage( tag, msg, timestamp, uniqueid, false );

      // Send the message
      if ( this.appLibService != null ) {
        boolean published = this.appLibService.publish( tag, msg,
            timestamp, uniqueid );
        if ( published ) {
          dbh.setRouted( id );
        }
      } else {
        Log.d( TAG, "Couldn't send message, no AppLib instance." );
      }

      // Get out of this activity
      super.finish();
      super.overridePendingTransition( 0,
          R.anim.spin_off );
    }


  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    super.overridePendingTransition( R.anim.in_from_left,
        R.anim.out_to_right );
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

    setContentView( R.layout.new_post_layout );

    this.setupGuiReferences();
    this.setupGuiCallbacks();

    // Disable submit button until the router connected
    // TODO: The message should go to the database regardless of whether
    // the router is connected or not.
    this.submitButton.setEnabled( false );

    // Set the tag field if required
    Intent i = super.getIntent();
    String tag = i.getStringExtra( "tag" );
    if ( tag != null ) {
      this.tagField.setText( tag );
      this.msgField.requestFocus();
    }
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d( TAG, "onStart()" );

    this.doBindService();
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

    this.doUnbindService();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d( TAG, "onDestroy()" );
  }
  //==========================================================================//


  //==========================================================================//
  // Service Handling
  //------------------------------------------------------------------------//
  // The new post activity must bind to the applib service in order to
  // publish the new message.
  //==========================================================================//
  private void doBindService() {
    this.serviceConnection = this.getServiceConnection();
    super.bindService( new Intent( this, AppLibService.class ),
        this.serviceConnection, Context.BIND_AUTO_CREATE );
  }

  private void doUnbindService() {
    super.unbindService( this.serviceConnection );
  }

  private ServiceConnection getServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected( ComponentName componentName,
                                      IBinder iBinder ) {
        if ( !( iBinder instanceof AppLibService.AppLibBinder ) ) {
          Log.e( TAG, "Wrong type of binder in onServiceConnected()" );
          return;
        }

        AppLibService.AppLibBinder binder =
            ( AppLibService.AppLibBinder ) iBinder;
        NewPostActivity.this.appLibService = binder.getService();

        NewPostActivity.this.submitButton.setEnabled( true );
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        NewPostActivity.this.submitButton.setEnabled( false );
      }
    };
  }
  //==========================================================================//
}
