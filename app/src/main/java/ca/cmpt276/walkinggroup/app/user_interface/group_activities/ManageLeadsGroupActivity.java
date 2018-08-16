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

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.GroupAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ManageGroupsActivity allows a user to manage the groups that they lead.

public class ManageLeadsGroupActivity extends AppCompatActivity {
    User rmUser;
    User Leader;
    private ProgressBar progressBar;
    private WGServerProxy proxy;
    private List<Group> groupList;
    private TextView noLeaderGroupText;
    private ListView listView;
    private Group clickedGroup;
    private long groupid;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_leads_group);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();


        listView = findViewById(R.id.LeadGroup_listview);

        String currentEmail = Preferences.getEmail();

        Call<User> caller = proxy.getUserByEmail(currentEmail);
        ProxyBuilder.callProxy(this, caller, user -> {
            Leader = user;

            Call<List<Group>> getGroupsCallers = proxy.getGroups();
            ProxyBuilder.callProxy(this, getGroupsCallers, groupsLists -> {
                List<Group> LeadsGroups = Leader.getLeadsGroups();

                for (int i = 0; i < LeadsGroups.size(); ++i) {
                    final int final_i = i;
                    Group g = groupsLists.stream()
                            .filter(x -> x.getId().equals(LeadsGroups.get(final_i).getId()))
                            .findFirst()
                            .orElse(null);
                    LeadsGroups.set(final_i, g);
                }

                Leader.setLeadsGroups(LeadsGroups);
                populateGroupLeadsListData();
            });

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("listgroupsss", "group!");

                clickedGroup = Leader.getLeadsGroups().get(position);
                groupid = clickedGroup.getId();
                    Intent intent;
                    intent = new Intent(ManageLeadsGroupActivity.this,ShowAllMembersOfGroupActivity.class);
                    intent.putExtra("groupId",groupid);
                    intent.putExtra("Asleader",true);
                    startActivity(intent);


            }
        });



        progressBar = findViewById(R.id.progressbar4);
        progressBar.setVisibility(View.VISIBLE);

        noLeaderGroupText = findViewById(R.id.no_leader_of_Group_textview);
        noLeaderGroupText.setVisibility(View.INVISIBLE);


    }

        private void populateGroupListData () {

            List<Group> MemberGroup = rmUser.getMemberOfGroups();
            GroupAdapter groupAdapter = new GroupAdapter(this,
                    R.layout.content_list_group, MemberGroup);

            listView.setAdapter(groupAdapter);

            progressBar.setVisibility(View.INVISIBLE);

            if (MemberGroup.size() == 0) {
                noLeaderGroupText.setVisibility(View.VISIBLE);
            }

        }
    private void populateGroupLeadsListData () {

        List<Group> LeadsGroup = Leader.getLeadsGroups();
        GroupAdapter groupAdapter = new GroupAdapter(this,
                R.layout.content_list_group, LeadsGroup);

        listView.setAdapter(groupAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (LeadsGroup.size() == 0) {
            noLeaderGroupText.setVisibility(View.VISIBLE);
        }

    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, ManageLeadsGroupActivity.class);
    }
    }

