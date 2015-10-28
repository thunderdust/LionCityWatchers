package com.example.thunderdust.lioncitywatchers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;

import com.example.thunderdust.lioncitywatchers.Exceptions.ViewNotFoundException;
import com.example.thunderdust.lioncitywatchers.R;
import com.example.thunderdust.lioncitywatchers.Utils.DeviceStatusValidator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends Activity {

    //Device management
    private DeviceStatusValidator mValidator = null;
    //UI components
    private EditText mIncidentDescriptionET;
    private ImageView mIncidentImageView;
    private Button mCameraButton;
    private Button mGalleryButton;
    private Button mShareButton;
    private Button mDiscardButton;
    private Button mPostButton;

    //Camera and photo
    private static final String PHOTO_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String ALBUM_NAME = "LionCityWatchers";
    private AlbumStorageDirFactory mAlnumStorageDirFactory = null;
    private String mCurrentPhotoPath = null;

    // Debug Settings
    private static final String DEBUG_TAG = "LionCityWathcers";
    private static final String ACTIVITY_TAG = "ReportActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        initializeWidgets();
        initializeHelpers();
    }

    private void initializeHelpers(){
        mValidator = DeviceStatusValidator.getInstance();
    }

    private void initializeWidgets(){
        mIncidentDescriptionET = (EditText) findViewById(R.id.report_description);
        mIncidentImageView = (ImageView) findViewById(R.id.report_image_view);
        mCameraButton = (Button) findViewById(R.id.btn_camera);
        mGalleryButton = (Button) findViewById(R.id.btn_gallery);
        mShareButton = (Button) findViewById(R.id.btn_report_share);
        mDiscardButton = (Button) findViewById(R.id.btn_report_discard);
        mPostButton = (Button) findViewById(R.id.btn_report_submit);

        if (mCameraButton!=null){
            mCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            mPostButton.setOnClickListener(new View.OnClickListener(){
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
            Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            //Update incident image view
            mIncidentImageView.setImageBitmap(bm);
            mIncidentImageView.setVisibility(View.VISIBLE);
        } else {
            throw new ViewNotFoundException("Incident image view does not exist.");
        }
    }













    private boolean shareToInstagram(){
        return false;

    }

    private boolean postNewReport(){
        return false;
    }
}
