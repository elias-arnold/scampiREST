package guerrilla.pics;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Thumbnail image view.
 *
 * @author teemuk
 */
public class ThumbImageView
    extends ImageView {

  public ThumbImageView( Context context, AttributeSet attrs ) {
    super( context, attrs );
  }

  @Override
  protected void onMeasure( int widthMeasureSpec,
                            int heightMeasureSpec ) {
    super.onMeasure( widthMeasureSpec, heightMeasureSpec );
    int width = getMeasuredWidth();
    int height = ( int ) Math.round( width / PicStorage.THUMB_ASPECT_RATIO );
    setMeasuredDimension( width, height );
  }

}
