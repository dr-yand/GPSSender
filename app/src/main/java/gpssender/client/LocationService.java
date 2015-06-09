package gpssender.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import gpssender.client.task.SendCoordinatesTask;
import gpssender.client.util.PreferenceUtils;

public class LocationService extends Service {

    private LocationManager mLocationManager;
    private Timer mTimer;
    private double mLat=0d, mLon=0d;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mLocationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                60 * 1000, 5, listener);

        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("location2",mLat+"");
                new SendCoordinatesTask(getApplicationContext(), PreferenceUtils.getUserId(getApplication()), mLat+"",mLon+"").execute(new Void[]{});
            }
        };
        mTimer.schedule(timerTask,60*1000,60*1000);
    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (location == null)
                return;
            mLat = location.getLatitude();
            mLon = location.getLongitude();
            Log.i("location",location.getLatitude()+"");
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(listener);
        listener = null;
        mTimer.cancel();
        super.onDestroy();
    }


}
