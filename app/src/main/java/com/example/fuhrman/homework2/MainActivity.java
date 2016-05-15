package com.example.fuhrman.homework2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();
        mShareDialog = new ShareDialog(this);
        mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(MainActivity.this, "Successfully posted!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Post cancelled!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "Error posting!, " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText titleEditText = (EditText) findViewById(R.id.post_title);
                EditText descriptionEditText = (EditText) findViewById(R.id.post_description);
                EditText urlEditText = (EditText) findViewById(R.id.post_url);

                String title = titleEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String url = urlEditText.getText().toString().trim();
                if ( title.isEmpty() || description.isEmpty() || url.isEmpty() ) {
                    if ( title.isEmpty() ) {
                        titleEditText.setError("Title of url is requried!");
                    }
                    if ( description.isEmpty() )  {
                        descriptionEditText.setError("Description of url is required!");
                    }
                    if ( url.isEmpty() ) {
                        urlEditText.setError("Url to share is required!");
                    }
                    return;
                }
                if( !Patterns.WEB_URL.matcher( url ).matches() ) {
                    Toast.makeText( MainActivity.this, "Invalid url! Please fix and try again.", Toast.LENGTH_LONG ).show();
                    return;
                }

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle( title )
                            .setContentDescription( description )
                            .setContentUrl( Uri.parse( url ) )
                            .build();

                    mShareDialog.show(linkContent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
