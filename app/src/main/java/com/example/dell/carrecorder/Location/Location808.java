package com.example.dell.carrecorder.Location;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.azhon.jtt808.JTT808Manager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class Location808 {
    static final String TAG = "Location808";
    public Context context;
    public LocationManager locationManager;
    public static final int UPDATE_LOCATION = 1;
    public static final int UPDATE_LOCATION_SECOND = 2;
    private Location location;
    private  OnLocationListener listener;
    private boolean mRecordLocation = false;
    private long preTime = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage( Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LOCATION:
                    Log.d(TAG, "UPDATE_LOCATION");
                    handler.removeMessages(UPDATE_LOCATION_SECOND);
                    location = getCurrentLocation();
                    updateLocation(location);
                    handler.sendEmptyMessageDelayed(UPDATE_LOCATION,10000);
                    break;
                case UPDATE_LOCATION_SECOND:
                    Log.d(TAG, "UPDATE_LOCATION_SECOND");
                    location = getCurrentLocation();
                    updateLocation(location);
                    //handler.sendEmptyMessageDelayed(UPDATE_LOCATION,10000);
                    break;
            }
        }
    };

    public Location808 setContext(Context context) {
        this.context = context;
        return this;
    }

    private static Location808 manager = new Location808();

    public static Location808 getInstance() {
        return manager;
    }

    private void setTime(long when){
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
    }

  /*  public void heartAlive(boolean immediately){
        if((System.currentTimeMillis() - preTime) < 1000*10 && !immediately){
            return;
        }
        Log.d(TAG, "heartAlive");
        preTime = System.currentTimeMillis();
        recordLocation(true);
        handler.sendEmptyMessageDelayed(UPDATE_LOCATION_SECOND,10000);
    }*/

    /* 获取定位方法 */
    public void getLocation() {
        //1.获取系统LocationManager服务
        Log.d(TAG, "onLocationChanged");
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，中精度高精度获取不到location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

        // String locationProvider = locationManager.getBestProvider(criteria, true);
        //2.获取GPS最近的定位信息
         location = getLastKnownLocation();

        //3.将location里面的位置信息展示在edittext中
        updateLocation(location);
        //4.设置没10秒获取一次GPS的定位信息
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //当GPS定位信息发生改变时，更新位置
                Log.d("onLocation", "onLocationChanged");
                updateLocation(location);

                setTime(location.getTime());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("onLocation", "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                //当GPS LocationProvider可用时，更新位置
                Log.d("onLocation", "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d("onLocation", "onProviderDisabled");
                updateLocation(null);

            }
        });*/


        recordLocation(true);
        handler.removeMessages(UPDATE_LOCATION);
        handler.sendEmptyMessageDelayed(UPDATE_LOCATION,10000);
    }

    public void stopLocation(){
        recordLocation(false);
    }

    private double preLatitude,preLongitude;

    public void RestartUpdateGPS(){
        Log.d(TAG, "RestartUpdateGPS");
        recordLocation(true);
        handler.removeMessages(UPDATE_LOCATION_SECOND);
        handler.sendEmptyMessageDelayed(UPDATE_LOCATION_SECOND,10000);
    }
    private double distance;
    public void updateLocation(Location location) {
        if (location != null) {
            /*StringBuffer sb = new StringBuffer();
            sb.append("实时的位置信息：\n经度：");
            sb.append(location.getLongitude());
            sb.append("\n纬度：");
            sb.append(location.getLatitude());
            sb.append("\n高度：");
            sb.append(location.getAltitude());
            sb.append("\n速度：");
            sb.append(location.getSpeed());
            sb.append("\n方向：");
            sb.append(location.getBearing());
            sb.append("\n精度：");
            sb.append(location.getAccuracy());*/
            //Log.d(TAG, sb.toString());
            if(preLatitude!=0&&preLongitude!=0) {
                //distance = MapHelper.distance(preLatitude, preLongitude, location.getLatitude(), location.getLongitude());
                //long duration = System.currentTimeMillis() - preTime;
                //Log.d(TAG, "distance:"+ distance + ";duration:" + duration + ";sppeed(km/h):" + distance*60*60*1000/duration + ";gps_speed(km/h):" + location.getSpeed()*3.6f);
                //if(distance!=0&&distance<340){
                    //Log.d(TAG, "distance callback:~~~:  "+distance);
                    listener.callBackLocation(location.getLatitude(),location.getLongitude(),location.getSpeed()*3.6f*10);
                //}
            }else {
                Log.d(TAG, "distance callback :!!!:  "+distance);
                listener.callBackLocation(location.getLatitude(),location.getLongitude(),0);
            }
            preLatitude = location.getLatitude();
            preLongitude = location.getLongitude();
            //preTime = System.currentTimeMillis();
        } else {
            Log.d(TAG, "null");
        }

    }
    private void checkLocaltionOK(double latitude,double longitude){

    }
    private Location getLastKnownLocation() {
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    public Location808 setListener(OnLocationListener listener) {
        this.listener = listener;
        return this;
    }
    public interface OnLocationListener {
        void callBackLocation(double latitude,double longitude,float speed);// 点击保存回调主界面
    }

    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(android.location.LocationManager.GPS_PROVIDER),
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER)
    };

    public Location getCurrentLocation() {
        if (!mRecordLocation) return null;
        // go in best to worst order
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            if (l != null) return l;
        }
        Log.d(TAG, "No location received yet. restart location");
        locationManager = null;
        stopReceivingLocationUpdates();
        startReceivingLocationUpdates();
        return null;
    }

    public void recordLocation(boolean recordLocation) {
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (recordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    private void startReceivingLocationUpdates() {
        if (locationManager == null) {
            locationManager = (android.location.LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        1000,
                        0.1F,
                        mLocationListeners[1]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            try {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000,
                        0.1F,
                        mLocationListeners[0]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            Log.d(TAG, "startReceivingLocationUpdates");
        }
    }

    private void stopReceivingLocationUpdates() {
        if (locationManager == null) {
            locationManager = (android.location.LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
        }

        if (locationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
            Log.d(TAG, "stopReceivingLocationUpdates");
        }
    }
    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }

            if (!mValid) {
                Log.d("onLocation", "Got first location.");
            }
            mLastLocation.set(newLocation);
            mValid = true;
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;
                    break;
                }
            }
        }

        public Location current() {
            return mValid ? mLastLocation : null;
        }
    }

}
