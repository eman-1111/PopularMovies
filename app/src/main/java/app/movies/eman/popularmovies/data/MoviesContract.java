package app.movies.eman.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by user on 31/07/2015.
 */
public class MoviesContract {


    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "app.movies.eman.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";


    /* Inner class that defines the table contents of the location table */
    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movies";


        public static final String COLUMN_IMAGE_PATH = "image_path";


        public static final String COLUMN_MOVIE_NAME = "move_name";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_FAVORITE = "favorite_movie";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieURL(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }
        public static Uri buildMoviesURL() {
            return CONTENT_URI.buildUpon().build();
        }
        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1)) ;
        }
    }




    public static  final class VideoEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String TABLE_NAME = "video";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_VIDEO_ID = "video_id";
        public static final String COLUMN_ADDRESS = "key";
        public static final String COLUMN_MOVIE_NAME = "name";

        public static final Uri buildVideoURL (long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildVideoURL(int movieId){
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }
        public static int getVideoIdFromURL(Uri uri){
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }



    public static final class ReviewEntry implements  BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_REVIEW;


        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_REVIEW = "content";

        public static final Uri buildReviewURL (long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewURL(int movieId){
            return CONTENT_URI.buildUpon().appendPath((Integer.toString(movieId))).build();
        }

        public static int getReviewIdFromURL(Uri uri){
            return Integer.parseInt(uri.getPathSegments().get(1));

        }


    }
}
