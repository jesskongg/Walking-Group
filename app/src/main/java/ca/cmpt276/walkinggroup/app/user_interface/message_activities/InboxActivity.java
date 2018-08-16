package ca.cmpt276.walkinggroup.app.user_interface.message_activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.SelectedMessage;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.model.ViewMessageDialogFragment;
import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class InboxActivity shows the message inbox of the current user.

public class InboxActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private Session session;
    private final String TAG = "InboxActivity";
    private boolean isFilterByUnread = false;
    private static final int TIMER_DELAY = 0;
    private static final int TIMER_INTERVAL = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        TimerTask updateMessageTask = new TimerTask() {
            @Override
            public void run() {
                getServerMessages();
            }
        };

        setupMessageFilterButtons();

        Timer retrieveMessageTimer = new Timer();
        retrieveMessageTimer.scheduleAtFixedRate(updateMessageTask, TIMER_DELAY, TIMER_INTERVAL);

        setupDetectMessageSelected();
        setupSendMessageButton();
    }

    private void setupDetectMessageSelected() {
        ListView messageList = findViewById(R.id.inbox_list_view);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView view = (TextView) viewClicked;

                String messageIdBuild = Preferences.getMessageIds();
                String [] messageIds = messageIdBuild.split(",");

                Log.d(TAG, "setupDetectMessageSelected: you clicked position " + position + " which says "
                        + view.getText() + " with id of " + messageIds[position]);

                SelectedMessage.setFullMessage(position);
                setupMessageDisplayDialog();

                Long selectedMessageId = Long.parseLong(messageIds[position]);
                getMessageStatus(selectedMessageId);
            }
        });
    }

    private void getMessageStatus(Long selectedMessageId) {
        Call<Message> caller = proxy.getOneMessage(selectedMessageId);
        ProxyBuilder.callProxy(this, caller, returnedMessage -> updateMessageStatusOnServer(returnedMessage));
    }

    private void updateMessageStatusOnServer(Message returnedMessage) {
        if(!returnedMessage.isRead()){
            Call<Message> caller = proxy.markMessageAsRead(returnedMessage.getId(), true);
            ProxyBuilder.callProxy(this, caller, doNothing -> getServerMessages());
        }
    }

    private void setupMessageDisplayDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ViewMessageDialogFragment viewMessageDialogFragment = new ViewMessageDialogFragment();
        viewMessageDialogFragment.show(fragmentManager, "Dialog");
        getServerMessages();
    }

    private void setupMessageFilterButtons() {
        Button allMessagesButton = findViewById(R.id.all_messages_button);
        Button unreadMessagesButton = findViewById(R.id.unread_messages_button);

        allMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterByUnread = false;
                getServerMessages();
            }
        });

        unreadMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterByUnread = true;
                getServerMessages();
            }
        });
    }

    public void getServerMessages(){

        Long currentUserId = session.getSessionUser().getId();

        if(isFilterByUnread) {
            Call<List<Message>> caller = proxy.getUnreadMessages(currentUserId, null);
            ProxyBuilder.callProxy(this, caller, returnedMessages -> populateInbox(returnedMessages));
        } else {
            Call<List<Message>> caller = proxy.getMessages(currentUserId, null);
            ProxyBuilder.callProxy(this, caller, returnedMessage -> populateInbox(returnedMessage));
        }

    }

    private List<Message> sortMessagesById(List<Message> returnedMessages) {
        List<Message> sortedMessages = new ArrayList<>();

        while(returnedMessages.size() != 0){
            Message largestIdMessage = returnedMessages.get(returnedMessages.size() - 1);
            int largestIdMessageIndex = returnedMessages.size() - 1;

            for(int i = returnedMessages.size() - 1; i >= 0; i--){
                Log.d(TAG, "condition: " + (returnedMessages.get(i).getId() < largestIdMessage.getId())
                 + "returnedmsgID: " + returnedMessages.get(i).getId() + "largest id: " + largestIdMessage.getId());
                if(returnedMessages.get(i).getId() > largestIdMessage.getId()){
                    largestIdMessage = returnedMessages.get(i);
                    largestIdMessageIndex = i;
                    Log.d(TAG, "smallest message: " + largestIdMessage.getId());
                }
            }

            sortedMessages.add(largestIdMessage);
            returnedMessages.remove(largestIdMessageIndex);
            Log.d(TAG, "size of message: " + returnedMessages.size());
        }

        return sortedMessages;
    }

    private void populateInbox(List<Message> returnedMessages) {
        String[] fromUserName = new String[returnedMessages.size()];
        String[] text = new String[returnedMessages.size()];
        String[] timestamp = new String[returnedMessages.size()];
        String[] isRead = new String[returnedMessages.size()];
        String[] wholeMessage = new String[returnedMessages.size()];
        String singleMessage;
        StringBuilder messageStringBuilder = new StringBuilder();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm:ss aa");

        List<Message> sortedMessages = sortMessagesById(returnedMessages);

        for(int i = 0; i < sortedMessages.size(); i++){
            messageStringBuilder.append(Long.toString(sortedMessages.get(i).getId())).append(",");
            fromUserName[i] = sortedMessages.get(i).getFromUser().getName();
            text[i] = sortedMessages.get(i).getText();
            timestamp[i] = dateFormat.format(sortedMessages.get(i).getTimestamp());

            if(sortedMessages.get(i).isRead()){
                isRead[i] = "Read";
            } else {
                isRead[i] = "Unread";
            }

            singleMessage = fromUserName[i] + " (" + timestamp[i] + ") [" + isRead[i] + "]\n" + text[i];
            wholeMessage[i] = singleMessage;
            Log.d(TAG, "populateInbox: " + fromUserName[i] + " id " + sortedMessages.get(i).getId());
        }

        SelectedMessage.setFromNameArray(fromUserName);
        SelectedMessage.setMessageArray(text);

        Log.d(TAG, "builder: "+messageStringBuilder.toString());
        Preferences.saveMessageIdBuild(messageStringBuilder.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.inbox_layout, wholeMessage);
        ListView inboxList = findViewById(R.id.inbox_list_view);
        inboxList.setAdapter(adapter);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, InboxActivity.class);
    }

    private void setupSendMessageButton() {
        ImageButton sendMessageButton = findViewById(R.id.send_message_button);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SendMessageActivity.makeIntent(InboxActivity.this);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
    }
}
