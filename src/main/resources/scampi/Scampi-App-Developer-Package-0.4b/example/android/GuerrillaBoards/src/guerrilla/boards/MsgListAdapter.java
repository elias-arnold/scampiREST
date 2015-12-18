package guerrilla.boards;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 *
 */
public class MsgListAdapter
    extends ArrayAdapter<String[]> {

  public static final int overlayColor = 0x50000000;

  private List<String[]> tags;
  private LayoutInflater inflater;
  private int layoutResource;

  public MsgListAdapter( Context context,
                         int textViewResourceId,
                         List<String[]> values ) {
    super( context, textViewResourceId, values );
    this.tags = values;
    this.inflater = LayoutInflater.from( context );
    this.layoutResource = textViewResourceId;
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent ) {
    final ViewHolder holder;
    View v = convertView;
    if ( v == null ) {
      v = this.inflater.inflate( this.layoutResource, parent, false );
      holder = new ViewHolder();
      holder.msgText = ( TextView ) v.findViewById( R.id.MsgText );
      holder.timeText = ( TextView ) v.findViewById( R.id.TimeText );
      v.setTag( holder );
    } else {
      holder = ( ViewHolder ) v.getTag();
    }

    String[] line = this.tags.get( position );
    if ( line != null ) {
      holder.msgText.setText( line[ 0 ] );
      holder.timeText.setText( line[ 1 ] );
    } else {
      Log.d( "debug", "List item doesn't exist for position " + position
                      + "." );
    }

    return v;
  }


  private static class ViewHolder {
    TextView msgText;
    TextView timeText;
  }
}
