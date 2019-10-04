package com.aquacontrol.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import static android.app.PendingIntent.getActivity;

public class Settings extends AppCompatActivity {
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        final SharedPreferences sharedpreferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);

        final EditText etBrokerURL = (EditText) findViewById(R.id.brokerURL);
        if (sharedpreferences.contains(Constants.PREF_BROKER_URL)) {
            etBrokerURL.setText(sharedpreferences.getString(Constants.PREF_BROKER_URL, ""));
        }
        final EditText etBrokerUSER = (EditText) findViewById(R.id.brokerUser);
        if (sharedpreferences.contains(Constants.PREF_BROKER_USER)) {
            etBrokerUSER.setText(sharedpreferences.getString(Constants.PREF_BROKER_USER, ""));
        }
        final EditText etBrokerPASS = (EditText) findViewById(R.id.brokerPass);
        if (sharedpreferences.contains(Constants.PREF_BROKER_PASS)) {
            etBrokerPASS.setText(sharedpreferences.getString(Constants.PREF_BROKER_PASS, ""));
        }

        etBrokerURL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Constants.PREF_BROKER_URL, etBrokerURL.getText().toString());
                    editor.commit();
                    handled = true;
                }
                return handled;
            }
        });

        etBrokerUSER.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Constants.PREF_BROKER_USER, etBrokerUSER.getText().toString());
                    editor.commit();
                    handled = true;
                }
                return handled;
            }
        });

        etBrokerPASS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Constants.PREF_BROKER_PASS, etBrokerPASS.getText().toString());
                    editor.commit();
                    handled = true;
                }
                return handled;
            }
        });
    }
}
