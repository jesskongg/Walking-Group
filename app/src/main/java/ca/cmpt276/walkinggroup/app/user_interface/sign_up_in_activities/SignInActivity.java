package ca.cmpt276.walkinggroup.app.user_interface.sign_up_in_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Validate;
import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
 * SignInActivity class implements user interface features for signing into an account
 */


public class SignInActivity extends AppCompatActivity {

    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        proxy = ProxyBuilder.getProxy(getString(R.string.cmpt276_server_api_key), null);

        setupSignInButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SignInActivity.class);
    }

    private void setupSignInButton() {
        EditText emailView = findViewById(R.id.sign_in_email_edit);
        EditText passwordView = findViewById(R.id.sign_in_password_edit);
        Button signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailView.getText().toString();
                String password = passwordView.getText().toString();

                if (validateInput(email, password)) {
                    setupSignInServerCall(email, password);
                }
            }
        });
    }

    private void setupSignInServerCall(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

        Call<Void> caller = proxy.login(user);
        ProxyBuilder.callProxy(SignInActivity.this, caller, null);
    }

    private void onReceiveToken(String token) {
        Log.w("SignInTest", "  --> NOW HAVE TOKEN: " + token);
        Toast.makeText(this, "You have just signed in", Toast.LENGTH_SHORT).show();

        Preferences.saveToken(token);
        proxy = ProxyBuilder.getProxy(getString(R.string.cmpt276_server_api_key), token);

        saveEmail();

        Intent intent = MainMenuActivity.makeIntent(SignInActivity.this);
        startActivity(intent);
        finish();
    }

    private void saveEmail() {
        EditText emailView = findViewById(R.id.sign_in_email_edit);
        String email = emailView.getText().toString();
        Preferences.saveEmail(email);
    }

    private boolean validateInput(String email, String password) {
        boolean isValidEmail = Validate.isValidEmailAddress(email);
        boolean emptyField = (email.isEmpty() || password.isEmpty());

        if (emptyField) {
            Toast.makeText(this, "Please fill in all of the information", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = SignUpActivity.makeIntent(SignInActivity.this);
        startActivity(intent);
    }
}
