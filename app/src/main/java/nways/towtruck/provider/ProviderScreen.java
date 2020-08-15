package nways.towtruck.provider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.messenger.ProviderChat;
import nways.towtruck.messenger.ProviderChatService;
import nways.towtruck.provider.crud.ShowSingleRecordActivity;
import nways.towtruck.server.HttpParse;

public class ProviderScreen extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Switch btnSwitch;
    Button btnInfo, btnExit;

    private GoogleMap myMap;
    private ProgressDialog myProgress;

    private static final String MYTAG = "MYTAG";
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static String finalEmail = "";

    String HttpURL = "http://172.20.10.3/towtruckbackend/ProviderAddLocationStatus.php";
    String finalResult ;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    String getStatus, getEmail, getLat, getLng;

    BroadcastReceiver bReceiver;

    String to_email, from_email;

    final String LOG_TAG = "myLogs";

    final int DIALOG_EXIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_screen);

        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        myProgress.show();

        btnSwitch = findViewById(R.id.btnSwitch);
        btnSwitch.setOnClickListener(this);

        btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(this);

        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(this);

        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentProvider);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });

        Intent intent = getIntent();
        getEmail = intent.getStringExtra(LoginActivity.UserEmail);

        UserUpdateLocationStatus(getEmail, getLat, getLng, "offline");

        startService(new Intent(this, ProviderChatService.class).putExtra("myEmail", getEmail));

        IntentFilter intentFilter = new IntentFilter("android.intent.action.UPDATE");
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                from_email = intent.getStringExtra("from_email");
                to_email = intent.getStringExtra("to_email");

                if (to_email.equals(getEmail)) {
                    showDialog(DIALOG_EXIT);
                    createNotification();
                }
                Log.i(LOG_TAG, "Email принят");
            }
        };
        registerReceiver(bReceiver, intentFilter);
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

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_EXIT) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.notification).setMessage(R.string.newRecord)
                    .setIcon(R.drawable.new_record)
                    .setPositiveButton(R.string.seeRecord, myClickListener)
                    .setCancelable(false);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    finish();
                    startActivity(new Intent(ProviderScreen.this, ProviderChat.class)
                            .putExtra("from_email", from_email).putExtra("to_email", to_email)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
            }
        }
    };

    @Override
    public void onBackPressed(){}

    public void onClick(View v){
        Intent intent;
        switch(v.getId()) {
            case R.id.btnSwitch:
                if (btnSwitch.isChecked()) {
                    getStatus = "online";
                    LayoutInflater inflater = getLayoutInflater();

                    View layout = inflater.inflate(R.layout.toast_layout_free,
                            (ViewGroup) findViewById(R.id.toast_layout_root_free));

                    TextView text = layout.findViewById(R.id.text_free);
                    text.setText("Свободен");

                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } else {
                    getStatus = "offline";
                    LayoutInflater inflater = getLayoutInflater();

                    View layout = inflater.inflate(R.layout.toast_layout_busy,
                            (ViewGroup) findViewById(R.id.toast_layout_root_busy));

                    TextView text = layout.findViewById(R.id.text_busy);
                    text.setText("Занят");

                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                }
                UserUpdateLocationStatus(getEmail, getLat, getLng, getStatus);
                break;
            case R.id.btnInfo:
                Intent intentEmail = getIntent();
                String email = intentEmail.getStringExtra(LoginActivity.UserEmail);
                intent = new Intent(this, ShowSingleRecordActivity.class);
                intent.putExtra(finalEmail, email);
                startActivity(intent);
                break;
            case R.id.btnExit:
                finish();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Log Out Successfully", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void UserUpdateLocationStatus(final String U_email, final String U_lat, final String U_lng, final String U_status){
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
                hashMap.put("status", params[3]);

                finalResult = httpParse.postRequest(hashMap, HttpURL);
                return finalResult;
            }
        }
        UserUpdateLocationStatusClass userUpdateLocationStatusClass = new UserUpdateLocationStatusClass();
        userUpdateLocationStatusClass.execute(U_email, U_lat, U_lng, U_status);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void createNotification() {
        unregisterReceiver(bReceiver);
        stopService(new Intent(ProviderScreen.this, ProviderChatService.class));

        Intent resultIntent = new Intent(this, ProviderChat.class);
        resultIntent.putExtra("from_email", from_email).putExtra("to_email", to_email)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon, options);

        Uri soundNotif = Uri.parse( "android.resource://" + getPackageName() + "/" + R.raw.sound);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_50)
                        .setContentTitle("TowTruck")
                        .setContentText("У Вас новое сообщение")
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmap)
                        .setVibrate(new long[] { 1000, 1000, 1000 })
                        .setLights(Color.RED, 3000, 3000)
                        .setSound(soundNotif)
                        .setTicker("Message");

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
