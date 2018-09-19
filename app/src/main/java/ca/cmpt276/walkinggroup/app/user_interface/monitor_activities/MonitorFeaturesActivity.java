package ca.cmpt276.walkinggroup.app.user_interface.monitor_activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.user_interface.ChangeMonitorActivity;
import ca.cmpt276.walkinggroup.app.user_interface.ListMonitorActivity;
import ca.cmpt276.walkinggroup.app.user_interface.map_activities.DashboardMapActivity;

public class MonitorFeaturesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_features);

        setupButtons();

    }

    private void setupButtons() {

        Button manageMonitors = findViewById(R.id.manage_monitor_btn);
        Button monitorees = findViewById(R.id.monitorees_btn);
        Button monitors = findViewById(R.id.monitors_btn);
        Button monitoredMap = findViewById(R.id.monitors_map_btn);

        manageMonitors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ChangeMonitorActivity.makeIntent(MonitorFeaturesActivity.this);
                startActivity(intent);
            }
        });

        monitorees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ListMonitorActivity.makeIntent(
                        MonitorFeaturesActivity.this, ListMonitorActivity.MONITORED);
                startActivity(intent);
            }
        });

        monitors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ListMonitorActivity.makeIntent(MonitorFeaturesActivity.this,
                        ListMonitorActivity.MONITORING);
                startActivity(intent);
            }
        });

        monitoredMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = DashboardMapActivity.makeIntent(MonitorFeaturesActivity.this);
                startActivity(intent);
            }
        });

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MonitorFeaturesActivity.class);
    }
}
