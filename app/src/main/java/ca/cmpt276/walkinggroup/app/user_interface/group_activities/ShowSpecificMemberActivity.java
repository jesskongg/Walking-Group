package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.UserAdapter;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ShowSpecificMemberActivity shows information about a specific user.

public class ShowSpecificMemberActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private ProgressBar progressBar;
    private User currentuser;
    private String Name;
    private String email;
    private ListView listview;
    private TextView noMonitorUserText;
    private List<User> UserList;
    private TextView NameText;
    private TextView Email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_member);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();

        Intent intent = getIntent();

        // Initialize the progress bar to be at spinning state.
        progressBar = findViewById(R.id.progressbar4);
        progressBar.setVisibility(View.VISIBLE);



        noMonitorUserText = findViewById(R.id.no_Monitored_textview);
        noMonitorUserText.setVisibility(View.INVISIBLE);

        email = intent.getStringExtra("Email");
        Name = intent.getStringExtra("Name");

        NameText = findViewById(R.id.Name_textview);
        Email = findViewById(R.id.email_textview);
        NameText.setText(Name);
        Email.setText(email);
        listview = findViewById(R.id.Specific_user_listview);

        fetchMonitorList(email);
    }


    private void fetchMonitorList(String email) {
        Call<User> caller = proxy.getUserByEmail(email);
        ProxyBuilder.callProxy(this, caller, currentUser -> {
            Call<List<User>> subcaller = proxy.getMonitoredByUsers(currentUser.getId());
            ProxyBuilder.callProxy(this, subcaller, result -> {
                UserList = result;
                populateUserListData();
            });
        });
    }

    private void populateUserListData() {
        UserAdapter listAdapter = new UserAdapter(this,
                R.layout.content_list_specific_user, UserList);
        listview.setAdapter(listAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (UserList.size() == 0) {
            noMonitorUserText.setVisibility(View.VISIBLE);
        }
    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, ShowSpecificMemberActivity.class);
    }
}

