package com.popo.mrpopo;

import android.database.Cursor;
import android.database.SQLException;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.popo.mrpopo.contentprovider.ContentDbHelper;
import com.popo.mrpopo.contentprovider.LocationContent;
import com.popo.mrpopo.contentprovider.School;
import com.popo.mrpopo.util.AppConstants;
import com.popo.mrpopo.util.DatabaseCopyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, LocationListener, LocationSource {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private OnLocationChangedListener mLocationChangeListener;
    private LocationManager locationManager;

    RightPanel rightPanel;
    View welcomePanel;
    TextView changeSchool;
    ListView schoolList;

    private int windowWidth;
    HashMap<School, Double> nearBySchools;
    private VelocityTracker mVelocityTracker = null;
    private int xVelocityThreshold;
    private int xPositionThreshold;
    private final double X_POSITION_THRESHOLD_RATIO = 0.4;
    private final double X_VELOCITY_THRESHOLD_RATIO = 0.20;
    HashMap<Marker, String> markers;
    Location mCurrentLocation;

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
        changeSchool = (TextView)findViewById(R.id.welcome_to_different_schools);
        changeSchool.setOnClickListener(new ChangeSchoolClickListener());
        schoolList = (ListView)findViewById(R.id.school_list);
        schoolList.setVisibility(View.GONE);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.windowWidth = dm.widthPixels;
        rightPanel.setX(this.windowWidth);
        rightPanel.setWindowWidth(this.windowWidth);
        this.xPositionThreshold = (int) (this.windowWidth * X_POSITION_THRESHOLD_RATIO);
        this.xVelocityThreshold = (int) (this.windowWidth * X_VELOCITY_THRESHOLD_RATIO);
        locationServiceSetup();
        setupMapIfNeeded();
        this.getNearbySchoolsAndSetCurrent();
    }

    private void locationServiceSetup() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location gpsLocation = null;
        Location networkLocation = null;
        if (locationManager != null) {

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, AppConstants.LOCATION_UPDATE_TIME_INTERVAL, AppConstants.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                // FIX ME: No GPS nor Network. Jump Out

            }

        } else {
            // Something is wrong with location manager Jump Out
            return;
        }

        if (gpsLocation != null && networkLocation != null){
            if (gpsLocation.getAccuracy() > networkLocation.getAccuracy()){
                mCurrentLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            }
            else {
                mCurrentLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            }
        }
        else if (gpsLocation != null){
            mCurrentLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        }
        else if (networkLocation != null) {
            mCurrentLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMapIfNeeded();
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

    private void setupMapIfNeeded() {
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
            mCurrentLocation = location;
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

    private void setCurrentSchool(School school){
        TextView welcomeToView = (TextView)findViewById(R.id.welcome_to);
        welcomeToView.setText(school.getName());
        clearAllMarkers();
        new DbAsyncTask().populatePoints(school);
    }

    private void clearAllMarkers(){
        mMap.clear();
        markers.clear();
    }
    private void getNearbySchoolsAndSetCurrent() {

        ArrayList<School> schools = new ArrayList<School>();
        nearBySchools = new DbAsyncTask().doInBackground();
        double minDistance = Double.MAX_VALUE;
        School closestSchool = null;
        for (School school : nearBySchools.keySet()){
            if (nearBySchools.get(school) < minDistance){
                closestSchool = school;
                minDistance = nearBySchools.get(school);
            }
        }
        ListView schoolList = (ListView)findViewById(R.id.school_list);
        schoolList.setOnItemClickListener(new OnSelectSchoolListItemListener());

        schoolList.setAdapter(new ArrayAdapter<School>(this, android.R.layout.simple_list_item_1, schools));
        ListAdapter listAdapter = schoolList.getAdapter();


        ViewGroup.LayoutParams params = schoolList.getLayoutParams();
        params.height = schoolList.getDividerHeight() * (listAdapter.getCount() - 1);
        schoolList.setLayoutParams(params);
        schoolList.requestLayout();
        Iterator<School> it = nearBySchools.keySet().iterator();
        while ( it.hasNext() ){
            schools.add(it.next());
        }
        setCurrentSchool(closestSchool);
    }

    private class ChangeSchoolClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            if ( View.VISIBLE == schoolList.getVisibility() ){
                schoolList.setVisibility(View.GONE);
            }
            else{
                schoolList.setVisibility(View.VISIBLE);
            }
        }
    }

    private class OnSelectSchoolListItemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView v = (TextView) view;
            School school = (School)adapterView.getItemAtPosition(i);
            setCurrentSchool(school);
        }
    }

    private class DbAsyncTask extends AsyncTask<Object, Object, HashMap<School, Double>>{

        protected void populatePoints(School school){
            ContentDbHelper mDbHelper = new ContentDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String where = LocationContent.PointsOfInterest.COLUMN_NAME_SCHOOL_ID + "=" + school.getId();
            Cursor c = db.query(LocationContent.PointsOfInterest.TABLE_NAME,
                        new String[]{LocationContent.PointsOfInterest.COLUMN_NAME_NAME,
                            LocationContent.PointsOfInterest.COLUMN_NAME_CONTENT_TEXT,
                            LocationContent.PointsOfInterest.COLUMN_NAME_LATITUDE,
                            LocationContent.PointsOfInterest.COLUMN_NAME_LONGITUDE},
                    where, null, null, null, null
            );
            while (c.moveToNext()) {
                double lat = c.getDouble(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_LATITUDE));
                double lng = c.getDouble(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_LONGITUDE));
                String name = c.getString(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_NAME));
                String content = c.getString(c.getColumnIndexOrThrow(LocationContent.PointsOfInterest.COLUMN_NAME_CONTENT_TEXT));
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                markers.put(m, name + "\n" +  content);
            }
        }

        protected HashMap<School, Double> doInBackground(Object... params) {
            copyDb();
            com.javadocmd.simplelatlng.LatLng currentLocation = new com.javadocmd.simplelatlng.LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            HashMap<School, Double> schools = new HashMap<School, Double>();

            ContentDbHelper mDbHelper = new ContentDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();


            Cursor schoolCursor = db.query(LocationContent.Schools.TABLE_NAME,
                    new String[]{LocationContent.Schools.COLUMN_NAME_ID,
                            LocationContent.Schools.COLUMN_NAME_NAME,
                            LocationContent.Schools.COLUMN_NAME_CENTER_LATITUDE,
                            LocationContent.Schools.COLUMN_NAME_CENTER_LONGITUDE},
                    null,null,null,null,null);

            while (schoolCursor.moveToNext()){
                double lat = schoolCursor.getDouble(schoolCursor.getColumnIndexOrThrow(LocationContent.Schools.COLUMN_NAME_CENTER_LATITUDE));
                double lng = schoolCursor.getDouble(schoolCursor.getColumnIndexOrThrow(LocationContent.Schools.COLUMN_NAME_CENTER_LONGITUDE));
                double distance = LatLngTool.distance(currentLocation, new com.javadocmd.simplelatlng.LatLng(lat, lng),  LengthUnit.MILE);
                if (distance <= AppConstants.MAX_DISTANCE_TO_CENTER_POINT){
                    schools.put( new School(schoolCursor.getInt(schoolCursor.getColumnIndexOrThrow(LocationContent.Schools.COLUMN_NAME_ID)),
                            schoolCursor.getString(schoolCursor.getColumnIndexOrThrow(LocationContent.Schools.COLUMN_NAME_NAME)),
                            lat,lng), distance);
                }
            }
            if (schools.size() == 0 ){
                Log.d("tag", "You're not near any school");
                //FIXME Throw some sort of exception or to expand the distance since no school found.
            }

            return schools;
        }

        private void copyDb() {
            DatabaseCopyUtil myDbHelper = new DatabaseCopyUtil(getApplicationContext());

            try {

                myDbHelper.createDataBase();

            } catch (IOException ioe) {

                throw new Error("Unable to create database");

            }

            try {

                myDbHelper.openDataBase();

            } catch (SQLException sqle) {

                throw sqle;

            }
        }
    }
}
