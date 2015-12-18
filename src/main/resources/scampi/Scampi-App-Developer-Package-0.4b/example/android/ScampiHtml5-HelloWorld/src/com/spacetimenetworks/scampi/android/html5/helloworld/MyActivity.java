package com.spacetimenetworks.scampi.android.html5.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.spacetimenetworks.scampi.android.html5.ScampiJavaScriptInterface;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Activity that loads a WebView with the Scampi HTML5 API and a simple Hello
 * World page.
 *
 * @author teemuk
 */
public class MyActivity
extends Activity {
  private static final String TAG = MyActivity.class.getSimpleName();

  //==========================================================================//
  // GUI elements
  //==========================================================================//
  /** The WebView component used by the HTML5 app. */
  private WebView webView;
  //==========================================================================//


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  /** Controller used to start and stop the Scampi HTML5 API instance. */
  private ScampiJavaScriptInterface.Controller javaScriptController;
  /** Directory for storing incoming files (if files are attached as a part
   * of the published Scampi Messages). */
  private File storageDir;
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

    // Setup the GUI
    this.setupGuiReferences();

    // Setup storage directory
    try {
      this.storageDir = this.setupRootDirectory( "ScampiHtml5HelloWorld" );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Failed to setup storage directory. Aborting." );
      return;
    }

    // Setup the web view
    WebSettings settings = this.webView.getSettings();
    settings.setJavaScriptEnabled( true );
    settings.setUserAgentString( "ScampiHelloWorld" );
    settings.setDomStorageEnabled( true );
    this.setupWebView( this.webView, this.storageDir );

    this.webView.loadUrl( "file:///android_asset/hello.html" );
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Stop the API controller, otherwise the AppLib instance will stay alive.
    this.javaScriptController.stop();

    Log.d( TAG, "onDestroy()" );
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void setupGuiReferences() {
    this.webView = ( WebView ) super.findViewById( R.id.webView );
  }

  private void setupWebView( WebView view, File fileStorage ) {
    // Create Scampi JavaScript interface
    ScampiJavaScriptInterface.InterfaceInstance intf
        = ScampiJavaScriptInterface.builder( webView, fileStorage ).build();

    // Used to start and stop the JavaScript interface (which uses AppLib
    // internally)
    this.javaScriptController = intf.controller;
    this.javaScriptController.start();

    // Add the interface to the WebView
    view.addJavascriptInterface( intf.javaScriptInterface, "Scampi" );

  }

  private File setupRootDirectory( String name )
  throws FileNotFoundException {
    File dlDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS );

    File rootDir = new File( dlDir, name );
    this.mkdirs( rootDir );

    return rootDir;
  }

  private void mkdirs( File dir )
      throws FileNotFoundException {
    if ( dir.exists() ) {
      if ( !dir.isDirectory() ) {
        throw new FileNotFoundException(
            "Directory exists as a file '" +
            dir.getAbsolutePath() + "'" );
      }
    } else {
      if ( !dir.mkdirs() ) {
        throw new FileNotFoundException(
            "Failed to create directory '" +
            dir.getAbsolutePath() + "'." );
      }
    }
  }
  //==========================================================================//
}
