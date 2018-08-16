package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.RequestAdapter;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.UserPointsAdapter;
import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that displays the history of permission requests for the current user.
 */
public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;
    private ListView requestListView;
    private List<PermissionRequest> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        progressBar = findViewById(R.id.history_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        requestListView = findViewById(R.id.history_request_list_view);
        requestListView.setVisibility(View.INVISIBLE);

        fetchRequests();
    }

    private void fetchRequests() {
        Log.d(TAG, "Fetching permission requests");
        Long userId = session.getSessionUser().getId();
        Call<List<PermissionRequest>> requestCaller = proxy.getAllPermissionForUser(userId);
        ProxyBuilder.callProxy(this, requestCaller, permissionRequests -> {
            requests = permissionRequests;
            Collections.reverse(requests);
            displayRequests();
        });
    }

    private void displayRequests() {
        Log.d(TAG, "Displaying permission requests");

        RequestAdapter adapter = new RequestAdapter(this,
                R.layout.content_list_request, requests);
        requestListView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
        requestListView.setVisibility(View.VISIBLE);
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, HistoryActivity.class);
    }
}
