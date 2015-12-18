package guerrilla.pics;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter for binding database items to list view rows.
 *
 * @author teemuk
 */
public class PicListAdapter
    extends BaseAdapter {
  static final String TAG = PicListAdapter.class.getSimpleName();

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private final Object itemsLock = new Object();
  private volatile List<DatabaseController.ItemDescriptor> items;
  private final Context context;
  private final int rowResourceId;
  private final PicStorage picStorage;

  // Need to use manual caching since LruCache is not added until 3.0
  private final Map<String, SoftReference<Drawable>> picCache
      = new ConcurrentHashMap<String, SoftReference<Drawable>>();
  private final List<Drawable> picCacheControl
      = new LinkedList<Drawable>();
  private int MAX_PIC_CACHE = 10;
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//
  public PicListAdapter( Context context, int rowResourceId,
                         List<DatabaseController.ItemDescriptor> items ) {
    super();

    this.context = context;
    this.rowResourceId = rowResourceId;
    this.items = items;

    PicStorage storage = null;
    try {
      storage = new PicStorage( context );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Failed to set up pic storage. (" +
                  e.getMessage() + ")" );
    }
    this.picStorage = storage;
  }

  /**
   * Swap in a new list of items.
   *
   * @param items
   *     list of items
   */
  public void setItems( List<DatabaseController.ItemDescriptor> items ) {
    // Switch the dataset
    synchronized ( this.itemsLock ) {
      this.items = items;
    }

    // Notify the observers that the data changed
    super.notifyDataSetChanged();
  }

  public DatabaseController.ItemDescriptor getItemAtPosition(
      int position ) {
    return this.items.get( position );
  }
  //==========================================================================//


  //==========================================================================//
  // BaseAdapter implementation
  //==========================================================================//
  @Override
  public int getCount() {
    synchronized ( this.itemsLock ) {
      return items.size();
    }
  }

  @Override
  public Object getItem( int position ) {
    synchronized ( this.itemsLock ) {
      return this.items.get( position );
    }
  }

  @Override
  public long getItemId( int position ) {
    synchronized ( this.itemsLock ) {
      return this.items.get( position )._id;
    }
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent ) {
    Log.d( TAG, "getView: " + position );
    Log.d( TAG, "items: " + this.items.size() );

    // Setup the view that we will bind the data to
    View rowLayout;
    if ( convertView != null ) {
      rowLayout = convertView;
    } else {
      LayoutInflater inflater =
          ( LayoutInflater ) context
              .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      rowLayout = inflater.inflate( rowResourceId, parent, false );
    }

    // Grab the GUI elements to populate
    ImageView picView =
        ( ImageView ) rowLayout.findViewById( R.id.picImage );
    TextView titleText =
        ( TextView ) rowLayout.findViewById( R.id.picTitleText );

    // Get the item
    DatabaseController.ItemDescriptor item;
    synchronized ( this.itemsLock ) {
      item = this.items.get( position );
    }

    // Populate the elements
    titleText.setText( item.title );
    // TODO: The images should be loaded in the background, cached,
    // and possibly pre-loaded.
    Drawable pic = null;
    try {
      pic = this.getThumb( item.path );
      picView.setImageDrawable( pic );
    } catch ( FileNotFoundException e ) {
      Log.d( TAG, "Failed to get picture (" + item.path + ")" );
    }

    return rowLayout;
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private Drawable getThumb( String path )
      throws FileNotFoundException {
    if ( this.picCache.containsKey( path ) ) {
      Log.d( TAG, "Returning thumb from cache" );
      Drawable cached = this.picCache.get( path ).get();
      if ( cached != null ) {
        return cached;
      } else {
        this.picCache.remove( path );
      }
    }

    if ( this.picCacheControl.size() >= MAX_PIC_CACHE ) {
      Log.d( TAG, "Purging thumb cache" );
      this.picCacheControl.subList( 0, MAX_PIC_CACHE / 2 ).clear();
    }

    Drawable pic = null;
    try {
      pic = Drawable.createFromPath(
          this.picStorage
              .getThumbnail( path )
              .getAbsolutePath()
      );
    } catch ( OutOfMemoryError e ) {
      // Expunge whole cache
      this.picCacheControl.clear();
      // Reduce cache size
      if ( MAX_PIC_CACHE > 3 ) {
        MAX_PIC_CACHE--;
      }
    }

    Log.d( TAG, "Loaded new thumb" );

    this.picCacheControl.add( pic );
    this.picCache.put( path, new SoftReference<Drawable>( pic ) );

    return pic;
  }
  //==========================================================================//

}
