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
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.MonitorUserAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class ShowAllMembersOfGroupActivity shows all members of a group.

public class ShowAllMembersOfGroupActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private WGServerProxy proxy;
    private TextView noMemberText;
    private ListView listView;
    private List<User> MemberList;
    private String email;
    private String name;
    private User rmuser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_all_member_of_group);

        Session session = Session.getInstance();
        proxy = session.getSessionProxy();

        // Initialize the progress bar to be at spinning state.
        progressBar = findViewById(R.id.progressbar4);
        progressBar.setVisibility(View.VISIBLE);

        noMemberText = findViewById(R.id.no_member_textview);
        noMemberText.setVisibility(View.INVISIBLE);
        listView = findViewById(R.id.member_listview);


        Intent intent = getIntent();
        long id = intent.getLongExtra("groupId", 0);
        boolean fromleader = intent.getBooleanExtra("Asleader", false);

        fetchMeberList(id);

        if (fromleader == false) {

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("listmember", "group!");

                    TextView userEmail = listView.getChildAt(position).findViewById(R.id.user_email);
                    TextView userName = listView.getChildAt(position).findViewById(R.id.user_name);

                    email = userEmail.getText().toString();
                    name = userName.getText().toString();
                    Intent intent;
                    intent = new Intent(ShowAllMembersOfGroupActivity.this, ShowSpecificMemberActivity.class);
                    intent.putExtra("Email", email);
                    intent.putExtra("Name",name);
                    startActivity(intent);
                }

            });

        } else if (fromleader == true) {

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("listmember", "group!");

                    TextView userEmail = listView.getChildAt(position).findViewById(R.id.user_email);
                    TextView userName = listView.getChildAt(position).findViewById(R.id.user_name);
                    name = userName.getText().toString();
                    email = userEmail.getText().toString();
                    Intent intent;
                    intent = new Intent(ShowAllMembersOfGroupActivity.this, ShowSpecificMemberActivity.class);
                    intent.putExtra("Name",name);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                }

            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                    TextView userEmail = listView.getChildAt(position).findViewById(R.id.user_email);
                    email = userEmail.getText().toString();
                    Tododelete(id,email);
                    return true;
                }
            });
        }
    }
    private void Tododelete(long id,String email) {
        Call<User> callers = proxy.getUserByEmail(email);
            ProxyBuilder.callProxy(this, callers, users -> {
                rmuser = users;

        Call<Void> deleteCall = proxy.removeGroupMember(id, rmuser.getId());
        ProxyBuilder.callProxy(ShowAllMembersOfGroupActivity.this, deleteCall, null);
        Toast.makeText(ShowAllMembersOfGroupActivity.this,
                "successfully delete the member from group", Toast.LENGTH_SHORT).show();
        finish();

        });
    }
    private void fetchMeberList(long id) {
        Call<List<User>> caller = proxy.getGroupMembers(id);
        ProxyBuilder.callProxy(this, caller, result -> {

                MemberList = result;
                populateMemberListData();

        });
    }

    private void populateMemberListData () {
        MonitorUserAdapter listAdapter = new MonitorUserAdapter(this,
                R.layout.content_list_monitor_user, MemberList);
        listView.setAdapter(listAdapter);
        progressBar.setVisibility(View.INVISIBLE);

        if (MemberList.size() == 0) {
            noMemberText.setVisibility(View.VISIBLE);
        }
    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, ShowAllMembersOfGroupActivity.class);
    }
}
