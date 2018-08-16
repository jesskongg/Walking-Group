package ca.cmpt276.walkinggroup.app.user_interface.message_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class PanicActivity allows a user to send a panic message.

public class PanicActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private Session session;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        setupPanicButton();
    }

    private void setupPanicButton() {
        message = new Message();
        EditText editMessageView = findViewById(R.id.edit_message);
        Button setupPanicMessageButton = findViewById(R.id.btn_panic_message);

        setupPanicMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message.setIsRead(false);
                message.setEmergency(true);
                if(editMessageView.getText().length() != 0){
                    message.setText("Panic Alert: " + editMessageView.getText().toString());
                } else {
                    message.setText("Panic Alert: Help Requested");
                }

                Call<List<Message>> callerParents = proxy.newMessageToParentsOf
                        (session.getSessionUser().getId(), message);
                ProxyBuilder.callProxy(PanicActivity.this,callerParents,returnedMessage->
                                        printSuccess());
                finish();
            }

            private void printSuccess() {
                Toast.makeText(PanicActivity.this, "Panic alert sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, PanicActivity.class);
    }
}
