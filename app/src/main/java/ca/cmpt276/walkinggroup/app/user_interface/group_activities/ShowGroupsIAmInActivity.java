package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.GroupAdapter;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ShowGroupsIAmInActivity shows all groups that the current user is a member of.

public class ShowGroupsIAmInActivity extends AppCompatActivity {
    User user;

    private ProgressBar progressBar;
    private WGServerProxy proxy;
    private Session session;
    private List<Group> groupList;
    private TextView noMemberGroupText;
    private ListView listView;
    private Group clickedGroup;
    private long groupid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list__all__group__i__am__in);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        listView = findViewById(R.id.MemberGroup_listview);

        user = session.getSessionUser();

        Call<List<Group>> getGroupsCallers = proxy.getGroups();
        ProxyBuilder.callProxy(this, getGroupsCallers, groupsLists -> {
            List<Group> MemberGroups = user.getMemberOfGroups();

            for (int i = 0; i < MemberGroups.size(); ++i) {
                final int final_i = i;
                Group g = groupsLists.stream()
                        .filter(x -> x.getId().equals(MemberGroups.get(final_i).getId()))
                        .findFirst()
                        .orElse(null);
                MemberGroups.set(final_i, g);
            }

            user.setMemberOfGroups(MemberGroups);
            populateGroupListData();
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                clickedGroup = user.getMemberOfGroups().get(position);
                groupid = clickedGroup.getId();

                Intent intent;
                intent = new Intent(ShowGroupsIAmInActivity.this, ShowAllMembersOfGroupActivity.class);
                intent.putExtra("groupId", groupid);
                startActivity(intent);


            }
        });
        progressBar = findViewById(R.id.progressbar4);
        progressBar.setVisibility(View.VISIBLE);

        noMemberGroupText = findViewById(R.id.no_member_of_Group_textview);
        noMemberGroupText.setVisibility(View.INVISIBLE);
    }

    private void populateGroupListData () {

        List<Group> MemberGroup = user.getMemberOfGroups();
        GroupAdapter groupAdapter = new GroupAdapter(this,
                R.layout.content_list_group, MemberGroup);

        listView.setAdapter(groupAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (MemberGroup.size() == 0) {
            noMemberGroupText.setVisibility(View.VISIBLE);
        }

    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, ShowGroupsIAmInActivity.class);
    }
}

