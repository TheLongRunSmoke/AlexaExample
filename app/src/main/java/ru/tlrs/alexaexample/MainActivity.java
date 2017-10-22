package ru.tlrs.alexaexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends Activity {

    private RequestContext requestContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestContext = RequestContext.create(this);

        requestContext.registerListener(new AuthorizeListener() {

            /* Authorization was completed successfully. */
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (result.getAccessToken() != null) {
                    String accessToken = result.getAccessToken();
                    Log.d("MainActivity", "onSuccess(): accessToken = " + accessToken);
                }
                fetchUserProfile();
            }

            /* There was an error during the attempt to authorize the
               application. */
            @Override
            public void onError(AuthError ae) {
            /* Inform the user of the error */
            }

            /* Authorization was cancelled before it could be completed. */
            @Override
            public void onCancel(AuthCancellation cancellation) {
            /* Reset the UI to a ready-to-login state */
            }
        });

        // Find the button with the login_with_amazon ID
        // and set up a click handler
        View loginButton = findViewById(R.id.login_with_amazon);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.authorize(new AuthorizeRequest
                        .Builder(requestContext)
                        .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                        .build());
            }
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
                    @Override
                    public void onSuccess(Void response) {
                        // Set logged out state in UI
                    }

                    @Override
                    public void onError(AuthError authError) {
                        // Log the error
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    private void fetchUserProfile() {
        User.fetch(this, new Listener<User, AuthError>() {

            /* fetch completed successfully. */
            @Override
            public void onSuccess(User user) {
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipcode = user.getUserPostalCode();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateProfileData(name, email, account, zipcode);
                    }
                });
            }

            /* There was an error during the attempt to get the profile. */
            @Override
            public void onError(AuthError ae) {
            /* Retry or inform the user of the error */
            }
        });
    }

    private void updateProfileData(String name, String email, String account, String zipcode) {
        TextView result = (TextView) findViewById(R.id.login_result);
        result.setText(String.format(Locale.ENGLISH, "Name: %1s\nEmail: %2s\nAccount: %3s\nZIP: %4s", name, email, account, zipcode));
    }
}
