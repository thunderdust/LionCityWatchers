package com.example.thunderdust.lioncitywatchers.GoogleApi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.LocationListener;

/**
 * Created by thunderdust on 30/10/15.
 */
public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Context mContext;
    Activity mActivity;
    /* Google Geo Api */
    GoogleApiClient mGoogleApiClient;

    // Bool to track whether the app is already resolving an error
    boolean mIsResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Request code to use when display error from google play service
    private static final int REQUEST_DISPLAY_ERROR = 1001;
    // Unique tag for the error dialog fragment
    static final String DIALOG_ERROR_TAG = "DIALOG_ERROR_TAG";
    private static final int PLAY_SERVICES_REQUEST_CODE = 10022;
    private static final String DEBUG_TAG = "Google Api Helper";

    public GoogleApiHelper(Activity a){
        //this.mContext = c;
        this.mActivity = a;
        isPlayServicesSupported();
        checkLocationServices();
        initializeGoogleApiClient();
        clientConnect();
        //locationServicesHelper = new LocationServicesHelper(this);
        //geofenceHelper = new GeofenceHelper(context);

    }

    protected synchronized void initializeGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    private boolean isPlayServicesSupported(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity, PLAY_SERVICES_REQUEST_CODE).show();
            } else {
            }
            Log.d(DEBUG_TAG, "Play Services is not supported on this device");
            return false;
        }
        return true;
    }

    private void checkLocationServices(){
        LocationManager lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // Build Alert dialog
            AlertDialog.Builder ab = new AlertDialog.Builder(mActivity);
            ab.setTitle("Location Service Not Active");
            ab.setMessage("Please turn on location services and GPS.");
            ab.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface di, int i) {
                    // Open location settings
                    Intent toSetLocationServiceIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mActivity.startActivity(toSetLocationServiceIntent);
                }
            });
            Log.d(DEBUG_TAG,"Location services not enabled");
        }
        else{
        }
    }

    public void clientConnect(){
        if (mGoogleApiClient!=null && !mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    public void clientDisconnect(){
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isClientConnected(){
        if (mGoogleApiClient!=null){
            return mGoogleApiClient.isConnected();
        } else{
            return false;
        }
    }

    /* Implementing Google Api Client Connection Handle interfaces */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(DEBUG_TAG, "Client is connected");
        //locationServicesHelper.startLocationUpdates(mGoogleApiClient);
        //geofenceHelper.addGeofenceApi(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(DEBUG_TAG, "Connection suspended, try to reconnect");
        // Disable location UI components
        // Until onConnected() is called
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mIsResolvingError){
            // Already attempting to resolve an error
            return;
        } else if (connectionResult.hasResolution()){
            try {
                mIsResolvingError = true;
                connectionResult.startResolutionForResult(mActivity, REQUEST_RESOLVE_ERROR);
            }
            catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity,connectionResult.getErrorCode(),REQUEST_DISPLAY_ERROR).show();
            mIsResolvingError = true;
        }
    }

    public Location getLastKnownLocation() {
        Location location = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        return location;
    }
}
