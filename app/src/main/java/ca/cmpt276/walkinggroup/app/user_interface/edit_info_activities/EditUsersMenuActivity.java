package ca.cmpt276.walkinggroup.app.user_interface.edit_info_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.app.user_interface.monitor_activities.DisplayMonitoredToEditActivity;

// Class EditUsersMenuActivity presents a menu for editing users.

public class EditUsersMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_users_menu);
        setupEditUserButton();
        setupEditMyselfButton();
    }

    private void setupEditUserButton() {
        Button setupEditUserButton = findViewById(R.id.btn_edit_monitored);
        setupEditUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditUsersMenuActivity.this, DisplayMonitoredToEditActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupEditMyselfButton() {
        Button setupEditMyselfButton = findViewById(R.id.btn_edit_myself);
        setupEditMyselfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditUsersMenuActivity.this,EditUserActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = MainMenuActivity.makeIntent(EditUsersMenuActivity.this);
        startActivity(intent);
        finish();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, EditUsersMenuActivity.class);
    }
}
