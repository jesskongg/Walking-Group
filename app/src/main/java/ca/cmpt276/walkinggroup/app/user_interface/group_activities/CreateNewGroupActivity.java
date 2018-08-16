package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.Validate;
import ca.cmpt276.walkinggroup.app.user_interface.map_activities.CreateGroupMapActivity;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
 * CreateNewGroupActivity class implements the UI for creating a new group
 * It requires user to provide a group description and set walking locations (via map)
 */

public class CreateNewGroupActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private Session session;
    private Group group;

    private final static String TAG = "CreateNewGroupActivity";
    private final static int MAP_RESULT_CODE = 1019;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        group = new Group();
        group.setRouteLatArray(null);
        group.setRouteLngArray(null);


        setupCreateGroupButton();
        setupWalkingLocations();
    }

    private void setupCreateGroupButton() {
        EditText groupDescriptionView = findViewById(R.id.edit_GroupDescription);

        Button create_group_button = findViewById(R.id.btn_Confirm_createGroup);

        create_group_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupDescription = groupDescriptionView.getText().toString();

                if(!groupDescription.equals("") && group.getRouteLatArray() != null && group.getRouteLngArray() != null) {

                    group.setGroupDescription(groupDescription);
                    createGroupOnServer(session.getSessionUser());

                } else if (groupDescription.equals("")) {
                    Toast.makeText(CreateNewGroupActivity.this, "Please add a group description",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "setupCreateGroupButton: missing group description");
                } else {
                    Toast.makeText(CreateNewGroupActivity.this, "Please assign walking " +
                            "locations for the group", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "setupCreateGroupButton: missing group walking locations");
                }
            }

            private void createGroupOnServer(User user) {
                group.setLeader(user);

                Set<User> memberUsers = new HashSet<>();
                memberUsers.add(user);

                group.setMemberUsers(memberUsers);

                Call<Group> caller_group = proxy.createGroup(group);
                ProxyBuilder.callProxy(CreateNewGroupActivity.this, caller_group,
                                        callNothing->doNothing(callNothing));
            }

            private void doNothing(Group callNothing) {
                Toast.makeText(CreateNewGroupActivity.this, "Created group", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupWalkingLocations() {
        Button setupWalkingLocationsButton = findViewById(R.id.set_walking_locations_button);

        setupWalkingLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate.isCorrectGooglePlayVersion(CreateNewGroupActivity.this)) {
                    Intent intent = CreateGroupMapActivity.makeIntent(CreateNewGroupActivity.this);
                    startActivityForResult(intent, MAP_RESULT_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MAP_RESULT_CODE && resultCode == RESULT_OK){
            double[] latArray = data.getDoubleArrayExtra("routeLatitude");
            double[] lngArray = data.getDoubleArrayExtra("routeLongitude");
            group.setRouteLatArray(latArray);
            group.setRouteLngArray(lngArray);
        }
    }
}
