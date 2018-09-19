package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.Validate;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that allows the current user to change their monitored and monitoring users.
 */
public class ChangeMonitorActivity extends AppCompatActivity {
    private static final String TAG = "ChangeMonitorActivity";

    private WGServerProxy proxy;
    private Session session;
    private enum Action {
        ADD_MONITORED, REMOVE_MONITORED,
        ADD_MONITORING, REMOVE_MONITORING
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_monitor);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        setupButton(Action.ADD_MONITORED);
        setupButton(Action.ADD_MONITORING);

        setupButton(Action.REMOVE_MONITORED);
        setupButton(Action.REMOVE_MONITORING);
    }

    private void setupButton(Action action) {
        Log.d(TAG, "Setting up button for action: " + action.toString());

        EditText emailEditText;
        Button button;

        switch (action) {
            case ADD_MONITORED:
                emailEditText = findViewById(R.id.add_monitored_email_edit_text);
                button = findViewById(R.id.add_monitored_button);
                break;

            case ADD_MONITORING:
                emailEditText = findViewById(R.id.add_monitoring_email_edit_text);
                button = findViewById(R.id.add_monitoring_button);
                break;

            case REMOVE_MONITORED:
                emailEditText = findViewById(R.id.remove_monitored_email_edit_text);
                button = findViewById(R.id.remove_monitored_button);
                break;

            case REMOVE_MONITORING:
                emailEditText = findViewById(R.id.remove_monitoring_email_edit_text);
                button = findViewById(R.id.remove_monitoring_button);
                break;

            default:
                Log.e(TAG, "Invalid action: " + action.toString());
                return;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();

                if (email.length() == 0) {
                    Toast.makeText(ChangeMonitorActivity.this,
                            "Please enter an email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Validate.isValidEmailAddress(email)) {
                    Toast.makeText(ChangeMonitorActivity.this,
                            "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                User currentUser = session.getSessionUser();
                Call<User> caller = proxy.getUserByEmail(email);
                ProxyBuilder.callProxy(ChangeMonitorActivity.this, caller,
                        otherUser -> handleAction(action, currentUser, otherUser));

            }
        });
    }

    private void handleAction(Action action, User currentUser, User otherUser) {
        Log.d(TAG, "Handling action: " + action.toString());

        Call<List<User>> addCaller;
        Call<Void> removeCaller;

        switch (action) {
            case ADD_MONITORED:
                addCaller = proxy.addToMonitorsUsers(currentUser.getId(), otherUser);
                ProxyBuilder.callProxy(ChangeMonitorActivity.this, addCaller,
                        x -> showResult("Request Sent"));
                break;

            case ADD_MONITORING:
                addCaller = proxy.addToMonitoredByUsers(currentUser.getId(), otherUser);
                ProxyBuilder.callProxy(ChangeMonitorActivity.this, addCaller,
                        x -> showResult("Request Sent"));
                break;

            case REMOVE_MONITORED:
                removeCaller = proxy.removeFromMonitorsUsers(currentUser.getId(), otherUser.getId());
                ProxyBuilder.callProxy(ChangeMonitorActivity.this, removeCaller,
                        x -> showResult("Request Sent"));
                break;

            case REMOVE_MONITORING:
                removeCaller = proxy.removeFromMonitoredByUsers(currentUser.getId(), otherUser.getId());
                ProxyBuilder.callProxy(ChangeMonitorActivity.this, removeCaller,
                        x -> showResult("Request Sent"));
                break;

            default:
                Log.e(TAG, "Invalid action: " + action.toString());
        }
    }

    private void showResult(String message) {
        Log.d(TAG, "Showing result: " + message);
        Toast.makeText(ChangeMonitorActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, ChangeMonitorActivity.class);
    }
}
