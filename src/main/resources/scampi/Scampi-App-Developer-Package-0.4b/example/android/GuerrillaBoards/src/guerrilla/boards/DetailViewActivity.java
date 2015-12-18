package guerrilla.boards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA. User: teemuk Date: 4/18/13 Time: 16:08 To change
 * this template use File | Settings | File Templates.
 */
public class DetailViewActivity
    extends Activity {
  private static String TAG = DetailViewActivity.class.getSimpleName();

  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private TextView msgText;
  //==========================================================================//


  //==========================================================================//
  // GUI
  //==========================================================================//
  private void setupGuiReferences() {
    this.msgText = ( TextView ) super.findViewById( R.id.DetailMessageText );
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

    setContentView( R.layout.detail_layout );

    // GUI
    this.setupGuiReferences();

    // Set the text field
    Intent i = super.getIntent();
    String msg = i.getStringExtra( "msg" );
    if ( msg != null ) {
      this.msgText.setText( msg );
    }

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
  }
  //==========================================================================//
}
