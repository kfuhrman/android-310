package com.example.fuhrman.homework1;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fuhrman.homework1.data.StateDatabase;
import com.example.fuhrman.homework1.provider.StateProvider;

/**
 * Created by fuhrman on 4/17/16.
 */
public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);

        Intent intent = getIntent();

        if ( Intent.ACTION_VIEW.equals( intent.getAction() ) ) {
            Intent wordIntent = new Intent(this, StateActivity.class);
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
        } else if ( Intent.ACTION_SEARCH.equals( intent.getAction() ) ) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            this.displayQueryResults(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
            MenuItem menuItem = menu.findItem( R.id.action_search );
            SearchView searchView = ( SearchView ) menuItem.getActionView();

            SearchManager searchManager = ( SearchManager ) getSystemService( Context.SEARCH_SERVICE );
            SearchableInfo info = searchManager.getSearchableInfo( getComponentName() );

            searchView.setSearchableInfo(info);
            searchView.setIconifiedByDefault(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item ) {
        int id = item.getItemId();

        if ( id == R.id.action_search ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                onSearchRequested();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayQueryResults( final String query ) {

        final Cursor cursor =  managedQuery(StateProvider.CONTENT_URI, null, null,
                new String[]{query}, null);

        if (cursor == null) {
            mTextView.setText( getString( R.string.no_results, new Object[]{ query } ) );
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = getResources().getQuantityString( R.plurals.search_results,
                    count, new Object[] { count, query } );
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[] {
                    StateDatabase.STATE_NAME,
                    StateDatabase.CAPITAL_NAME
            };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] {
                    R.id.state_name,
                    R.id.capital_name
            };

            SimpleCursorAdapter states = new SimpleCursorAdapter(this,
                    R.layout.results, cursor, from, to);
            mListView.setAdapter( states );

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent wordIntent = new Intent(getApplicationContext(), StateActivity.class);
                        Uri data = Uri.withAppendedPath(StateProvider.CONTENT_URI,
                                String.valueOf(id));
                        wordIntent.setData(data);
                        startActivity(wordIntent);
                    }
                });
        }
    }

}
