package com.example.fuhrman.homework1.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.fuhrman.homework1.data.StateDatabase;

/**
 * Created by fuhrman on 4/24/16.
 */
public class StateProvider extends ContentProvider {

    String TAG = "StateProvider";

    public static String AUTHORITY = "com.example.fuhrman.homework1.provider.StateProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/states");

    public static final String STATE_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.example.android.homework1";
    public static final String STATE_EXPANDED_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.example.android.homework1";

    private StateDatabase mStateDb;

    private static final int SEARCH_STATES = 0;
    private static final int GET_STATE = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mStateDb = new StateDatabase( getContext() );
        return true;
    }

    @Nullable
    @Override
    public Cursor query( @Nullable final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder ) {

        switch ( sURIMatcher.match( uri ) ) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri );
                }
                return this.getSuggestions( selectionArgs[0] );
            case SEARCH_STATES:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri );
                }
                return this.search( selectionArgs[0] );
            case GET_STATE:
                return this.getState( uri);
            default:
                throw new IllegalArgumentException( "Unknown Uri: " + uri );
        }

    }

    @Nullable
    @Override
    public String getType( @Nullable final Uri uri ) {
        switch ( sURIMatcher.match( uri ) ) {
            case SEARCH_STATES:
                return STATE_MIME_TYPE;
            case GET_STATE:
                return STATE_EXPANDED_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException( "Unknown URL " + uri );
        }
    }

    @Nullable
    @Override
    public Uri insert( @Nullable final Uri uri, final ContentValues values ) {
        throw new UnsupportedOperationException( "Cannot insert states" );
    }

    @Override
    public int delete( @Nullable final Uri uri, final String selection, final String[] selectionArgs ) {
        throw new UnsupportedOperationException( "Cannot delete states" );
    }

    @Override
    public int update( final @NonNull Uri uri, final ContentValues values, final String selection, final String[] selectionArgs ) {
        throw new UnsupportedOperationException( "Cannot update states" );
    }

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher( UriMatcher.NO_MATCH );

        matcher.addURI( AUTHORITY, "states", SEARCH_STATES );
        matcher.addURI( AUTHORITY, "states/#", GET_STATE );

        matcher.addURI( AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST );
        matcher.addURI( AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST );

        return matcher;
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        String[] columns = new String[] {
                BaseColumns._ID,
                StateDatabase.STATE_NAME,
                StateDatabase.CAPITAL_NAME,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };

        return mStateDb.getStateMatches( query, columns );
    }

    private Cursor search( final String query ) {
        String[] columns = new String[] {
                BaseColumns._ID,
                StateDatabase.STATE_NAME,
                StateDatabase.CAPITAL_NAME
        };

        return mStateDb.getStateMatches( query.toLowerCase(), columns );
    }

    private Cursor getState( final Uri uri ) {
        String rowId = uri.getLastPathSegment();
        String[] columns = new String[] {
                StateDatabase.STATE_NAME,
                StateDatabase.CAPITAL_NAME,
                StateDatabase.STATE_ABBR,
                StateDatabase.STATE_FOUNDING_DATE,
                StateDatabase.STATE_FUN_FACT
        };

        return mStateDb.getState( rowId, columns );
    }

}
