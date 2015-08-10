package app.movies.eman.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by user on 31/07/2015.
 */
public class MoviesProvider extends ContentProvider {


   private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_IMAGE = 101;

    private static final String sImageSettingSelection =
            MoviesContract.MoviesEntry.TABLE_NAME +
                    "." + MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH + " = ? ";


private Cursor getMoviesByImage(Uri uri, String[] projection, String sortOrder) {
    String ImageURL= MoviesContract.MoviesEntry.getImageFromUri(uri);

    String  selection = sImageSettingSelection;
    String[]  selectionArgs = new String[]{ImageURL};

 return   mOpenHelper.getReadableDatabase().query(
            MoviesContract.MoviesEntry.TABLE_NAME, projection,selection,
            selectionArgs,null,null,sortOrder);

}


    static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*", MOVIE_WITH_IMAGE);


        return matcher;
    }

    @Override
    public String getType(Uri uri) {


        final int match = sUriMatcher.match(uri);

        switch (match) {


            case MOVIE:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIE_WITH_IMAGE:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "movies/*"
            case MOVIE_WITH_IMAGE: {
                retCursor = getMoviesByImage( uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME, projection,selection,
                        selectionArgs,null,null,sortOrder);


                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case MOVIE: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME,null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MoviesEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {


        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowMatched ;
        switch (match){
            case MOVIE:
                rowMatched = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
  }

        if(rowMatched != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowMatched;
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {

            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);

            int rowMatched;
            switch (match){
                case MOVIE: {
                    rowMatched = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values,selection, selectionArgs);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }


            if(rowMatched != 0){
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowMatched;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
