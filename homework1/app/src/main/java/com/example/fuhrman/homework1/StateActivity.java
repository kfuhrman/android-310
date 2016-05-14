package com.example.fuhrman.homework1;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.fuhrman.homework1.data.StateDatabase;

/**
 * Created by fuhrman on 5/14/16.
 */
public class StateActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Toolbar toolbar = (Toolbar) findViewById(R.id.state_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Uri uri = getIntent().getData();
        Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();

            TextView capitalName = (TextView) findViewById( R.id.exp_capital_name );
            TextView stateAbbr = (TextView) findViewById( R.id.exp_state_abbr );
            TextView stateFoundingDate = (TextView) findViewById( R.id.exp_state_founding_date );
            TextView stateFunFact = (TextView) findViewById( R.id.exp_state_fun_fact );

            int stateNameIndex = cursor.getColumnIndexOrThrow( StateDatabase.STATE_NAME );
            int capitalNameIndex = cursor.getColumnIndexOrThrow( StateDatabase.CAPITAL_NAME );
            int stateAbbrIndex = cursor.getColumnIndexOrThrow( StateDatabase.STATE_ABBR );
            int stateFoundingDateIndex = cursor.getColumnIndexOrThrow( StateDatabase.STATE_FOUNDING_DATE );
            int stateFunFactIndex = cursor.getColumnIndexOrThrow( StateDatabase.STATE_FUN_FACT );

            capitalName.setText( cursor.getString( capitalNameIndex ) );
            stateAbbr.setText( cursor.getString( stateAbbrIndex ) );
            stateFoundingDate.setText( cursor.getString( stateFoundingDateIndex ) );
            stateFunFact.setText( cursor.getString( stateFunFactIndex ) );
            getSupportActionBar().setTitle( cursor.getString( stateNameIndex ) );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

}
