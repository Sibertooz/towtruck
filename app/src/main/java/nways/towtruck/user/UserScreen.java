package nways.towtruck.user;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hamza.slidingsquaresloaderview.SlidingSquareLoaderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import nways.towtruck.messenger.UserChat;
import nways.towtruck.R;
import nways.towtruck.server.HttpParse;
import nways.towtruck.server.HttpServicesClass;

public class UserScreen extends AppCompatActivity implements LocationListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {
    private GoogleMap myMap;
    private ProgressDialog myProgress;

    private static final String MYTAG = "MYTAG";

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    String HttpURL = "http://172.20.10.3/towtruckbackend/GetProviders.php";
    String LocationHttpURL = "http://172.20.10.3/towtruckbackend/UserAddLocationStatus.php";
    String getEmail, getLat, getLng;

    Button btnRefresh;

    SlidingSquareLoaderView slidingview;

    String providerPhone, providerName, providerEmail;
    double providerLat, providerLng;

    ArrayList<Provider> providers;

    Marker marker;

    String finalResult ;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_screen);

        slidingview = findViewById(R.id.sliding_view);

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(this);

        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        myProgress.show();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentUser);
        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });

        Intent intent = getIntent();
        getEmail = intent.getStringExtra(UserActivity.finalEmail);
    }

    private void onMyMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                myProgress.dismiss();
                askPermissionsAndShowMyLocation();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        myMap.setMyLocationEnabled(true);
    }

    private void askPermissionsAndShowMyLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                return;
            }
        }
        this.showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();
                    this.showMyLocation();
                }
                else Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    private void showMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) return;

        final long MIN_TIME_BW_UPDATES = 1000;
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation;
        try {
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        } catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            getLat = String.valueOf(myLocation.getLatitude());
            getLng = String.valueOf(myLocation.getLongitude());

            MarkerOptions option = new MarkerOptions();
            option.title("My Location");
            option.position(latLng);
            Marker currentMarker = myMap.addMarker(option);
            currentMarker.showInfoWindow();
            currentMarker.setTag("myTag");
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
        }
    }

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    public boolean onMarkerClick(final Marker marker) {
        if (marker.getTag().toString().equals("myTag")) return true;
        else {
            providerName = marker.getTitle();
            providerPhone = marker.getSnippet();
            providerEmail = marker.getTag().toString();
            Intent intent = new Intent(this, UserChat.class);
            intent.putExtra("name", providerName);
            intent.putExtra("phone", providerPhone);
            intent.putExtra("email", providerEmail);
            intent.putExtra("myLat", getLat);
            intent.putExtra("myLng", getLng);
            intent.putExtra("myEmail", getEmail);
            startActivity(intent);
        }
        return false;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRefresh: new ShowProviders(UserScreen.this).execute();
                UserUpdateLocationStatus(getEmail, getLat, getLng);
                break;
        }
    }

    private class ShowProviders extends AsyncTask<Void, Void, Void> {
        public Context context;
        String JSonResult;

        ShowProviders(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            slidingview.start();
            slidingview.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpServicesClass httpServicesClass = new HttpServicesClass(HttpURL);
            try {
                httpServicesClass.ExecutePostRequest();
                if(httpServicesClass.getResponseCode() == 200) {
                    JSonResult = httpServicesClass.getResponse();
                    if(JSonResult != null) {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(JSonResult);
                            JSONObject jsonObject;
                            Provider provider;
                            providers = new ArrayList<>();

                            for(int i=0; i< jsonArray.length(); i++) {
                                jsonObject = jsonArray.getJSONObject(i);
                                providerName = jsonObject.getString("name");
                                providerEmail = jsonObject.getString("email");
                                providerPhone = jsonObject.getString("phone");
                                providerLat = jsonObject.getDouble("lat");
                                providerLng = jsonObject.getDouble("lng");

                                provider = new Provider(providerName, providerPhone, providerEmail, providerLat, providerLng);
                                providers.add(provider);
                            }
                        }
                        catch (JSONException e) { e.printStackTrace(); }
                    }
                }
                else { Toast.makeText(context, httpServicesClass.getErrorMessage(), Toast.LENGTH_SHORT).show(); }
            }
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (int i = 0; i < providers.size(); i++) {
                createMarker(providers.get(i).getProviderLat(), providers.get(i).getProviderLng(),
                        providers.get(i).getProviderName(), providers.get(i).getProviderPhone(),
                        providers.get(i).getProviderEmail());
            }
            providers.clear();
            slidingview.stop();
            slidingview.setVisibility(View.GONE);
        }
    }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet, String tag) {
        marker = myMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        marker.setTag(tag);
        marker.showInfoWindow();
        myMap.setOnMarkerClickListener(this);
        return marker;
    }

    public void UserUpdateLocationStatus(final String U_email, final String U_lat, final String U_lng){
        class UserUpdateLocationStatusClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
            }

            @Override
            protected String doInBackground(String... params) {
                hashMap.put("email", params[0]);
                hashMap.put("lat", params[1]);
                hashMap.put("lng", params[2]);

                finalResult = httpParse.postRequest(hashMap, LocationHttpURL);
                return finalResult;
            }
        }
        UserUpdateLocationStatusClass userUpdateLocationStatusClass = new UserUpdateLocationStatusClass();
        userUpdateLocationStatusClass.execute(U_email, U_lat, U_lng);
    }
}