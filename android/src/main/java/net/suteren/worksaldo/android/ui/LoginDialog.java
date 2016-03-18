package net.suteren.worksaldo.android.ui;

import android.app.Dialog;
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
import net.suteren.worksaldo.android.R;

import javax.ws.rs.client.Client;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.API_KEY;

/**
 * Dialog which allow user to login, obtain api_token and stores it to shared preferences.
 */
public class LoginDialog extends Dialog {

    private MainActivity mainActivity;
    private AsyncTask<String, Void, String> togglLogin;

    /**
     * Constructs login dialog.
     *
     * @param mainActivity Main activity which nests login dialog.
     */
    public LoginDialog(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        final Button okButton = (Button) findViewById(R.id.okButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelButton);
        final EditText usernameField = (EditText) findViewById(R.id.username);
        final EditText passwordField = (EditText) findViewById(R.id.password);
        final TextView errorMessage = (TextView) findViewById(R.id.error);

        setTitle(R.string.login);

        // This watcher hides error message when username or password changes.
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
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
                okButton.setEnabled(false);

                final String username = usernameField.getText().toString();
                final String password = passwordField.getText().toString();
                if (togglLogin != null && AsyncTask.Status.RUNNING == togglLogin.getStatus()) {
                    togglLogin.cancel(true);
                }
                togglLogin = createLoginTask(okButton, usernameField, passwordField, errorMessage);
                togglLogin.execute(username, password);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okButton.isEnabled()) {
                    LoginDialog.this.dismiss();
                } else {
                    togglLogin.cancel(true);
                }
            }
        });
    }

    private AsyncTask<String, Void, String> createLoginTask(final Button okButton, final EditText usernameField, final EditText passwordField, final TextView errorMessage) {
        return new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                String apiToken = null;

                try {
                    apiToken = new JToggl(params[0], params[1]) {
                        @Override
                        protected Client prepareApiClient() {
                            return super.prepareApiClient().register(AndroidFriendlyFeature.class);
                        }
                    }.getCurrentUser().getApiToken();
                } catch (Exception e) {
                    Log.e("LoginDialog", "login failed", e);
                }
                return apiToken;
            }

            @Override
            protected void onCancelled(String s) {
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                okButton.setEnabled(true);
                super.onCancelled(s);
            }

            @Override
            protected void onPostExecute(String apiToken) {

                super.onPostExecute(apiToken);

                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                okButton.setEnabled(true);

                if (apiToken == null) {
                    errorMessage.setVisibility(View.VISIBLE);
                } else {
                    errorMessage.setVisibility(View.GONE);
                    mainActivity.getSharedPreferences().edit().putString(API_KEY, apiToken).apply();
                    LoginDialog.this.dismiss();
                }
            }
        };
    }
}
