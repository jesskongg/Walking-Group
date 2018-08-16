package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.GroupAdapter;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.app.user_interface.map_activities.WalkActivity;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Activity that allows the current user to select a group to walk with.
 */
public class SelectGroupActivity extends AppCompatActivity {
    private static final String TAG = "SelectGroupActivity";

    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;
    private ListView groupListView;
    private List<Group> groups;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        progressBar = findViewById(R.id.select_group_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        groupListView = findViewById(R.id.select_group_list);
        groups = new ArrayList<>();

        updateCurrentUser();
    }

    private void updateCurrentUser() {
        Log.d(TAG, "Updating current user");
        Long userId = session.getSessionUser().getId();
        Call<User> userCaller = proxy.getUserById(userId);
        ProxyBuilder.callProxy(this, userCaller, user -> {
            currentUser = user;
            session.saveSessionUser(currentUser);
            fetchGroups();
        });
    }

    private void fetchGroups() {
        Log.d(TAG, "Fetching all groups");
        Call<List<Group>> groupCaller = proxy.getGroups();
        ProxyBuilder.callProxy(this, groupCaller, allGroups -> filterGroups(allGroups));
    }

    private void filterGroups(List<Group> allGroups) {
        Log.d(TAG, "Filtering groups");

        List<Group> memberOfGroups = currentUser.getMemberOfGroups();
        List<Group> leadsGroups = currentUser.getLeadsGroups();

        for (Group potential : allGroups) {
            for (Group memberOf : memberOfGroups) {
                if (potential.getId().equals(memberOf.getId())) {
                    Log.d(TAG, "Found matching group: " + potential.toString());
                    groups.add(potential);
                    break;
                }
            }

            for (Group leads : leadsGroups) {
                if (potential.getId().equals(leads.getId())) {
                    Log.d(TAG, "Found matching group: " + potential.toString());
                    groups.add(potential);
                    break;
                }
            }
        }

        populateGroupListView();
        setGroupListViewListeners();
    }

    private void populateGroupListView () {
        Log.d(TAG, "Populating group list view");
        GroupAdapter groupAdapter = new GroupAdapter(this,
                R.layout.content_list_group, groups);
        groupListView.setAdapter(groupAdapter);

        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setGroupListViewListeners() {
        Log.d(TAG, "Setting group list view listeners");
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long groupId = groups.get(position).getId();

                Intent intent = WalkActivity.makeIntent(SelectGroupActivity.this);
                intent.putExtra("groupId", groupId);

                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = MainMenuActivity.makeIntent(SelectGroupActivity.this);
        startActivity(intent);
        finish();
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, SelectGroupActivity.class);
    }
}
