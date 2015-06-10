package gpssender.client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
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

        mLocationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10 * 1000, 5, listener);

        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("location2",mLat+"");
                addNotification(mLat+"",mLon+"");
                new SendCoordinatesTask(getApplicationContext(), PreferenceUtils.getUserId(getApplication()), mLat+"",mLon+"").execute(new Void[]{});
            }
        };
        mTimer.schedule(timerTask, 10 * 1000, 10 * 1000);

        return START_STICKY;
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);


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

    private void addNotification(String lat, String lon){
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags (Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(new Date().toString()+" ["+lat+", "+lon+"]");
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentIntent(pintent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(listener);
        listener = null;
        mTimer.cancel();
        super.onDestroy();
    }


}
