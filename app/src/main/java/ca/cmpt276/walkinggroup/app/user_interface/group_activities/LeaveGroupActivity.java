package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

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
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class LeaveGroupActivity allows a user to leave a group.

public class LeaveGroupActivity extends AppCompatActivity {
    Long groupIds[];
    private WGServerProxy proxy;
    private Session session;
    private List<Group> groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_group);

        session = Session.getInstance();
        proxy = session.getSessionProxy();
        getJoinedGroupIds();
    }

    private void getJoinedGroupIds() {
        List<Group> group = session.getSessionUser().getMemberOfGroups();
        groupIds = new Long[group.size()];

        for(int i=0;i<group.size();i++) {
            groupIds[i] = group.get(i).getId();
        }

        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(LeaveGroupActivity.this, caller,
                returnGroups-> parseJoinedGroupsInfo(returnGroups));
    }

    private void parseJoinedGroupsInfo(List<Group> returnedGroups) {
        String name[]= new String[groupIds.length*2];
        String id[] = new String[groupIds.length*2];
        String names[]= new String[groupIds.length];
        groupList = new ArrayList<>();
        int count=0;
        
        for (int i = 0; i < returnedGroups.size(); i++) {
            for (int j = 0; j < groupIds.length; j++) {
                if(returnedGroups.get(i).getId().toString().equals(groupIds[j].toString()) ) {
                  name[count] = returnedGroups.get(i).getGroupDescription();
                  groupList.add(returnedGroups.get(i));
                  Long ID = groupIds[j];
                  id[count]=Long.toString(ID);
                  names[count] = id[count] + "  , "+name[count];
                  count++;
                }
            }
        }
        displayJoinedGroups(names);
    }

    private void displayJoinedGroups(String[]names) {
        ListView listView = findViewById(R.id.lst_leaveGroup);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,R.layout.leave_group_textview_layout,names);
        listView.setAdapter(arrayAdapter);
        registerClickGroup(listView);
    }

    private void registerClickGroup(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                String text = textView.getText().toString();
                int IndexOfSpace = text.indexOf(" ");
                String groupId = text.substring(0, IndexOfSpace);
                Long tempGroupId = Long.parseLong(groupId);
                List<Group> sendGroupList = new ArrayList<>();
                //updating session group list
                for(int i=0;i<groupList.size();i++)
                {
                    if(!groupList.get(i).getId().toString().equals(tempGroupId.toString()))
                    {
                        sendGroupList.add(groupList.get(i));
                    }
                }
                session.getSessionUser().setMemberOfGroups(sendGroupList);


                Call<Void> caller = proxy.removeGroupMember(Long.parseLong(groupId),
                                                            session.getSessionUser().getId());
                ProxyBuilder.callProxy(LeaveGroupActivity.this, caller,
                        returnedUser-> notifyUser());
            }

            private void notifyUser() {
                Toast.makeText(LeaveGroupActivity.this, "Request Sent",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
