package ca.cmpt276.walkinggroup.app.user_interface.message_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class SendMessageActivity allows the current user to send a message.

public class SendMessageActivity extends AppCompatActivity {

    private static final String TAG = "SendMessageActivity";
    private WGServerProxy proxy;
    private Session session;
    private String selectContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        populateContacts();
        setupDropListClicks();
        setupSendButton();

    }

    private void setupSendButton() {
        Button sendMessageButton = findViewById(R.id.send_message_button);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroupsToSendMessage();

                finish();
            }
        });
    }

    private void getGroupsToSendMessage() {
        User sessionUser = session.getSessionUser();

        for(int i = 0; i < sessionUser.getLeadsGroups().size(); i++){
            if(sessionUser.getLeadsGroups().get(i).getGroupDescription().equals(selectContact)){
                sendMessageToGroup(sessionUser.getLeadsGroups().get(i));
            }
        }

        for(int j = 0; j < sessionUser.getMonitoredByUsers().size(); j++){
            if(sessionUser.getMonitoredByUsers().get(j).getName().equals(selectContact)){
                sendMessageToMonitorsAndGroupLeaders(sessionUser.getId());
            }
        }

    }

    private void sendMessageToMonitorsAndGroupLeaders(Long userId) {
        TextView messageSubjectText = findViewById(R.id.message_subject_text);
        TextView messageBodyText = findViewById(R.id.message_body_text);

        String messageSubject = messageSubjectText.getText().toString();
        String messageBody = messageBodyText.getText().toString();
        String fullMessage = messageSubject + "\n" + messageBody;

        User user = new User();
        user.setEmail(Preferences.getEmail());

        Message message = new Message();
        message.setText(fullMessage);
        message.setFromUser(user);

        Call<List<Message>> caller = proxy.newMessageToParentsOf(userId, message);
        ProxyBuilder.callProxy(this, caller, response -> notifyMessageSent());
    }

    private void sendMessageToGroup(Group group) {
        TextView messageSubjectText = findViewById(R.id.message_subject_text);
        TextView messageBodyText = findViewById(R.id.message_body_text);

        String messageSubject = messageSubjectText.getText().toString();
        String messageBody = messageBodyText.getText().toString();
        String fullMessage = messageSubject + "\n" + messageBody;

        User user = new User();
        user.setEmail(Preferences.getEmail());

        Message message = new Message();
        message.setText(fullMessage);
        message.setFromUser(user);

        Call<List<Message>> caller = proxy.newMessageToGroup(group.getId(), message);
        ProxyBuilder.callProxy(this, caller, response -> notifyMessageSent());
    }

    private void notifyMessageSent() {
        Log.d(TAG, "notifyMessageSent: message has been sent");
        Toast.makeText(this, "Message has been sent", Toast.LENGTH_SHORT).show();
    }

    private void populateContacts() {
        List<Group> leadsGroups = session.getSessionUser().getLeadsGroups();
        List<User> PeopleWhoMonitorUser = session.getSessionUser().getMonitoredByUsers();
        int totalSizeOfList = leadsGroups.size() + PeopleWhoMonitorUser.size();
        Log.d(TAG, "populateContacts: " + "total size: " + totalSizeOfList
        + " number of groups: " + leadsGroups.size() + " number of monitors " + PeopleWhoMonitorUser.size());

        String [] contactsToSendMessagesTo = new String[totalSizeOfList];

        for (int i = 0; i < totalSizeOfList; i++) {
            if(i < leadsGroups.size()) {
                contactsToSendMessagesTo[i] = leadsGroups.get(i).getGroupDescription();
            } else {
                contactsToSendMessagesTo[i] = PeopleWhoMonitorUser.get(i-leadsGroups.size()).getName();
            }
        }

        Spinner sendContactListSpinner = findViewById(R.id.send_to_selection_spinner);
        ArrayAdapter adapter = new ArrayAdapter(this,
                R.layout.send_message_drop_list_layout,contactsToSendMessagesTo);
        sendContactListSpinner.setAdapter(adapter);
    }

    private void setupDropListClicks() {
        Spinner sendContactListSpinner = findViewById(R.id.send_to_selection_spinner);
        sendContactListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView view = (TextView) viewClicked;
                selectContact = view.getText().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public static Intent makeIntent(Context context ) {
        return new Intent(context, SendMessageActivity.class);
    }
}
