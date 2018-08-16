package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ChooseGroupForMonitoredActivity allows users to choose which group to add their
// monitored users to.

public class ChooseGroupForMonitoredActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private ListView listView;
    private Long UID;
    private Long groupID;
    private List<Group> groupList;
    private Group group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group_for_monitored);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();
        setUpGroupList();
    }

    private void setUpGroupList() {
        Intent intent =getIntent();
        UID = intent.getLongExtra("userID",0);
        listView = findViewById(R.id.lst_chooseGroup);

        Call<List<Group>> group_caller = proxy.getGroups();
        ProxyBuilder.callProxy(ChooseGroupForMonitoredActivity.this, group_caller,
                returnedUser->response(returnedUser));
    }

    private void response(List<Group> returnedUser) {
        String name[] = new String[returnedUser.size()*2];
        String id[] = new String[returnedUser.size()*2];
        String names[] = new String[returnedUser.size()];

        for (int i=0; i < returnedUser.size(); i++) {
            name[i] = returnedUser.get(i).getGroupDescription();
            Long ID = returnedUser.get(i).getId();
            id[i]=Long.toString(ID);
            names[i] = id[i] + "  , " + name[i];
        }
        displayList(names);
    }

    private void displayList(String[] names) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, R.layout.groups_textview_layout, names);
        listView.setAdapter(arrayAdapter);
        registerClickGroup();
    }

    private void registerClickGroup() {
        listView = findViewById(R.id.lst_chooseGroup);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                String text = textView.getText().toString();
                int indexOfSpace = text.indexOf(" ");
                String groupId = text.substring(0, indexOfSpace);
                groupID = Long.parseLong(groupId);

                Call<Group> caller_group = proxy.getGroupById(groupID);
                ProxyBuilder.callProxy(ChooseGroupForMonitoredActivity.this, caller_group,
                        returnedGroup2->response2(returnedGroup2));

            }
            private void response2(Group returnedGroup2) {
                //group= new Group();
                group = returnedGroup2;
                groupList = new ArrayList<>();
                groupList.add(returnedGroup2);
                Call<User> caller_user = proxy.getUserById(UID);
                ProxyBuilder.callProxy(ChooseGroupForMonitoredActivity.this, caller_user,
                        returnedUser3->response3(returnedUser3));
            }

            private void response3(User returnedUser3) {
                //group.setMembers(returnedUser3);
                returnedUser3.setMemberOfGroups(groupList);
                Call<List<User>> callerLast = proxy.addGroupMember(groupID, returnedUser3);
                ProxyBuilder.callProxy(ChooseGroupForMonitoredActivity.this, callerLast, returnedUser4->response4(returnedUser4));
            }

            private void response4(List<User> returnedUser4) {
              Toast.makeText(ChooseGroupForMonitoredActivity.this,
                        "Request Sent", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}