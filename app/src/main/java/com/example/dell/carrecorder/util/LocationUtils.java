package com.example.dell.carrecorder.util;

import android.content.ContentValues;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;

import java.io.File;

public class LocationUtils {

    public Location myLocation;
    private Context myContext;
    private double latitude;
    private double longitude;
    private String TAG = "CarcorderDemo/LocationUtils";
    public LocationUtils(Context context){
        myContext = context;
    }
    public Location getMyLocation(){
        LocationManager manager = (LocationManager)myContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String providerName = manager.getBestProvider(criteria, true);
        Log.d("LocationUtils",TAG+"providerName"+providerName);
        //manager.requestLocationUpdates(providerName,1000,0,mLocationListener);
        if(providerName!=null){
            myLocation = manager.getLastKnownLocation(providerName);
//			if(myLocation==null){
//                myLocation = manager.getLastKnownLocation(providerName);
//			}
            Log.d("LocationUtils",TAG+"myLoction = "+myLocation);
            if(myLocation != null){
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude(); 
                Log.d("LocationUtils",TAG+"getMyLocation"+", latitude = "+latitude+", longitude = "+longitude);
            }
        }
		//manager.removeUpdates(mLocationListener);
        return myLocation;
    }
    public LocationListener mLocationListener = new LocationListener(){

        @Override
	public void onLocationChanged(Location location) {
	// TODO Auto-generated method stub
	    myLocation = location;
	    if(location != null){
	        latitude = location.getLatitude();
		longitude = location.getLongitude(); 
	        Log.d("LocationUtils",TAG+"location"+", latitude = "+latitude+", longitude = "+longitude);
	    }
        }

	@Override
	public void onProviderDisabled(String arg0) {
	    // TODO Auto-generated method stub
			
	}

        @Override
	public void onProviderEnabled(String arg0) {
	    // TODO Auto-generated method stub
			
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	      // TODO Auto-generated method stub
			
	}};
    public void saveLocationInfoDatabase(String path){
        if(path==null && path.isEmpty()){
	    return;
         }
        Log.d("LocationUtils",TAG+"SaveLocationInfoDatabase"+", path = "+path);
	Uri uri = null;
	File imageFile = new File(path);
		
        long dataSize = imageFile.length();
	String[] pathSplit = path.split("/");
	String fileName = pathSplit[pathSplit.length - 1];
	String title = fileName.split("\\.")[0];
	ContentValues values = new ContentValues();
	values.put(Images.Media.TITLE, title);
	values.put(Images.Media.DISPLAY_NAME, fileName);
	values.put(Images.Media.DATA, path);
	values.put(Images.Media.LATITUDE, latitude);
	values.put(Images.Media.LONGITUDE, longitude);
	uri = myContext.getContentResolver().insert(
                  Images.Media.EXTERNAL_CONTENT_URI, values);
	}
}
