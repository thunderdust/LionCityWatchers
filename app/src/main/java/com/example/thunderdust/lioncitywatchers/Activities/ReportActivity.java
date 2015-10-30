package com.example.thunderdust.lioncitywatchers.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thunderdust.lioncitywatchers.Exceptions.ViewNotFoundException;
import com.example.thunderdust.lioncitywatchers.GoogleApi.GoogleApiHelper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class ReportActivity extends Activity  {

    /* Device management */
    private DeviceStatusValidator mValidator = null;
    //UI components
    private EditText mIncidentDescriptionET;
    private TextView mLocationTextView;
    private ImageView mIncidentImageView;
    private Button mCameraButton;
    private Button mGalleryButton;
    private Button mLocationButton;
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

    /* Location */
    private GoogleApiHelper mGoogleApiHelper = null;
    private Location mCurrentLocation = null;

    /* Debug Settings */
    private static final String DEBUG_TAG = "LionCityWathcers";
    private static final String ACTIVITY_TAG = "ReportActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        // Validator must be initialized before widgets
        initializeHelpers();
        initializeWidgets();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        Log.d(DEBUG_TAG, "Destroy activity");
        // Recycle bitmap
        recycleBitmap(mReportBitmap);
        mIncidentImageView.setImageBitmap(null);
        mCurrentPhotoPath = null;
        // Disconnet and reset google API client
        mGoogleApiHelper.clientDisconnect();
        mGoogleApiHelper = null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiHelper.clientConnect();
    }

    @Override
    protected void onStop(){
        mGoogleApiHelper.clientDisconnect();
        super.onStop();
    }

    private void initializeHelpers(){

        mValidator = DeviceStatusValidator.getInstance();
        // GoogleApiClient is automatically connect when create new instance of GoogleApiHelper
        mGoogleApiHelper = new GoogleApiHelper(this);
    }

    private void initializeWidgets(){

        mIncidentDescriptionET = (EditText) findViewById(R.id.report_description);
        mIncidentDescriptionET.clearFocus();
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mIncidentImageView = (ImageView) findViewById(R.id.report_image_view);
        mCameraButton = (Button) findViewById(R.id.btn_camera);
        mGalleryButton = (Button) findViewById(R.id.btn_gallery);
        mLocationButton = (Button) findViewById(R.id.btn_location);
        mShareButton = (FloatingActionButton) findViewById(R.id.btn_report_share);
        mDiscardButton = (FloatingActionButton) findViewById(R.id.btn_report_discard);
        mPostButton = (FloatingActionButton) findViewById(R.id.btn_report_submit);
        mToaster = Toaster.getInstance();

        // Determine album directory based on Android version
        if (mValidator.isDeviceUpdatedTo(Build.VERSION_CODES.FROYO)){
            mAlnumStorageDirFactory = new FroyoAlbumDirFactory();
        }
        else {
            mAlnumStorageDirFactory = new BaseAlbumDirFactory();
        }

        if (mCameraButton!=null){
            mCameraButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchCameraIntent();
                }
            });
        }

        if(mGalleryButton!=null){
            mGalleryButton.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                }
            });
        }

        if(mLocationButton!=null){
            mLocationButton.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mCurrentLocation = mGoogleApiHelper.getLastKnownLocation();
                    if (mCurrentLocation!=null){
                        Log.d(DEBUG_TAG, "Location obtained, altitude: "+ mCurrentLocation.getAltitude() + "latitude: " + mCurrentLocation.getAltitude());
                        updateLocationTextView();
                    } else{
                        Log.d(DEBUG_TAG, "Failed to get current location");
                    }
                }
            });
        }

        if(mShareButton!=null){
            mShareButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Log.d(DEBUG_TAG,"_____________SHARE TO SNS");
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
                    boolean isReportSubmitted = postNewReport();
                    if (isReportSubmitted){
                        mToaster.ToastLongCenter(getBaseContext(), "Report Submitted!");
                    }
                    else{
                        mToaster.ToastLongCenter(getBaseContext(), "Report Submission Failed!");
                    }
                }
            });
        }
    }

    private Location getCurrentLocation(){
        if(mGoogleApiHelper!=null && mGoogleApiHelper.isClientConnected()){
            return mGoogleApiHelper.getLastKnownLocation();
        }
        else{
            return null;
        }
    }


    /***************** BEGIN Camera related methods ************************/
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
        Log.d(DEBUG_TAG, "______________PhotoPath: " + mCurrentPhotoPath);
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
                //mCurrentPhotoPath = null;
            }
            catch (ViewNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /************** END Camera related methods ************************/

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
            // Copy description to clipboard
            if (mIncidentDescriptionET!=null){
                String description = mIncidentDescriptionET.getText().toString();
                if (description!= null && !description.isEmpty()){
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("description", description);
                    cm.setPrimaryClip(clip);
                    mToaster.ToastLongBottom(this, "Incident description has been copied to clipboard.");
                }
            }
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
        // Check for input fields: description and location are compulsory, photo is optional
        boolean isValidPost = true;
        boolean isPostWithPicture = false;
        boolean shouldContinueCheck = false;
        ArrayList<String> requiredFields = new ArrayList<String>();
        String description;
        // Check description text
        if (mIncidentDescriptionET!=null){
            description = mIncidentDescriptionET.getText().toString();
            if (description!=null && !description.isEmpty()){
                // Valid description
                shouldContinueCheck = true;
            }
            else {
                isValidPost = false;
                requiredFields.add("Incident Description");
                shouldContinueCheck = false;
            }
        }
        // Check location
        if (shouldContinueCheck){
            if (mCurrentLocation!=null){
                shouldContinueCheck = true;
            }
            else {
                isValidPost = false;
                shouldContinueCheck = false;
                requiredFields.add("Incident Location");
            }
        }
        // Check photo
        if (shouldContinueCheck){
            if (mCurrentPhotoPath!=null){
                isPostWithPicture = true;
            }
        }
        // Start to post
        if (isValidPost){

        }
        // Invalid post: Display error message and cancel post
        else {
            Iterator<String> iterator = requiredFields.iterator();
            String message = null;
            while(iterator.hasNext()){
                message += iterator.next() + " ";
            }
            message = "Invalid post request, missing " + message;
            mToaster.ToastLongCenter(this, message);
            return false;
        }
        return false;
    }

    private void updateLocationTextView() {
        if (mLocationTextView!=null && mCurrentLocation!=null){
            String locationDescription = "Current Location: " + mCurrentLocation.getAltitude() + ", " + mCurrentLocation.getLatitude();
            mLocationTextView.setText(locationDescription);
        }
    }
}
