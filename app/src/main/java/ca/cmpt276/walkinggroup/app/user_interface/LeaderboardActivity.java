package ca.cmpt276.walkinggroup.app.user_interface;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.GroupAdapter;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.UserPointsAdapter;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that shows the leaderboard of users ranked by points.
 */
public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";

    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;
    private ListView userListView;
    private List<User> users;

    private class PointsComparator implements Comparator<User> {
        @Override
        public int compare(User first, User second) {
            return first.getTotalPointsEarned().compareTo(second.getTotalPointsEarned());
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        progressBar = findViewById(R.id.leaderboard_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        userListView = findViewById(R.id.leaderboard_user_list_view);
        userListView.setVisibility(View.INVISIBLE);

        fetchUsers();
    }

    private void fetchUsers() {
        Log.d(TAG, "Fetching users");
        Call<List<User>> usersCaller = proxy.getUsers();
        ProxyBuilder.callProxy(this, usersCaller, allUsers -> {
            users = allUsers;
            sortUsers();
            displayUsers();
        });
    }

    private void sortUsers() {
        Log.d(TAG, "Sorting users");

        for (User user : users) {
            if (user.getTotalPointsEarned() == null) {
                user.setTotalPointsEarned(0);
            }
        }
        Collections.sort(users, new PointsComparator());
        Collections.reverse(users);
    }

    private void displayUsers() {
        Log.d(TAG, "Displaying users");
        UserPointsAdapter adapter = new UserPointsAdapter(this,
                R.layout.content_list_points, users);
        userListView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
        userListView.setVisibility(View.VISIBLE);
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, LeaderboardActivity.class);
    }
}
