package app.movies.eman.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by user on 29/07/2015.
 */

public class MoviesAdapter extends CursorAdapter {

    private Context mContext;
    String baseURL = "http://image.tmdb.org/t/p/w185/";
        public MoviesAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mContext = context;
        }

    public static String getSortBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));
    }



        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
            return view;

        }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.


        String image = cursor.getString(MoviesFragment.COL_IMAGE_PATH);
       String imageURL = baseURL + image;
        ImageView movieImageView = (ImageView) view.findViewById(R.id.list_item_icon);
        Picasso.with(context).load(imageURL).into(movieImageView);





    }


}
