package ca.cmpt276.walkinggroup.app.user_interface.group_activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class AddMonitoredToGroupActivity allows users to add their monitored users to a group.

public class AddMonitoredToGroupActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private Session session;
    private String text;
    private TextView Nousermonitoredbyme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_monitored_to_group);

        session = Session.getInstance();
        proxy = session.getSessionProxy();
        Nousermonitoredbyme = findViewById(R.id.empty_monitor_list_label);
        Nousermonitoredbyme.setVisibility(View.INVISIBLE);


        setUpList();
    }

    private void setUpList() {
        Call<List<User>> caller = proxy.getMonitorsUsers(session.getSessionUser().getId());
        ProxyBuilder.callProxy(AddMonitoredToGroupActivity.this, caller,
                returnedUsers-> retrieveMonitorsUsers(returnedUsers));

    }

    private void retrieveMonitorsUsers(List<User> returnedUsers) {
        String name[] = new String[returnedUsers.size()*2];
        String id[] = new String[returnedUsers.size()*2];
        String names[] = new String[returnedUsers.size()];

        for (int i=0; i < returnedUsers.size(); i++) {
                name[i] = returnedUsers.get(i).getName();
                Long ID = returnedUsers.get(i).getId();
                id[i] = Long.toString(ID);
                names[i] = id[i] + "  , " + name[i];
        }
        displayList(names);
    }

    private void displayList(String[]names) {
        ListView listView = findViewById(R.id.lst_Monitered);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, R.layout.textview_layout, names);
        listView.setAdapter(arrayAdapter);
        if(names.length==0){
            Nousermonitoredbyme.setVisibility(View.VISIBLE);
        }
        registerClickGroup(listView);
    }

   private void registerClickGroup(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                text = textView.getText().toString();
                int indexOfSpace = text.indexOf(" ");
                String userId = text.substring(0, indexOfSpace);

                Intent intent = new Intent(AddMonitoredToGroupActivity.this,
                        ChooseGroupForMonitoredActivity.class);
                intent.putExtra("userID", Long.parseLong(userId));
                startActivity(intent);
            }
        });
    }
}
