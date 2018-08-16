package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.MonitorUserAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.group_activities.ManageMonitoredGroupActivity;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that allows the current user to view their monitored or monitoring users.
 */
public class ListMonitorActivity extends AppCompatActivity {
    private static final String TAG = "ListMonitorActivity";
    private static final String USER_TYPE_EXTRA = "UserType";

    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;
    private TextView emptyLabel;
    private ListView monitorListView;
    private int monitorUserType;

    public static final int MONITORED = 0;
    public static final int MONITORING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_monitor);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        monitorUserType = getIntent().getIntExtra(USER_TYPE_EXTRA, MONITORED);

        progressBar = findViewById(R.id.list_monitor_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        emptyLabel = findViewById(R.id.empty_monitor_list_label);
        emptyLabel.setVisibility(View.INVISIBLE);

        monitorListView = findViewById(R.id.monitor_list_view);
        monitorListView.setVisibility(View.INVISIBLE);

        fetchUsers();
    }

    private void fetchUsers() {
        Log.d(TAG, "Fetching users from servers for type: " + Integer.toString(monitorUserType));
        Long currentUserId = session.getSessionUser().getId();

        switch (monitorUserType) {
            case MONITORED:
                Call<List<User>> monitoredCaller = proxy.getMonitorsUsers(currentUserId);
                ProxyBuilder.callProxy(this, monitoredCaller, users -> populateListView(users));
                break;
            case MONITORING:
                Call<List<User>> monitoringCaller = proxy.getMonitoredByUsers(currentUserId);
                ProxyBuilder.callProxy(this, monitoringCaller, users -> populateListView(users));
                break;
            default:
                Log.e(TAG, "Unknown user type: " + Integer.toString(monitorUserType));
        }
    }

    private void populateListView(List<User> users) {
        Log.d(TAG, "Populating list view with users");

        MonitorUserAdapter adapter = new MonitorUserAdapter(this,
                R.layout.content_list_monitor_user, users);
        monitorListView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (users.size() == 0) {
            emptyLabel.setVisibility(View.VISIBLE);
        } else {
            monitorListView.setVisibility(View.VISIBLE);
        }

        if (monitorUserType == MONITORED) {
            monitorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "Registered click at position: " + Integer.toString(position));

                    User selected = users.get(position);
                    String email = selected.getEmail();

                    Intent intent = ManageMonitoredGroupActivity.makeIntent(
                            ListMonitorActivity.this, email);
                    startActivity(intent);
                }
            });
        }
    }

    public static Intent makeIntent(Context context, int userType) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        Intent intent = new Intent(context, ListMonitorActivity.class);
        intent.putExtra(USER_TYPE_EXTRA, userType);
        return intent;
    }
}
