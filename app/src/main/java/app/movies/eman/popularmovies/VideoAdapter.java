package app.movies.eman.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by user on 26/09/2015.
 */
public class VideoAdapter extends CursorAdapter {
    private Context mContext;


    public VideoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }




    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_video, parent, false);
        return view;

    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String key = cursor.getString(MovieDetailFragment.COL_ADDRESS);

        TextView keyTV = (TextView)view.findViewById(R.id.list_item_trailer);
    }

}
