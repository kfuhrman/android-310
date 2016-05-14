package com.example.fuhrman.homework1.data;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.example.fuhrman.homework1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by fuhrman on 4/24/16.
 */
public class StateDatabase {

    private static final String TAG = "StatesDatabase";

    public static final String STATE_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String CAPITAL_NAME = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String STATE_ABBR = "STATE_ABBR";
    public static final String STATE_FOUNDING_DATE = "STATE_FOUNDING_DATE";
    public static final String STATE_FUN_FACT = "STATE_FUN_FACT";

    private static final String DATABASE_NAME = "state_database";
    private static final String FTS_VIRTUAL_TABLE = "FTSstate_table";
    private static final int DATABASE_VERSION = 2;

    private final StateDatabaseOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public StateDatabase( final Context context ) {
        mDatabaseOpenHelper = new StateDatabaseOpenHelper( context );
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put( STATE_NAME, STATE_NAME );
        map.put( CAPITAL_NAME, CAPITAL_NAME );
        map.put( STATE_ABBR, STATE_ABBR );
        map.put( STATE_FOUNDING_DATE, STATE_FOUNDING_DATE );
        map.put( STATE_FUN_FACT, STATE_FUN_FACT );
        map.put( BaseColumns._ID, "rowid AS " +
                BaseColumns._ID );
        map.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );
        map.put( SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID );
        return map;
    }

    /**
     * Returns a Cursor positioned at the state specified by rowId
     *
     * @param rowId id of state to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching state, or null if not found.
     */
    public Cursor getState( final String rowId, final String[] columns ) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);
    }

    /**
     * Returns a Cursor over all states that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all states that match, or null if none found.
     */
    public Cursor getStateMatches( final String query, final String[] columns ) {
        String selection = STATE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query( selection, selectionArgs, columns );
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query( final String selection, final  String[] selectionArgs, final String[] columns ) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables( FTS_VIRTUAL_TABLE );
        builder.setProjectionMap( mColumnMap );

        Cursor cursor = builder.query( mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if ( !cursor.moveToFirst() ) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static class StateDatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        STATE_NAME + ", " +
                        CAPITAL_NAME + ", " +
                        STATE_ABBR + ", " +
                        STATE_FOUNDING_DATE + ", " +
                        STATE_FUN_FACT + ");";

        StateDatabaseOpenHelper(Context context) {
            super( context, DATABASE_NAME, null, DATABASE_VERSION );
            mHelperContext = context;
        }

        @Override
        public void onCreate( final SQLiteDatabase db ) {
            mDatabase = db;
            mDatabase.execSQL( FTS_TABLE_CREATE );
            loadStateDatabase();
        }

        /**
         * Starts a thread to load the database table with states
         */
        private void loadStateDatabase() {
            new Thread( new Runnable() {
                public void run() {
                    try {
                        loadStates();
                    } catch ( final IOException e ) {
                        throw new RuntimeException( e );
                    }
                }
            }).start();
        }

        private void loadStates() throws IOException {
            Log.d( TAG, "Loading states..." );
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource( R.raw.data );
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            try {
                String line;
                while ( (line = reader.readLine() ) != null) {
                    String[] strings = TextUtils.split( line, "-" );
                    if ( strings.length < 5 ) continue;
                    long id = addState(strings[0].trim(), strings[3].trim(), strings[1].trim(), strings[2].trim(), strings[4].trim() );
                    if ( id < 0 ) {
                        Log.e( TAG, "unable to add state: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(TAG, "DONE loading states.");
        }

        /**
         * Add a state to the dictionary.
         * @return rowId or -1 if failed
         */
        public long addState( final String stateName, final String capitalName, final String stateAbbr, final String stateFoundingDate, final String stateFunFact ) {
            ContentValues initialValues = new ContentValues();
            initialValues.put( STATE_NAME, stateName );
            initialValues.put( CAPITAL_NAME, capitalName );
            initialValues.put( STATE_ABBR, stateAbbr );
            initialValues.put( STATE_FOUNDING_DATE, stateFoundingDate );
            initialValues.put( STATE_FUN_FACT, stateFunFact );

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL( "DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE );
            onCreate(db);
        }

    }

}
