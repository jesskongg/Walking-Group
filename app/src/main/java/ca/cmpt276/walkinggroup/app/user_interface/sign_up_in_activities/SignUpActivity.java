package ca.cmpt276.walkinggroup.app.user_interface.sign_up_in_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
 * SignUpActivity class implements user interface features for the activity
 * 1. Checks user input is valid
 * 2. Stores input data in user object
 * 3. Sends new user data to server
 * 4. Checks whether user is already signed in
 */

public class SignUpActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    public static Context contextOfApplication;

    private String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        contextOfApplication = getApplicationContext();

        proxy = ProxyBuilder.getProxy(getString(R.string.cmpt276_server_api_key), null);

        if(Preferences.isTokenSaved()){
            launchActivity();
        }

        setupSignUpButton();
        setupSignInLink();
    }

    private void setupSignInLink() {
        TextView signInView = findViewById(R.id.sign_in_prompt_text);
        signInView.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable signInTextSpan = (Spannable) signInView.getText();
        final int SPAN_START = signInView.getText().toString().indexOf("SIGN");

        ClickableSpan clickSignInSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = SignInActivity.makeIntent(SignUpActivity.this);
                startActivity(intent);
                finish();
            }
        };

        signInTextSpan.setSpan(clickSignInSpan, SPAN_START, signInTextSpan.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private boolean validateInput(String firstName, String lastName, String email, String password, String passwordConfirm) {
        boolean isValidEmail = Validate.isValidEmailAddress(email);
        boolean isValidFirstName = Validate.isValidFirstName(firstName);
        boolean isValidLastName = Validate.isValidLastName(lastName);
        boolean emptyField = (
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()
        );

        if (emptyField) {
            Toast.makeText(this, "Please fill in all of the information", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidFirstName || !isValidLastName) {
            Toast.makeText(this, "Names can only contain letters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "The password does not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void setupSignUpButton() {
        EditText firstNameView = findViewById(R.id.sign_up_first_name_edit);
        EditText lastNameView = findViewById(R.id.sign_up_last_name_edit);
        EditText emailView = findViewById(R.id.sign_up_email_edit);
        EditText passwordView = findViewById(R.id.sign_up_password_edit);
        EditText passwordConfirmView = findViewById(R.id.sign_up_password_confirm_edit);

        Button signUpBtn = findViewById(R.id.sign_up_button);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = firstNameView.getText().toString();
                String lastName = lastNameView.getText().toString();
                String email = emailView.getText().toString();
                String password = passwordView.getText().toString();
                String passwordConfirm = passwordConfirmView.getText().toString();
                String fullName = firstName + " " + lastName;

                if (validateInput(firstName, lastName, email, password, passwordConfirm)) {
                    User user = new User();
                    user.setName(fullName);
                    user.setEmail(email);
                    user.setPassword(password);

                    Call<User> caller = proxy.createUser(user);
                    ProxyBuilder.callProxy(SignUpActivity.this, caller,
                            x -> saveTokenAndEmail(user));
                }
            }
        });
    }

    private void saveTokenAndEmail(User user) {
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token, user));
        Call<Void> caller = proxy.login(user);
        ProxyBuilder.callProxy(SignUpActivity.this, caller, null);
    }

    private void onReceiveToken(String token, User user) {
        Preferences.saveToken(token);
        Log.d(TAG, "saveToken: token saved");
        Preferences.saveEmail(user.getEmail());
        launchActivity();
    }

    private void launchActivity() {
        Log.d(TAG, "launchActivity: main activity launched");
        startActivity(MainMenuActivity.makeIntent(SignUpActivity.this));
        finish();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }
}
