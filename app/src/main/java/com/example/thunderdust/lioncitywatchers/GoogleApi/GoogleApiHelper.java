package com.example.thunderdust.lioncitywatchers.GoogleApi;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

/**
 * Created by thunderdust on 30/10/15.
 */
public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Context mContext;
    Activity mActivity;
    /* Google Geo Api */
    GoogleApiClient mGoogleApiClient;
    // Bool to track whether the app is already resolving an error
    boolean mIsResolvingError = false;
    // Request code to use when launching the resolution activity
    static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    static final String DIALOG_ERROR_TAG = "DIALOG_ERROR_TAG";

    public GoogleApiHelper(Context c, Activity a){
        this.mContext = c;
        this.mActivity = a;
        checkPlayServices();
        checkLocationServices();
        buildGoogleApiClient();
        connect();
        locationServicesHelper = new LocationServicesHelper(this);
        geofenceHelper = new GeofenceHelper(context);

    }

    private void initializeGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    /* Implementing Google Api Client Connection Handle interfaces */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
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
            showErrorDialog(connectionResult.getErrorCode());
            mIsResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR_TAG, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(mActivity.getFragmentManager(), DIALOG_ERROR_TAG);
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mIsResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR_TAG);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            onDialogDismissed();
        }
    }

}
