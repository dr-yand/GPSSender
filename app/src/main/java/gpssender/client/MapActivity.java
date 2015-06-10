package gpssender.client;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import gpssender.client.task.LoginTask;
import gpssender.client.util.PreferenceUtils;


public class MapActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        addNotification();

        initMenu();
        initMap();
        initView();

    }


    private void addNotification(){
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags (Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentIntent(pintent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

    private void updateCurrentLocation(double lat, double lon){
        LatLng latLng = new LatLng(lat, lon);
        CameraPosition CITY =
                new CameraPosition.Builder().target(latLng)
                        .zoom(15f)
                        .bearing(0)
                        .tilt(25)
                        .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CITY));

        if(mMarker!=null)
            mMarker.remove();
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
    }


    private void initView(){
        ((Button)findViewById(R.id.signout)).setOnClickListener(this);
    }

    private void initMenu(){
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Устанавливаем параметры карты
    private void initMap(){
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps)).getMap();

        mMap.setOnMarkerClickListener(this);

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                updateCurrentLocation(location.getLatitude(), location.getLongitude());
                mMap.setOnMyLocationChangeListener(null);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        marker.showInfoWindow();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.signout){
            stopService(new Intent(this, LocationService.class));

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);

            PreferenceUtils.saveUserId(this, "");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
