package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.map_activities.DashboardMapActivity;
import ca.cmpt276.walkinggroup.app.user_interface.message_activities.InboxActivity;
import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that provides a dashboard for the current user.
 */
public class DashboardActivity extends AppCompatActivity {
    public static final String TAG = "DashboardActivity";
    public static final int REQUEST_CODE_INBOX_ACTIVITY = 1000;

    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;
    private Button dashboardMapButton;
    private Button inboxButton;
    private static final int TIMER_DELAY = 0;
    private static final int TIMER_INTERVAL = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        progressBar = findViewById(R.id.dashboard_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        inboxButton = findViewById(R.id.inbox_button);
        inboxButton.setVisibility(View.INVISIBLE);

        dashboardMapButton = findViewById(R.id.dashboard_map_button);
        dashboardMapButton.setVisibility(View.INVISIBLE);

        TimerTask updateMessageTask = new TimerTask() {
            @Override
            public void run() {
                fetchUnreadMessages();
            }
        };
        Timer retrieveMessageTimer = new Timer();
        retrieveMessageTimer.scheduleAtFixedRate(updateMessageTask, TIMER_DELAY, TIMER_INTERVAL);

        setupInboxButton();
        setupDashboardMapButton();
    }

    private void fetchUnreadMessages() {
        Log.d(TAG, "Fetching unread messages");

        Long currentUserId = session.getSessionUser().getId();
        Call<List<Message>> messageCaller = proxy.getUnreadMessages(currentUserId, null);
        ProxyBuilder.callProxy(this, messageCaller, messages -> {
            displayUnreadMessageCount(messages.size());
        });

    }

    private void displayUnreadMessageCount(int count) {
        Log.d(TAG, "Displaying unread message count");

        TextView newMessageView = findViewById(R.id.unread_message_count_text);
        newMessageView.setTextSize(16f);
        newMessageView.setText("Unread Messages: " + Integer.toString(count));

        inboxButton.setVisibility(View.VISIBLE);
        dashboardMapButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setupDashboardMapButton() {
        dashboardMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = DashboardMapActivity.makeIntent(DashboardActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupInboxButton() {
        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InboxActivity.makeIntent(DashboardActivity.this);
                startActivityForResult(intent, REQUEST_CODE_INBOX_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"Received activity result");
        fetchUnreadMessages();
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, DashboardActivity.class);
    }
}
