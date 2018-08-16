package ca.cmpt276.walkinggroup.app.user_interface.map_activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.CustomInfoWindowAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class DashboardMapActivity shows the last reported locations and check-ins of monitored users.

public class DashboardMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DashboardMapActivity";
    private static final int PERMISSION_REQUEST_CODE = 1111;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap map;
    private FusedLocationProviderClient locationClient;

    private GoogleApiClient googleApiClient;
    private WGServerProxy proxy;
    private Session session;
    private Boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_map);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        session = Session.getInstance();
        proxy = session.getSessionProxy();

        getLocationPermission();
    }

    private void getLocationPermission() {
        boolean isFineGranted = ContextCompat.checkSelfPermission(
                this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean isCoarseGranted = ContextCompat.checkSelfPermission(
                this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (isFineGranted && isCoarseGranted) {
            Log.d(TAG, "Location permissions already granted");
            permissionGranted = true;
            setupMap();
        } else {
            Log.d(TAG, "Requesting location permissions");
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void setupMap(){
        Log.d(TAG, "Setting up map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dashboard_map);
        mapFragment.getMapAsync(DashboardMapActivity.this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        map = googleMap;

        if (permissionGranted) {
            Boolean hasFinePermission = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            Boolean hasCoarseLocation = ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasFinePermission || !hasCoarseLocation) {
                Log.d(TAG, "Failed to load map: missing permission");
                return;
            }

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);

            centerMapOnUser();
            setupGpsIcon();

            Call<List<User>> caller = proxy.getMonitorsUsers(session.getSessionUser().getId());
            ProxyBuilder.callProxy(DashboardMapActivity.this, caller,
                    monitoredUsers -> expandAndShowUsers(monitoredUsers));

        }
    }

    private void expandAndShowUsers(List<User> stubUsers) {
        Log.d(TAG, "Expanding and showing users");

        for (int i = 0; i < stubUsers.size(); i++) {
            Call<User> caller = proxy.getUserById(stubUsers.get(i).getId());
            ProxyBuilder.callProxy(DashboardMapActivity.this, caller, user -> {
                showLeadersOfUser(user);
                showMonitoredUser(user);
            });
        }
    }

    private void showMonitoredUser(User user) {
        Log.d(TAG, "Adding marker for user: " + user.toString());

        GpsLocation gpsLocation = user.getLastGpsLocation();
        if ((gpsLocation.getLat() == null) || (gpsLocation.getLng() == null)) {
            Log.d(TAG, "Skipping user with no GPS location");
            return;
        }

        LatLng position = new LatLng(gpsLocation.getLat(), gpsLocation.getLng());

        Timestamp lastCheckIn = new Timestamp(Long.valueOf(gpsLocation.getTimestamp()));
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd hh:mm a");

        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title(user.getName())
                .snippet(formatter.format(lastCheckIn));
        map.addMarker(options);
    }

    private void showLeadersOfUser(User user) {
        Log.d(TAG, "Showing group leaders for user: " + user.toString());
        Long userId = user.getId();

        Call<List<Group>> groupCaller = proxy.getGroups();
        ProxyBuilder.callProxy(DashboardMapActivity.this, groupCaller, groups -> {

            for (int i = 0; i < groups.size(); i++) {
                Call<Group> memberCaller = proxy.getGroupById(groups.get(i).getId());
                ProxyBuilder.callProxy(DashboardMapActivity.this, memberCaller, group -> {

                    Set<User> memberUsers = group.getMemberUsers();
                    if (memberUsers.contains(user)) {
                        Log.d(TAG, "User is a member of group: " + group.getGroupDescription());
                        Call<User> leaderCaller = proxy.getUserById(group.getLeader().getId());
                        ProxyBuilder.callProxy(DashboardMapActivity.this, leaderCaller,
                                leader -> showGroupLeader(leader));
                    }
                });
            }
        });
    }

    private void showGroupLeader(User leader) {
        Log.d(TAG, "Adding marker for leader: " + leader.toString());

        GpsLocation gpsLocation = leader.getLastGpsLocation();
        if ((gpsLocation.getLat() == null) || (gpsLocation.getLng() == null)) {
            Log.d(TAG, "Skipping leader with no GPS location");
            return;
        }

        LatLng position = new LatLng(gpsLocation.getLat(), gpsLocation.getLng());

        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title(leader.getName())
                .snippet("Leader");
        map.addMarker(options);
    }

    private void setupGpsIcon() {
        Log.d(TAG, "Setting up GPS icon");
        ImageView mapGpsIcon = findViewById(R.id.dashboard_map_gps_icon_view);

        mapGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerMapOnUser();
            }
        });
    }

    private void centerMapOnUser() {
        Log.d(TAG, "Centering map on user");

        try {
            if (permissionGranted) {
                Task location = locationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null){
                            Log.d(TAG, "onComplete: found location!");

                            Location currentLocation = (Location) task.getResult();
                            LatLng position = new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude());

                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
                            map.setInfoWindowAdapter(new CustomInfoWindowAdapter(DashboardMapActivity.this));
                        } else {
                            Log.e(TAG, "onComplete: null object");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: " + e.toString());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, "onConnectionFailed: " + connectionResult, Toast.LENGTH_SHORT).show();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, DashboardMapActivity.class);
    }
}
