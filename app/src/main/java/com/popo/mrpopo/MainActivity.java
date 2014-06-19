package com.popo.mrpopo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.popo.mrpopo.com.popo.mrpopo.contentprovider.ContentDbHelper;
import com.popo.mrpopo.com.popo.mrpopo.contentprovider.LocationContent;
import com.popo.mrpopo.util.AppConstants;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, LocationListener, LocationSource {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private OnLocationChangedListener mLocationChangeListener;
    private LocationManager locationManager;

    RightPanel rightPanel;
    View welcomePanel;

    private int windowWidth;

    private VelocityTracker mVelocityTracker = null;
    private int xVelocityThreshold;
    private int xPositionThreshold;
    private final double X_POSITION_THRESHOLD_RATIO = 0.4;
    private final double X_VELOCITY_THRESHOLD_RATIO = 0.20;
    HashMap<Marker, String> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        markers = new HashMap<Marker, String>();
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_main);
        rightPanel = (RightPanel) findViewById(R.id.rightpanel);
        rightPanel.setVisibility(View.GONE);

        welcomePanel = findViewById(R.id.welcome_panel);
        welcomePanel.animate().translationY(20);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.windowWidth = dm.widthPixels;
        rightPanel.setX(this.windowWidth);
        rightPanel.setWindowWidth(this.windowWidth);
        this.xPositionThreshold = (int) (this.windowWidth * X_POSITION_THRESHOLD_RATIO);
        this.xVelocityThreshold = (int) (this.windowWidth * X_VELOCITY_THRESHOLD_RATIO);
        locationServiceSetup();
        setUpMapIfNeeded();
        this.performDbRead();
    }

    private void locationServiceSetup() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
            } else {
                // FIX ME: No GPS nor Network
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
        this.rightPanel.togglePanel();
    }

    @Override
    public void onBackPressed() {
        if (this.rightPanel.getCurrentContentFragmentState() == RightPanel.ContentFragmentState.OPEN) {
            this.rightPanel.togglePanel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        TextView rightPanelText = (TextView) findViewById(R.id.rightpanelcontent);
        rightPanelText.setText(markers.get(marker));
        this.toggleContent();
        return true;
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

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (rightPanel.getCurrentContentFragmentState() == RightPanel.ContentFragmentState.OPEN) {
            int index = motionEvent.getActionIndex();
            int action = motionEvent.getActionMasked();
            int pointerId = motionEvent.getPointerId(index);

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                    mVelocityTracker.addMovement(motionEvent);
                    if (motionEvent.getX() > rightPanel.getPreviousDownX()) {
                        rightPanel.setX(motionEvent.getX(index) - rightPanel.getPreviousDownX());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = mVelocityTracker.getXVelocity(pointerId);
                    if (xVelocity > this.xVelocityThreshold || (motionEvent.getX() - rightPanel.getPreviousDownX() > this.xPositionThreshold)) {
                        rightPanel.animateSwipeClose();
                    } else {
                        rightPanel.animateSwipeOpen();
                    }
                    mVelocityTracker.clear();
                    break;
                default:
                    Log.d("LOGTAG", "Nothing we care about atm");
                    break;
            }

            return true;
        }

        return true;
    }

    private void performDbRead() {

        new DbAsyncTask().doInBackground();

    }

    private class DbAsyncTask extends AsyncTask<Object, Object, Cursor>{
        protected Cursor doInBackground(Object... params) {
            ContentDbHelper mDbHelper = new ContentDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            Cursor c = db.query(LocationContent.PointsOfInterest.TABLE_NAME,
                    new String[]{LocationContent.PointsOfInterest.COLUMN_NAME_NAME,
                            LocationContent.PointsOfInterest.COLUMN_NAME_CONTENT_TEXT,
                            LocationContent.PointsOfInterest.COLUMN_NAME_LATITUDE,
                            LocationContent.PointsOfInterest.COLUMN_NAME_LONGITUDE},
                    null, null, null, null, null
            );
            while (c.moveToNext()) {
                double lat = c.getDouble(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_LATITUDE));
                double lng = c.getDouble(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_LONGITUDE));
                String name = c.getString(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_NAME));
                String content = c.getString(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_CONTENT_TEXT));
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                markers.put(m, name + "\n" +  content);
            }
            return c;
        }
    }
}
