package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.user_interface.map_activities.JoinGroupsMap;

// Class ManageGroupsActivity allows a user to manage their groups.

public class ManageGroupsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);
        setupCreateNewGroupButton();
        setupJoinGroupByMapButton();
        setupLeaveGroupButton();
        setupAddMonitoredToGroup();
        setupListGroupILeadButton();
        setupListGroupIamInButton();

    }


    private void setupCreateNewGroupButton() {
        Button CrtGroupButton = findViewById(R.id.create_group_button);
        CrtGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGroupsActivity.this, CreateNewGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupJoinGroupByMapButton() {
        Button joinByMapButton = findViewById(R.id.join_group_by_map_button);

        joinByMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = JoinGroupsMap.makeIntent(ManageGroupsActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupAddMonitoredToGroup() {
        Button AddMonitoredToGroup = findViewById(R.id.add_monitored_to_group_button);
        AddMonitoredToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGroupsActivity.this, AddMonitoredToGroupActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setupLeaveGroupButton() {
        Button leaveButton = findViewById(R.id.leave_group_button);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGroupsActivity.this,LeaveGroupActivity.class);
                startActivity(intent);

            }
        });
    }

    private void setupListGroupIamInButton(){
        Button setupListGroupsIamInBt = findViewById(R.id.joined_groups_button);
        setupListGroupsIamInBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ShowGroupsIAmInActivity.makeIntent(ManageGroupsActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupListGroupILeadButton() {
        Button setuplistgroupButton = findViewById(R.id.my_group_button);

        setuplistgroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ManageLeadsGroupActivity.makeIntent(ManageGroupsActivity.this);
                intent.putExtra("listGroupLead",true);
                startActivity(intent);
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ManageGroupsActivity.class);
    }
}
