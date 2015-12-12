package guerrilla.pics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

import java.io.FileNotFoundException;

/**
 * Activity to display detailed view of a photo.
 *
 * @author teemuk
 */
public class DetailViewActivity
    extends Activity {
  private static String TAG = DetailViewActivity.class.getSimpleName();

  //==========================================================================//
  // GUI Elements
  //==========================================================================//
  private WebView picView;
  private ImageButton backButton, saveButton, deleteButton;
  //==========================================================================//


  private PicStorage picStorage;


  //==========================================================================//
  // GUI
  //==========================================================================//
  private void setupGuiReferences() {
    this.picView = ( WebView ) super.findViewById( R.id.webView );
    this.backButton = ( ImageButton ) super.findViewById( R.id.backButton );
    this.saveButton = ( ImageButton ) super.findViewById( R.id.saveButton );
    this.deleteButton = ( ImageButton ) super.findViewById( R.id.deleteButton );
  }

  private void setupGuiCallbacks() {
    this.backButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick( View view ) {
            backButtonPushed();
          }
        }
    );

    this.saveButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick( View view ) {
            saveButtonPushed();
          }
        }
    );

    this.deleteButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick( View view ) {
            deleteButtonPushed();
          }
        }
    );
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

    // Setup pic storage
    if ( this.picStorage == null ) {
      try {
        this.picStorage = new PicStorage( this );
      } catch ( FileNotFoundException e ) {
        Log.d( TAG, "Failed to setup picture storage. (" +
                    e.getMessage() + ")" );
        return;
      }
    }

    // GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();

    // Set the text field
    Intent i = super.getIntent();
    String path = i.getStringExtra( "path" );
    if ( path != null ) {
      String fullPath;
      try {
        fullPath = this.picStorage.getPicture( path )
            .getAbsolutePath();
      } catch ( FileNotFoundException e ) {
        Log.d( TAG, "Failed to get picture (" + e.getMessage() + ")" );
        return;
      }
      String imageUrl = "file://" + fullPath;
      Log.d( TAG, "Image URL: " + imageUrl );
      this.picView.getSettings().setBuiltInZoomControls( true );
      this.picView.getSettings().setUseWideViewPort( true );
      this.picView.getSettings().setLoadWithOverviewMode( true );
      //this.picView.setInitialScale( 100 );
      this.picView.loadUrl( imageUrl );
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


  //==========================================================================//
  // Callbacks
  //==========================================================================//
  private void saveButtonPushed() {
    Log.d( TAG, "Save button pushed." );
  }

  private void backButtonPushed() {
    Log.d( TAG, "Back button pushed." );
    super.finish();
  }

  private void deleteButtonPushed() {
    Log.d( TAG, "Delete button pushed." );
  }
  //==========================================================================//

}
