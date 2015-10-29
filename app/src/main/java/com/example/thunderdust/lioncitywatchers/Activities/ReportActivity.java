package com.example.thunderdust.lioncitywatchers.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.thunderdust.lioncitywatchers.Exceptions.ViewNotFoundException;
import com.example.thunderdust.lioncitywatchers.Media.AlbumStorageDirFactory;

import com.example.thunderdust.lioncitywatchers.Media.BaseAlbumDirFactory;
import com.example.thunderdust.lioncitywatchers.Media.FroyoAlbumDirFactory;
import com.example.thunderdust.lioncitywatchers.R;
import com.example.thunderdust.lioncitywatchers.Utils.DeviceStatusValidator;
import com.example.thunderdust.lioncitywatchers.Utils.Toaster;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Device management */
    private DeviceStatusValidator mValidator = null;
    //UI components
    private EditText mIncidentDescriptionET;
    private ImageView mIncidentImageView;
    private FloatingActionButton mCameraButton;
    private FloatingActionButton mGalleryButton;
    private FloatingActionButton mShareButton;
    private FloatingActionButton mDiscardButton;
    private FloatingActionButton mPostButton;
    private Toaster mToaster;

    /* Camera and photo */
    private static final String PHOTO_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_CHOOSE_FROM_GALLERY = 2;
    private static final String ALBUM_NAME = "LionCityWatchers";
    private static final String INSTAGRAM_SHARE_TYPE_IMAGE = "image/*";
    private AlbumStorageDirFactory mAlnumStorageDirFactory = null;
    private String mCurrentPhotoPath = null;
    private Bitmap mReportBitmap = null;

    /* Debug Settings */
    private static final String DEBUG_TAG = "LionCityWathcers";
    private static final String ACTIVITY_TAG = "ReportActivity";

    /* Google Geo Api */
    private GoogleApiClient mGoogleApiClient;
    // Bool to track whether the app is already resolving an error
    private boolean mIsResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR_TAG = "DIALOG_ERROR_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        // Validator must be initialized before widgets
        initializeHelpers();
        initializeWidgets();
        initializeGoogleApiClient();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        // Recycle bitmap
        recycleBitmap(mReportBitmap);
        mIncidentImageView.setImageBitmap(null);
        mCurrentPhotoPath = null;
        // Disconnet and reset google API client
        if ( mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Open connection only when location service is called
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void initializeGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                              .addApi(Places.GEO_DATA_API)
                                              .addApi(Places.PLACE_DETECTION_API)
                                              .addConnectionCallbacks(this)
                                              .addOnConnectionFailedListener(this).build();
    }

    private void initializeHelpers(){
        mValidator = DeviceStatusValidator.getInstance();
    }

    private void initializeWidgets(){
        mIncidentDescriptionET = (EditText) findViewById(R.id.report_description);
        mIncidentDescriptionET.clearFocus();
        mIncidentImageView = (ImageView) findViewById(R.id.report_image_view);
        mCameraButton = (FloatingActionButton) findViewById(R.id.btn_camera);
        mGalleryButton = (FloatingActionButton) findViewById(R.id.btn_gallery);
        mShareButton = (FloatingActionButton) findViewById(R.id.btn_report_share);
        mDiscardButton = (FloatingActionButton) findViewById(R.id.btn_report_discard);
        mPostButton = (FloatingActionButton) findViewById(R.id.btn_report_submit);
        mToaster = Toaster.getInstance();
        if (mValidator.isDeviceUpdatedTo(Build.VERSION_CODES.FROYO)){
            mAlnumStorageDirFactory = new FroyoAlbumDirFactory();
        }
        else {
            mAlnumStorageDirFactory = new BaseAlbumDirFactory();
        }

        if (mCameraButton!=null){
            mCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchCameraIntent();
                }
            });
        }

        if(mGalleryButton!=null){
            mGalleryButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                }
            });
        }

        if(mShareButton!=null){
            mShareButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    shareToSNS();
                }
            });
        }

        if(mDiscardButton!=null){
            mDiscardButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ReportActivity.this.finish();
                    // alert report post discarded
                }
            });
        }

        if(mPostButton!=null){
            mPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    /***************** Camera related methods ************************/
    private File generatePhotoFile() throws IOException {
        // Name based on timestamp
        String timeStamp = new SimpleDateFormat(PHOTO_TIMESTAMP_FORMAT).format(new Date());
        String imageName = JPEG_FILE_PREFIX + "_" + timeStamp;
        File albumDirectory = getAlbumDirectory();
        File imageFile = File.createTempFile(imageName, JPEG_FILE_SUFFIX, albumDirectory);
        return imageFile;
    }

    private File getAlbumDirectory(){
        File albumDir = null;
        if(mValidator.isDiskMounted()){
            albumDir = mAlnumStorageDirFactory.getAlbumStorageDir(ALBUM_NAME);
            // Create image album directory if it has not existed
            if (albumDir!=null){
                if (!albumDir.mkdirs()){
                    if (!albumDir.exists()){
                        Log.d(DEBUG_TAG, "Failed to create album directory");
                        return null;
                    }
                }
            }
        }
        else {
            Log.v(ACTIVITY_TAG, "External storage is NOT mounted");
        }
        return albumDir;
    }

    private File setUpImageFile() throws IOException {
        File imageFile = generatePhotoFile();
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.d(DEBUG_TAG, "__________________________________PhotoPath: " + mCurrentPhotoPath);
        return imageFile;
    }

    // According to mCurrentPhotoPath, return the image file
    private File getCurrentPhoto() {
        return new File(mCurrentPhotoPath);
    }

    private void addPhotoToGallery() throws Exception {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File photoFile = getCurrentPhoto();
        if (photoFile!=null){
            Uri contentUri = Uri.fromFile(photoFile);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
        else
            throw new Exception("Current photo does not exist");
    }

    private void updateIncidentImageView() throws ViewNotFoundException {

        if(mIncidentImageView!=null){
            /* Get image view dimensions */
            int viewWidth = mIncidentImageView.getWidth();
            int viewHeight = mIncidentImageView.getHeight();

            /* Get image size */
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            /* Read dimensions and type of image data prior to construction and memory allocation to load large image efficiently */
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int imageWidth = bmOptions.outWidth;
            int imageHeight = bmOptions.outHeight;

            /* Compute scaling */
            int scaleIndex = 1;
            // In case image view is not initialized
            if (viewHeight > 0 || viewWidth > 0){
                scaleIndex = Math.min(imageHeight/viewHeight, imageWidth/viewWidth);
            }
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleIndex;
            /* May lead to frame lost
            if (!mValidator.isDeviceUpdatedTo(Build.VERSION_CODES.LOLLIPOP)){
                // Deprecated after lollipop
                bmOptions.inPurgeable = true;
            }*/

            recycleBitmap(mReportBitmap);
            mReportBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            //Update incident image view
            mIncidentImageView.setImageBitmap(mReportBitmap);
            mIncidentImageView.setVisibility(View.VISIBLE);
        } else {
            throw new ViewNotFoundException("Incident image view does not exist.");
        }
    }

    // For VERSIONS before honeycomb, bitmap recycle is necessary
    private void recycleBitmap(Bitmap bm){
        if(mValidator!=null){

            if (!mValidator.isDeviceUpdatedTo(Build.VERSION_CODES.HONEYCOMB)){
                if (bm!=null){
                    bm.recycle();
                }
            }
        }
    }


    private void dispatchCameraIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpImageFile();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(cameraIntent, ACTION_TAKE_PHOTO);
    }

    private void handleCameraPhoto(){
        if(mCurrentPhotoPath!=null){
            try{
                updateIncidentImageView();
                addPhotoToGallery();
                mCurrentPhotoPath = null;
            }
            catch (ViewNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case ACTION_CHOOSE_FROM_GALLERY:{
                break;
            }
            case ACTION_TAKE_PHOTO:{
                if (resultCode == RESULT_OK){
                    Log.d(DEBUG_TAG, "Ready to process image");
                    handleCameraPhoto();
                }
                break;
            }
            default:{
                break;
            }
        }
    }

    /* Sharing post to SNS */
    private void shareToSNS(){

        if(mCurrentPhotoPath!=null){
            File f = new File(mCurrentPhotoPath);
            Uri uri = Uri.fromFile(f);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(INSTAGRAM_SHARE_TYPE_IMAGE);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            //Broadcase the intent
            startActivity(Intent.createChooser(share, "Share this report to"));
        }
        else {
            // No current image
            mToaster.ToastLongCenter(getBaseContext(), "No image available to share");
        }
    }

    private boolean postNewReport(){
        return false;
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
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
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
        dialogFragment.show(this.getFragmentManager(), DIALOG_ERROR_TAG);
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mIsResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
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
            ((ReportActivity) getActivity()).onDialogDismissed();
        }
    }
}
