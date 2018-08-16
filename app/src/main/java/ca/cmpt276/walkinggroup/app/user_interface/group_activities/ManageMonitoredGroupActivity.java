package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.GroupAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.ListMonitorActivity;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ManageMonitoredGroupActivity allows users to manage the groups of their monitored users.

public class ManageMonitoredGroupActivity extends AppCompatActivity {
    private static final String TAG = "ManageMonitoredGroupActivity";
    private static final String EMAIL_EXTRA = "email";

    User affectedUser;
    private ProgressBar progressBar;
    private WGServerProxy proxy;
    private TextView noMonitorgroupText;
    private ListView listView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_monitored_group);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();

        // Initialize the progress bar to be at spinning state.
        progressBar = findViewById(R.id.progressbar3);
        progressBar.setVisibility(View.VISIBLE);

        noMonitorgroupText = findViewById(R.id.no_group_textview);
        noMonitorgroupText.setVisibility(View.INVISIBLE);

        listView = findViewById(R.id.group_listview);

        Intent intent = getIntent();
        String email = intent.getStringExtra(EMAIL_EXTRA);

        Call<User> caller = proxy.getUserByEmail(email);
        ProxyBuilder.callProxy(this, caller, user -> {
            affectedUser = user;
            Call<List<Group>> getGroupsCaller = proxy.getGroups();
            ProxyBuilder.callProxy(this, getGroupsCaller, groupsList -> {
                List<Group> Groups = affectedUser.getMemberOfGroups();

                for (int i = 0; i < Groups.size(); ++i) {
                    final int final_i = i;
                    Group g = groupsList.stream()
                            .filter(x -> x.getId().equals(Groups.get(final_i).getId()))
                            .findFirst()
                            .orElse(null);
                    Groups.set(final_i, g);
                }

                affectedUser.setMemberOfGroups(Groups);
                populateUserListData();
            });

        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("listgroup", "group!");
                Group clickedGroup = affectedUser.getMemberOfGroups().get(position);
                Call<Void> removeCall = proxy.removeGroupMember(clickedGroup.getId(), affectedUser.getId());
                ProxyBuilder.callProxy(ManageMonitoredGroupActivity.this, removeCall, null);
                Toast.makeText(ManageMonitoredGroupActivity.this,
                        "successfully delete the member from group", Toast.LENGTH_SHORT).show();
                return true;

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Group clickedGroup = affectedUser.getMemberOfGroups().get(position);
                long groupid = clickedGroup.getId();

                Intent intent;
                intent = new Intent(ManageMonitoredGroupActivity.this,ShowAllMembersOfGroupActivity.class);
                intent.putExtra("groupId",groupid);
                startActivity(intent);
            }
        });

    }

    private void populateUserListData() {
        List<Group> MemberGroup = affectedUser.getMemberOfGroups();
        GroupAdapter groupAdapter = new GroupAdapter(this,
                R.layout.content_list_group, MemberGroup);

        listView.setAdapter(groupAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (MemberGroup.size() == 0) {
            noMonitorgroupText.setVisibility(View.VISIBLE);
        }

    }

    public static Intent makeIntent(Context context, String email) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        Intent intent = new Intent(context, ManageMonitoredGroupActivity.class);
        intent.putExtra(EMAIL_EXTRA, email);
        return intent;
    }
}