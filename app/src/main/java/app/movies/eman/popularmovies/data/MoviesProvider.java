package app.movies.eman.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by user on 31/07/2015.
 */
public class MoviesProvider extends ContentProvider {


   private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int VIDEO = 200;
    static final int REVIEW =300;





    private static final String sIDSettingSelection =
           MoviesContract.MoviesEntry.TABLE_NAME +
                   "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ? ";
//
//    private static final String sReviewSettingSelection =
//            MoviesContract.VideoEntry.TABLE_NAME +
//                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_IMAGE + " = ? ";



    private Cursor getMoviesById(Uri uri, String[] projection, String sortOrder) {
        int  movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

        String selection = sIDSettingSelection;
        String[] selectionArgs = new String[]{Integer.toString(movieId)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.MoviesEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
    }

//    private Cursor getVideosById(Uri uri, String[] projection, String sortOrder) {
//        String image = MoviesContract.VideoEntry.getImageFromURL(uri);
//
//        String selection = sVideoSettingSelection;
//        String[] selectionArgs = new String[]{image};
//
//        return mOpenHelper.getReadableDatabase().query(
//                MoviesContract.VideoEntry.TABLE_NAME, projection, selection,
//                selectionArgs, null, null, sortOrder);
//
//
//    }
//
//    private Cursor getReviewsById(Uri uri, String[] projection, String sortOrder) {
//        String image = MoviesContract.VideoEntry.getImageFromURL(uri);
//        String selection = sReviewSettingSelection;
//        String [] selectionArgs = new String[]{image};
//
//        return mOpenHelper.getReadableDatabase().query(
//                MoviesContract.VideoEntry.TABLE_NAME, projection, selection,
//                selectionArgs, null, null, sortOrder);
//
//
//    }


    static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_VIDEO , VIDEO);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW , REVIEW);



        return matcher;
    }

    @Override
    public String getType(Uri uri) {


        final int match = sUriMatcher.match(uri);

        switch (match) {


            case MOVIE:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case VIDEO:
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
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
            case MOVIE_WITH_ID: {
                retCursor = getMoviesById( uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME, projection,selection,
                        selectionArgs,null,null,sortOrder);
                break;
            }


            //"video/#"
            case VIDEO: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.VideoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            }

            //"review/#"
            case REVIEW: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);

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
                    throw new SQLException("Failed to insert row into " + uri);
                break;

            }

            case REVIEW:{
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if(_id > 0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewURL(_id);
                else
                    throw new SQLException("Failed to insert row into" + uri);
                break;
            }

            case VIDEO: {
                long _id = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, values);
                if(_id > 0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewURL(_id);
                else
                    throw new SQLException("Failed to insert row into" + uri);
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

            case REVIEW:
                rowMatched = db.delete(MoviesContract.ReviewEntry.TABLE_NAME,selection, selectionArgs);
                break;
            case VIDEO:
                rowMatched = db.delete(MoviesContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
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
                    rowMatched = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values,
                            selection, selectionArgs);
                    break;
                }
                case REVIEW: {
                    rowMatched = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values,
                            selection, selectionArgs);
                    break;
                }
                case VIDEO: {
                    rowMatched = db.update(MoviesContract.VideoEntry.TABLE_NAME, values,
                            selection, selectionArgs);
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
            case REVIEW:
                db.beginTransaction();
                int returnCountR = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCountR++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountR;
            case VIDEO:
                db.beginTransaction();
                int returnCountV = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCountV++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountV;
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
