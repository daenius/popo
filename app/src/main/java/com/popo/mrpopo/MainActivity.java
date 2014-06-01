package com.popo.mrpopo;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.popo.mrpopo.util.AppConstants;


public class MainActivity extends FragmentActivity implements LandmarkContentFragment.OnFragmentInteractionListener, GoogleMap.OnMarkerClickListener, LocationListener, LocationSource {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private OnLocationChangedListener mLocationChangeListener;
    private LocationManager locationManager;
    private Marker myMarker;
    FragmentViewContainer rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        this.rootView = (FragmentViewContainer) this.getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(rootView);
        locationServiceSetup();
        setUpMapIfNeeded();

    }

    private void locationServiceSetup() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
            } else if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
            } else {
                // No GPS nor Network
            }
        } else {
            // Something is wrong with location manager
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (locationManager != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void toggleContent() {
        this.rootView.toggleContent();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        if (rootView.getCurrentContentFragmentState() == FragmentViewContainer.ContentFragmentState.OPEN) {
            this.toggleContent();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(myMarker)) {
            this.toggleContent();
            return true;
        }
        return false;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

        mMap.setLocationSource(this);
    }

    private void setUpMap() {
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (mLocationChangeListener != null) {
            mLocationChangeListener.onLocationChanged(location);

            CameraPosition.Builder cameraPositionBuilder = CameraPosition.builder();
            cameraPositionBuilder = cameraPositionBuilder.zoom(16);
            cameraPositionBuilder = cameraPositionBuilder.target(new LatLng(location.getLatitude(), location.getLongitude()));

            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build());
            mMap.animateCamera(cu);
            mMap.clear();
            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationChangeListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

        mLocationChangeListener = null;
    }
}
