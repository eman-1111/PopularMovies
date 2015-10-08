package app.movies.eman.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by user on 03/10/2015.
 */

public class ReviewAdapter extends CursorAdapter {
    private Context mContext;

    Loader<Cursor> reviewCursor;

    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }




    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_review, parent, false);
        return view;

    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String  review = cursor.getString(MovieDetailFragment.COL_REVIEW);
        String author = cursor.getString(MovieDetailFragment.COL_AUTHOR);

        TextView reviewTV = (TextView)view.findViewById(R.id.list_item_review);
        TextView authorTV = (TextView)view.findViewById(R.id.list_item_author);
        reviewTV.setText(review);
        authorTV.setText(author);


    }

    public void swapCursor(Loader<Cursor> reviewCursor) {
        this.reviewCursor = reviewCursor;
    }
}