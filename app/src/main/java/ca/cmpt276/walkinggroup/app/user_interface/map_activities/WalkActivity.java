package ca.cmpt276.walkinggroup.app.user_interface.map_activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
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

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.CustomInfoWindowAdapter;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.app.user_interface.MainMenuActivity;
import ca.cmpt276.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class WalkActivity shows a map of the current walk and updates user GPS location.

public class WalkActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WalkActivity";
    private static final int PERMISSION_REQUEST_CODE = 2018;
    private static final String CHANNEL_ID = "WalkActivity";
    private static final float DEFAULT_ZOOM = 15f;
    private static final int UPLOAD_DELAY = 30 * 1000; // milliseconds
    private static final int QUIT_DELAY = 10 * 60 * 1000; // milliseconds
    private static final int DESTINATION_THRESHOLD = 50; // metres

    private GoogleMap map;
    private FusedLocationProviderClient locationClient;
    private Handler uploadHandler;
    private Runnable uploadRunnable;
    private Handler quitHandler;
    private Runnable quitRunnable;
    private NotificationManager notificationManager;

    private GoogleApiClient googleApiClient;
    private WGServerProxy proxy;
    private Session session;
    private Long groupId;
    private Boolean permissionGranted = false;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);

        groupId = getIntent().getLongExtra("groupId", 0);
        Log.d(TAG, "Parsed group ID: " + Long.toString(groupId));

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
                .findFragmentById(R.id.walk_map);
        mapFragment.getMapAsync(WalkActivity.this);
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

            Call<Group> caller = proxy.getGroupById(groupId);
            ProxyBuilder.callProxy(WalkActivity.this, caller, g -> {
                group = g;
                showWalkingLocations();
            });

            startUploadHandler();
            showUploadNotification();
        }
    }

    private void startUploadHandler() {
        Log.d(TAG, "Starting upload handler");
        uploadUserLocation();
        uploadHandler = new Handler();
        uploadHandler.postDelayed(uploadRunnable = new Runnable() {
            @Override
            public void run() {
                uploadUserLocation();
                uploadHandler.postDelayed(this, UPLOAD_DELAY);
            }
        }, UPLOAD_DELAY);
    }

    private void stopUploadHandler() {
        Log.d(TAG, "Stopping upload handler");
        if (uploadHandler != null) {
            uploadHandler.removeCallbacks(uploadRunnable);
        }
    }

    private void showUploadNotification() {
        Log.d(TAG, "Showing upload notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(WalkActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_map_gps)
                .setContentTitle("Sharing location")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(WalkActivity.this, WalkActivity.class);
        PendingIntent pending = PendingIntent.getActivity(WalkActivity.this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, builder.build());
    }

    private void hideUploadNotification() {
        Log.d(TAG, "Hiding upload notification");
        notificationManager.cancelAll();
    }

    private void showWalkingLocations() {
        Log.d(TAG, "Showing walking locations");
        double[] latitudes = group.getRouteLatArray();
        double[] longitudes = group.getRouteLngArray();

        LatLng startPos = new LatLng(latitudes[1], longitudes[1]);
        MarkerOptions startOptions = new MarkerOptions()
                .position(startPos)
                .title(getString(R.string.walk_start_text))
                .snippet(group.getGroupDescription());
        map.addMarker(startOptions);
        Log.d(TAG, "Set starting marker");

        LatLng endPos = new LatLng(latitudes[0], longitudes[0]);
        MarkerOptions endOptions = new MarkerOptions()
                .position(endPos)
                .title(getString(R.string.walk_end_text))
                .snippet(group.getGroupDescription());
        map.addMarker(endOptions);
        Log.d(TAG, "Set ending marker");
    }

    private void setupGpsIcon() {
        Log.d(TAG, "Setting up GPS icon");
        ImageView mapGpsIcon = findViewById(R.id.walk_gps_icon_view);

        mapGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerMapOnUser();
            }
        });
    }

    private void uploadUserLocation() {
        Log.d(TAG, "Uploading user location");
        try {
            if (permissionGranted) {
                Task location = locationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null){
                            Log.d(TAG, "onComplete: found location!");

                            Location location = (Location) task.getResult();

                            GpsLocation gpsLocation = new GpsLocation();
                            gpsLocation.setLat(location.getLatitude());
                            gpsLocation.setLng(location.getLongitude());
                            gpsLocation.setTimestamp(Long.toString(location.getTime()));

                            Call<GpsLocation> caller = proxy.setLastGpsLocation
                                    (session.getSessionUser().getId(), gpsLocation);
                            ProxyBuilder.callProxy(WalkActivity.this, caller, x -> {
                                Log.d(TAG, "Set last GPS location to: " + gpsLocation.toString());
                            });

                            cancelIfAtDestination(location);

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

    private void cancelIfAtDestination(Location currentLocation) {
        Log.d(TAG, "Checking if user is at destination");

        if (group == null) {
            Log.d(TAG, "Group is null; skipping check");
            return;
        }

        double[] latitudes = group.getRouteLatArray();
        double[] longitudes = group.getRouteLngArray();
        Location destination = new Location("dummy");
        destination.setLatitude(latitudes[0]);
        destination.setLongitude(longitudes[0]);

        if (currentLocation.distanceTo(destination) <= DESTINATION_THRESHOLD) {
            Toast.makeText(WalkActivity.this, "Reached destination", Toast.LENGTH_SHORT).show();
            addRewardPoints();
            hideUploadNotification();
            stopUploadHandler();
            startQuitHandler();
        }
    }

    private void addRewardPoints() {
        final Integer REWARD_POINTS_PER_WALK = 10;

        User currentUser = session.getSessionUser();

        Integer newTotalPoints = currentUser.getTotalPointsEarned()
                + REWARD_POINTS_PER_WALK;
        Integer newCurrentPoints = currentUser.getCurrentPoints()
                + REWARD_POINTS_PER_WALK;

        currentUser.setTotalPointsEarned(newTotalPoints);
        currentUser.setCurrentPoints(newCurrentPoints);

        Call<User> caller = proxy.editUser(currentUser.getId(), currentUser);
        ProxyBuilder.callProxy(this, caller, returnedEditedUser -> logUserUpdate(returnedEditedUser));
        Log.d(TAG, "points added to user");
    }

    private void logUserUpdate(User returnedEditedUser) {
        Log.d(TAG, "logUserUpdate: " + returnedEditedUser.toString());
    }

    private void startQuitHandler() {
        Log.d(TAG, "Starting quit handler");
        quitHandler = new Handler();
        quitHandler.postDelayed(quitRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, QUIT_DELAY);
    }

    private void stopQuitHandler() {
        Log.d(TAG, "Stopping quit handler");
        if (quitHandler != null) {
            quitHandler.removeCallbacks(quitRunnable);
        }
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
                            map.setInfoWindowAdapter(new CustomInfoWindowAdapter(WalkActivity.this));
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

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        startMainMenu();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        Toast.makeText(WalkActivity.this, "Stopped walking with group", Toast.LENGTH_SHORT).show();
        hideUploadNotification();
        stopUploadHandler();
        stopQuitHandler();
        super.onDestroy();
    }

    private void startMainMenu() {
        Intent intent = MainMenuActivity.makeIntent(WalkActivity.this);
        startActivity(intent);
    }


    public static Intent makeIntent(Context context) {
        return new Intent(context, WalkActivity.class);
    }
}
