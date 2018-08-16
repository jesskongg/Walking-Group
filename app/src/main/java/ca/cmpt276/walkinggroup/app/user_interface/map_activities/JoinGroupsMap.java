package ca.cmpt276.walkinggroup.app.user_interface.map_activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.CustomInfoWindowAdapter;
import ca.cmpt276.walkinggroup.app.model.MapDialogFragment;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

// Class JoinGroupsMap allows users to join groups from a map.

public class JoinGroupsMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, MapDialogFragment.OnCompleteListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private WGServerProxy proxy;
    private Session session;
    private FusedLocationProviderClient locationClient;

    private String markerTitle;

    private final String TAG = "JoinGroupsMap";
    private Boolean permissionGranted = false;
    private static final int PERMISSION_REQUEST_CODE = 1992;
    private static final float DEFAULT_ZOOM = 15f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_groups_map);

        session = Session.getInstance();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        proxy = ProxyBuilder.getProxy(getString(R.string.cmpt276_server_api_key),
                Preferences.getToken(), 1);

        getLocationPermission();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.join_group_map);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        if (permissionGranted) {
            Boolean hasFinePermission = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            Boolean hasCoarseLocation = ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasFinePermission || !hasCoarseLocation) {
                Log.d(TAG, "Failed to load map: missing permission");
                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            centerMapOnUser();
            setupGpsIcon();
            getGroupLocations();
        }
    }

    private void setupGpsIcon() {
        ImageView mapGpsIcon = findViewById(R.id.join_groups_gps_icon_view);

        mapGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupGpsIcon: Gps recentering!");
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

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
                            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(JoinGroupsMap.this));
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

    private void getGroupLocations(){
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(this, caller, returnedGroups ->
                populateMapWithWalkingGroups(returnedGroups));
    }

    private void populateMapWithWalkingGroups(List<Group> returnedGroups) {
        double[] routeLatArray;
        double[] routeLngArray;
        LatLng latLng;
        String groupDescription;
        Long groupId;

        for(int i = 0; i < returnedGroups.size(); i++){
            Group group = returnedGroups.get(i);
            if(group.getRouteLatArray() != null && group.getRouteLngArray() != null){
                if(group.getRouteLatArray().length == 2 && group.getRouteLngArray().length == 2){
                    groupDescription = returnedGroups.get(i).getGroupDescription();
                    groupId = returnedGroups.get(i).getId();
                    routeLatArray = returnedGroups.get(i).getRouteLatArray();
                    routeLngArray = returnedGroups.get(i).getRouteLngArray();
                    latLng = new LatLng(routeLatArray[1], routeLngArray[1]);

                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(groupId.toString())
                            .snippet(groupDescription);
                    mMap.addMarker(options);
                }
            }
        }
        setupMarkerInfoWindowClick();
    }

    private void setupMarkerInfoWindowClick() {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                MapDialogFragment mapDialogFragment = new MapDialogFragment();
                mapDialogFragment.setGroupDescription(marker.getSnippet());
                mapDialogFragment.show(fragmentManager, "Dialog");
                markerTitle = marker.getTitle();
            }
        });
    }

    public void onComplete(boolean confirm){
        if(confirm){
            addUserToGroup(markerTitle);
            Log.d(TAG, "onInfoWindowClick: join group confirmed");
        } else {
            Log.d(TAG, "onInfoWindowClick: join group rejected");
        }
    }

    private void addUserToGroup(String receivedGroupId){
        Long groupId = Long.parseLong(receivedGroupId);
        Log.d(TAG, "addUserToGroup: adding user to group");
        Call<List<User>> caller = proxy.addGroupMember(groupId, session.getSessionUser());
        ProxyBuilder.callProxy(JoinGroupsMap.this, caller, response -> notifyUserToastAndLog());
    }

    private void notifyUserToastAndLog() {
        Toast.makeText(this, "Request Sent", Toast.LENGTH_SHORT).show();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, JoinGroupsMap.class);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, "onConnectionFailed: " + connectionResult, Toast.LENGTH_SHORT).show();
    }
}
