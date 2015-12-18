package guerrilla.pics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * {@code PicStorage} abstracts picture storage on the filesystem.
 *
 * @author teemuk
 */
public class PicStorage {
  private static final String TAG = PicStorage.class.getSimpleName();

  //==========================================================================//
  // Constants
  //==========================================================================//
  public static final String ROOT_DIR
      = "GuerrillaPics";
  public static final String PICS_DIR
      = "pics";
  public static final String THUMB_DIR
      = "thumbnails";

  public static final double THUMB_ASPECT_RATIO = 16.0 / 6.0;
  //==========================================================================//


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private final File rootDir;
  private final File picsDir;
  private final File thumbDir;

  private final int thumbWidth, thumbHeight;

  private static Random RNG = new Random();
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//
  public PicStorage( Context context )
      throws FileNotFoundException {
    this.rootDir = this.setupRootDirectory( ROOT_DIR );
    this.picsDir = this.setupPicDirectory( rootDir, PICS_DIR );
    this.thumbDir = this.setupPicDirectory( rootDir, THUMB_DIR );

    WindowManager wm = ( WindowManager ) context
        .getSystemService( Context.WINDOW_SERVICE );
    DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics( metrics );
    this.thumbWidth = metrics.widthPixels;
    this.thumbHeight = ( int ) Math.round( this.thumbWidth /
                                           THUMB_ASPECT_RATIO );
  }

  public String addPicture( File srcFile )
      throws IOException {
    // Save image
    File outFile = this.getPicFile( this.picsDir );
    this.copy( srcFile, outFile );

    // Create and save thumbnail
    File thumbFile = new File( this.thumbDir, outFile.getName() );
    this.createThumbnail( srcFile, thumbFile,
        this.thumbWidth, this.thumbHeight );

    return outFile.getName();
  }

  public String addPicture( File srcFile, int width, int height )
      throws IOException {
    // Save image
    File outFile = this.getPicFile( this.picsDir );
    this.createThumbnail( srcFile, outFile, width, height );

    // Create and save thumbnail
    File thumbFile = new File( this.thumbDir, outFile.getName() );
    this.createThumbnail( srcFile, thumbFile,
        this.thumbWidth, this.thumbHeight );

    return outFile.getName();
  }

  public String addFile( File srcFile )
      throws IOException {
    if ( this.picsDir.compareTo( srcFile.getParentFile() ) != 0 ) {
      throw new IllegalArgumentException( "srcFile must be inside the picture" +
                                          " storage directory." );
    }

    // Create and save thumbnail
    File thumbFile = new File( this.thumbDir, srcFile.getName() );
    this.createThumbnail( srcFile, thumbFile,
        this.thumbWidth, this.thumbHeight );

    return srcFile.getName();
  }

  public File getPicture( String name )
      throws FileNotFoundException {
    // Pre-condition check
    if ( name == null || name.length() == 0 ) {
      throw new IllegalArgumentException( "Name cannot be null or " +
                                          "empty." );
    }

    // Get file
    File file = new File( this.picsDir, name );
    if ( !file.exists() || !file.isFile() ) {
      throw new FileNotFoundException( "No file of the given name " +
                                       "found." );
    }

    return file;
  }

  public File getThumbnail( String name )
      throws FileNotFoundException {
    // Pre-condition check
    if ( name == null || name.length() == 0 ) {
      throw new IllegalArgumentException( "Name cannot be null or " +
                                          "empty." );
    }

    // Get file
    File file = new File( this.thumbDir, name );
    if ( !file.exists() || !file.isFile() ) {
      throw new FileNotFoundException( "No file of the given name " +
                                       "found." );
    }

    return file;
  }

  public boolean containsPicture( String name ) {
    // Pre-condition check
    if ( name == null || name.length() == 0 ) {
      throw new IllegalArgumentException( "Name cannot be null or " +
                                          "empty." );
    }

    File file = new File( this.picsDir, name );
    return ( file.exists() && file.isFile() );
  }

  public File getEmptyFile()
      throws FileNotFoundException {
    return this.getPicFile( this.picsDir );
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private File setupRootDirectory( String name )
      throws FileNotFoundException {
    File dlDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS );

    File rootDir = new File( dlDir, name );
    this.mkdirs( rootDir );

    return rootDir;
  }

  private File setupPicDirectory( File rootDir, String name )
      throws FileNotFoundException {
    File picsDir = new File( rootDir, name );
    this.mkdirs( picsDir );
    return picsDir;
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

  /**
   * Returns a non-existent file in the pics directory.
   *
   * @param picsDir
   *     the pics directory
   *
   * @return file in the directory that does not exist yet
   *
   * @throws FileNotFoundException
   */
  private File getPicFile( File picsDir )
      throws FileNotFoundException {
    // Precondition check
    if ( !picsDir.exists() || !picsDir.isDirectory() ) {
      throw new IllegalArgumentException( "picsDir must be an existing " +
                                          "directory." );
    }

    // Try to find a non-existent file
    File file;
    int maxTryCount = 100;
    int tries = 0;
    do {
      String name = "pic-" + System.currentTimeMillis() + "-" +
                    RNG.nextInt( Integer.MAX_VALUE ) + ".jpg";
      file = new File( picsDir, name );
      tries++;
    }
    while ( file.exists() &&
            ( tries < maxTryCount ) );

    // Check if we were successful
    if ( file.exists() ) {
      throw new FileNotFoundException( "Could not find a non-existent " +
                                       "file name." );
    }

    return file;
  }

  private void streamToFile( InputStream in, File out )
      throws FileNotFoundException {
    // Get output stream
    FileOutputStream fout = new FileOutputStream( out );

    // Copy loop
    byte[] buffer = new byte[ 10 * 1024 ];

    int r = -1;
    try {
      while ( ( r = in.read( buffer ) ) > 0 ) {
        fout.write( buffer, 0, r );
      }
    } catch ( IOException e ) {
      // Clean the target file
      Log.e( TAG, "Failed to write to file (" + e.getMessage() + ")." );
      try {
        fout.close();
      } catch ( IOException e2 ) {
      }
      fout = null;
      if ( !out.delete() ) {
        Log.e( TAG, "Failed to delete the target file." );
      }
    } finally {
      // Close streams
      if ( fout != null ) {
        try {
          fout.close();
        } catch ( IOException e ) {
        }
      }
      if ( in != null ) {
        try {
          in.close();
        } catch ( IOException e ) {
        }
      }
    }
  }

  private void copy( File source, File destination )
      throws IOException {
    // Pre-condition check
    if ( !source.exists() || !source.isFile() ) {
      throw new FileNotFoundException( "File '" +
                                       source.getAbsolutePath() +
                                       "' does not exist or is not a " +
                                       "file." );
    }

    // Delete destination if it exists
    if ( destination.exists() ) {
      if ( !destination.isFile() ) {
        throw new IllegalArgumentException( "'" +
                                            destination.getAbsolutePath() +
                                            "' exists and is not a " +
                                            "file." );
      }
      if ( !destination.delete() ) {
        throw new IOException( "Failed to delete existing " +
                               "destination '" + destination.getAbsolutePath() +
                               "'." );
      }
    }

    // Copy loop
    FileInputStream fin = new FileInputStream( source );
    FileOutputStream fout = new FileOutputStream( destination );

    try {
      int r;
      byte[] buffer = new byte[ 10 * 1024 ];
      while ( ( r = fin.read( buffer ) ) >= 0 ) {
        fout.write( buffer, 0, r );
      }
    } finally {
      try {
        fin.close();
      } catch ( IOException e ) {
        Log.e( TAG, "Failed to close input stream (" + e.getMessage()
                    + ")." );
      }
      try {
        fout.close();
      } catch ( IOException e ) {
        Log.e( TAG, "Failed to close input stream (" + e.getMessage()
                    + ")." );
      }
    }
  }

  /**
   * Creates a scaled thumbnail that will display in an area of a given width
   * and height. The resulting image will not be the exact width and height,
   * instead it's scaled down to the smallest possible size such that: the scale
   * factor is a power of 2 and both dimensions of the resulting image are equal
   * or larger than the corresponding dimensions of the original image.
   *
   * @param srcFile
   *     source file
   * @param dstFile
   *     destination file
   * @param width
   *     target width
   * @param height
   *     target height
   */
  private void createThumbnail( File srcFile, File dstFile,
                                int width, int height )
      throws IOException {
    //Decode image size
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;

    FileInputStream fis = null;
    try {
      fis = new FileInputStream( srcFile );
      BitmapFactory.decodeStream( fis, null, o );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Couldn't create thumbnail. (" +
                  e.getMessage() + ")" );
    } finally {
      if ( fis != null ) {
        try {
          fis.close();
        } catch ( Exception e ) {
          Log.d( TAG, "Failed to close source file when creating a " +
                      "thumbnail (" + e.getMessage() + ")." );
        }
      }
    }

    // Calculate scales
    int h_scale = 1;
    while ( o.outWidth / h_scale >= width ) {
      h_scale *= 2;
    }
    h_scale /= 2;
    int v_scale = 1;
    while ( o.outHeight / v_scale >= height ) {
      v_scale *= 2;
    }
    v_scale /= 2;
    // Pick the smaller scale factor
    int scale = ( h_scale <= v_scale ) ? ( h_scale ) : ( v_scale );
    if ( scale <= 0 ) {
      throw new IOException( "Source picture dimensions " +
                             "are invalid." );
    }
    int w = o.outWidth / scale;
    int h = o.outHeight / scale;
    Log.d( TAG, "Thumbnail scale factor: " + scale + ", " +
                "resulting dimensions: " + w + " x " + h + ", " +
                "target dimensions: " + width + " x " + height );

    // Decode scaled down bitmap
    BitmapFactory.Options o2 = new BitmapFactory.Options();
    o2.inSampleSize = scale;

    fis = null;
    Bitmap bitmap = null;
    try {
      fis = new FileInputStream( srcFile );
      bitmap = BitmapFactory.decodeStream( fis, null, o2 );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Couldn't create thumbnail. (" +
                  e.getMessage() + ")" );
    } finally {
      if ( fis != null ) {
        try {
          fis.close();
        } catch ( Exception e ) {
          Log.d( TAG, "Failed to close source file when creating a " +
                      "thumbnail (" + e.getMessage() + ")." );
        }
      }
    }

    // Save bitmap to destination
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream( dstFile );
      bitmap.compress( Bitmap.CompressFormat.JPEG, 65, fout );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Couldn't create thumbnail. (" +
                  e.getMessage() + ")" );
    } finally {
      if ( fout != null ) {
        try {
          fout.flush();
        } catch ( Exception e ) {
          Log.d( TAG, "Failed to write file when creating a " +
                      "thumbnail (" + e.getMessage() + ")." );
        }
        try {
          fout.close();
        } catch ( Exception e ) {
          Log.d( TAG, "Failed to close destination file when " +
                      "creating a " +
                      "thumbnail (" + e.getMessage() + ")." );
        }
      }
    }
  }
  //==========================================================================//
}
