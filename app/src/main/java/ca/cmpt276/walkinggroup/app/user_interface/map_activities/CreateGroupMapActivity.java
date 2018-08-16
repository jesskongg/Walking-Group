package ca.cmpt276.walkinggroup.app.user_interface.map_activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.CustomInfoWindowAdapter;
import ca.cmpt276.walkinggroup.app.model.PlaceAutocompleteAdapter;
import ca.cmpt276.walkinggroup.app.model.PlaceInfo;

// Class CreateGroupMapActivity allows the user to select the destination and start
// locations of a walk

public class CreateGroupMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private double[] routeLatArray;
    private double[] routeLngArray;
    private int countLocationSelections = 0;
    private FusedLocationProviderClient locationClient;

    private AutoCompleteTextView mSearchView;
    private GoogleMap mMap;

    private final String TAG = "CREATE_GROUP_MAP_ACTIVITY";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2222;
    public static final int TWO_LOCATIONS_SELECTED = 2;
    public static final int ONE_LOCATION_SELECTED = 1;
    public static final int NO_LOCATION_SELECTED = 0;

    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final float DEFAULT_ZOOM = 15f;
    private final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168)
    , new LatLng(71, 136));

    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_groups_maps);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        mSearchView = findViewById(R.id.input_search_view);

        routeLatArray = new double[2];
        routeLngArray = new double[2];

        getLocationPermission();

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, CreateGroupMapActivity.class);
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION,COARSE_LOCATION};

        boolean isFineLocationAccessGranted = ContextCompat.
                checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean isCoarseLocationAccessGranted = ContextCompat.
                checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(isFineLocationAccessGranted && isCoarseLocationAccessGranted){
            mLocationPermissionGranted = true;
            setupMap();
            Log.d(TAG, "getLocationPermission: fine and coarse location permission granted");
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{

                for(int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        Log.d(TAG, "onRequestPermissionResult: location permissions denied");
                        mLocationPermissionGranted = false;
                        return;
                    }
                }
                if(grantResults.length > 0){
                    mLocationPermissionGranted = true;
                    setupMap();
                }
            }
        }
    }

    private void setupMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.join_group_map);
        mapFragment.getMapAsync(CreateGroupMapActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;

        if(mLocationPermissionGranted){
            getDeviceLocation();

            if(ActivityCompat.checkSelfPermission(this, FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            Toast.makeText(this, "Select a destination and a start location"
                    , Toast.LENGTH_SHORT).show();
            setupSearch();
            setupChooseLocations();
            setupFinalizeLocationButton();

        }
    }

    private void getDeviceLocation(){

        Log.d(TAG, "getDeviceLocation: attempting to obtain device's current location");
        try{
            if(mLocationPermissionGranted){
                Task location = locationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.e(TAG, "onComplete: null object");
                        }
                    }
                });
            }
        } catch (SecurityException exception) {
            Log.e(TAG, "getDeviceLocation: Security Exception: " + exception.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    //------------------------- Location Selection Functions -------------------------

    private void setupFinalizeLocationButton() {
        ImageView finalizeButton = findViewById(R.id.finalize_location_button);

        finalizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countLocationSelections == TWO_LOCATIONS_SELECTED){
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("routeLatitude", routeLatArray);
                    resultIntent.putExtra("routeLongitude", routeLngArray);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else if(countLocationSelections == ONE_LOCATION_SELECTED){
                    Toast.makeText(CreateGroupMapActivity.this,
                                    "Choose a start location",
                                    Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateGroupMapActivity.this,
                                    "Choose a destination location",
                                    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupChooseLocations() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "LAT: " + latLng.latitude + " LNG: " + latLng.longitude);

                String selectionTitle;

                if(countLocationSelections == NO_LOCATION_SELECTED){
                    selectionTitle = "Destination";
                } else {
                    selectionTitle = "Start";
                }

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title("Group")
                        .snippet(selectionTitle);

                setWalkingLocations(latLng.latitude, latLng.longitude, options);
            }
        });
    }

    private void setWalkingLocations(double latitude, double longitude, MarkerOptions options) {
        Log.d(TAG, "setWalkingLocations[" + countLocationSelections +
                "]: " + latitude + " " + longitude);

        if(countLocationSelections == TWO_LOCATIONS_SELECTED){
            countLocationSelections = NO_LOCATION_SELECTED;
            mMap.clear();
        }

        if(countLocationSelections == NO_LOCATION_SELECTED){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        mMap.addMarker(options);

        routeLatArray[countLocationSelections] = latitude;
        routeLngArray[countLocationSelections] = longitude;
        countLocationSelections++;

        if(countLocationSelections == TWO_LOCATIONS_SELECTED){
            Log.d(TAG, "setWalkingLocations: Start and destination locations selected");
        }
    }

    //------------------------- Search Functions -------------------------

    private void setupSearch() {
        Log.d(TAG, "setupSearch: initializing search function");

        mSearchView.setOnItemClickListener(mAutocompleteListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,
                Places.getGeoDataClient(this, null), LAT_LNG_BOUNDS, null);

        mSearchView.setAdapter(mPlaceAutocompleteAdapter);

        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d(TAG, "onEditorAction: action happening");
                boolean actionCondition = (actionId == EditorInfo.IME_ACTION_SEARCH || actionId ==
                        EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == keyEvent.KEYCODE_ENTER);

                if(actionCondition){
                    Log.d(TAG, "onEditorAction: detecting search entry");
                    geoLocate();
                }

                return false;
            }
        });

        setupGpsIcon();
        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchView.getText().toString();

        Geocoder geocoder = new Geocoder(CreateGroupMapActivity.this);
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException exception) {
            Log.e(TAG, "geoLocate: IOException: " + exception.getMessage());
        }

        if(list.size() > 0){
            Log.d(TAG, "geoLocate: valid address found");
            Address address = list.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM);
        }
    }

    private void setupGpsIcon() {
        ImageView mapGpsIcon = findViewById(R.id.gps_icon_view);

        mapGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupGpsIcon: Gps recentering!");
                getDeviceLocation();
            }
        });
    }

    //-----------------------google places API autocomplete suggestions ------------------
    private AdapterView.OnItemClickListener mAutocompleteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places
                    .GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);

            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete" +
                        "successfully! " + places.getStatus().toString());
                //Need to release place buffer objects otherwise there is a memory leak
                places.release();
                return;
            }

            final Place place = places.get(0);

            PlaceInfo placeInfo = new PlaceInfo();

            try {
                placeInfo.setName(place.getName().toString());
                placeInfo.setAddress(place.getAddress().toString());
                placeInfo.setLatLng(place.getLatLng());
                placeInfo.setPhoneNumber(place.getPhoneNumber().toString());

                Log.d(TAG, "onResult: place details received");
            } catch (NullPointerException exception) {
                Log.e(TAG, "onResult: NullPointerException: " + exception.getMessage());
            }
            moveCamera(placeInfo.getLatLng(), DEFAULT_ZOOM);

            AutoCompleteTextView view = findViewById(R.id.input_search_view);
            view.setText("");
            hideSoftKeyboard();
            places.release();
        }
    };

    //------------------------- Move Camera Functions -------------------------

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, "onConnectionFailed: " + connectionResult, Toast.LENGTH_SHORT).show();
    }
}
