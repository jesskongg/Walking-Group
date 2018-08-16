package ca.cmpt276.walkinggroup.app.user_interface.monitor_activities;

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
import ca.cmpt276.walkinggroup.app.user_interface.edit_info_activities.EditMonitoredActivity;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class DisplayMonitoredToEditActivity allows the selection of a monitored user to be edited.

public class DisplayMonitoredToEditActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private Session session;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_monitored_to_edit);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        retrieveMonitorsUsers();
    }

    private void retrieveMonitorsUsers() {
        listView = findViewById(R.id.lst_editMonitored);

        Call<List<User>> caller = proxy.getMonitorsUsers(session.getSessionUser().getId());
        ProxyBuilder.callProxy(DisplayMonitoredToEditActivity.this, caller,
                returnedUsers-> extractUserNamesAndIds(returnedUsers));
    }

    private void extractUserNamesAndIds(List<User> returnedUsers) {
        String name[] = new String[returnedUsers.size()*2];
        String id[] = new String[returnedUsers.size()*2];
        String namesAndIds[] = new String[returnedUsers.size()];

        for (int i = 0; i < returnedUsers.size(); i++) {
            name[i] = returnedUsers.get(i).getName();
            Long Id = returnedUsers.get(i).getId();
            id[i] = Long.toString(Id);
            namesAndIds[i] = id[i] + "  , " + name[i];
        }
        displayList(namesAndIds);
    }

    private void displayList(String[] names) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, R.layout.textview_layout, names);
        listView.setAdapter(arrayAdapter);
        registerClickUser();
    }

    private void registerClickUser() {
        listView = findViewById(R.id.lst_editMonitored);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                String text = textView.getText().toString();
                int indexOfSpace = text.indexOf(" ");

                String userId = text.substring(0, indexOfSpace);

                Intent intent = new Intent(DisplayMonitoredToEditActivity.this,
                        EditMonitoredActivity.class);
                intent.putExtra("userID", Long.parseLong(userId));
                startActivity(intent);
                finish();
            }
        });
    }
}
