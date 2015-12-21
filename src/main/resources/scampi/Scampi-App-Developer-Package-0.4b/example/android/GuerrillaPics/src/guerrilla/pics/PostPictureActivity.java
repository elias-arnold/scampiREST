package guerrilla.pics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity to post pictures.
 *
 * @author teemuk
 */
public class PostPictureActivity
    extends Activity {
  private static final String TAG = PostPictureActivity.class.getSimpleName();

  //=========================================================================//
  // Constants
  //=========================================================================//
  /** Layout resource to use for this activity. */
  private static final int LAYOUT = R.layout.post_pic_layout;
  //=========================================================================//


  //=========================================================================//
  // GUI Elements
  //=========================================================================//

  //=========================================================================//


  //=========================================================================//
  // Instance vars
  //=========================================================================//

  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    super.setContentView( LAYOUT );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();
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
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {

  }

  private void setupGuiCallbacks() {

  }

  private void setupGuiElements() {

  }
  //=========================================================================//
}
