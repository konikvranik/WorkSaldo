package net.suteren.worksaldo.android;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ch.simas.jtoggl.JToggl;

import static net.suteren.worksaldo.android.TogglCachedProvider.API_KEY;

/**
 * Created by hpa on 3.3.16.
 */
public class LoginDialog extends Dialog {

    private MainActivity mainActivity;

    public LoginDialog(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        Button okButton = (Button) findViewById(R.id.okButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        final EditText usernameField = (EditText) findViewById(R.id.username);
        final EditText passwordField = (EditText) findViewById(R.id.password);
        final TextView errorMessage = (TextView) findViewById(R.id.error);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorMessage.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        usernameField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameField.getText().toString();
                final String password = passwordField.getText().toString();
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        String apiToken = null;
                        try {
                            apiToken = new JToggl(username, password).getCurrentUser().getApi_token();
                        } catch (Exception e) {
                            Log.e("MAinActivity", "login failed", e);
                        }
                        return apiToken;
                    }

                    @Override
                    protected void onPostExecute(String apiToken) {
                        super.onPostExecute(apiToken);
                        if (apiToken == null) {
                            errorMessage.setVisibility(View.VISIBLE);
                        } else {
                            errorMessage.setVisibility(View.GONE);
                            mainActivity.getSharedPreferences().edit().putString(API_KEY, apiToken).apply();
                            LoginDialog.this.dismiss();
                        }
                    }
                }.execute();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginDialog.this.dismiss();
            }
        });
    }
}
