package com.example.thunderdust.lioncitywatchers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;

public class ReportActivity extends AppCompatActivity {

    private EditText mIncidentDescriptionET;
    private ImageView mIncidentImageView;
    private Button mCameraButton;
    private Button mGalleryButton;
    private Button mShareButton;
    private Button mDiscardButton;
    private Button mPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        initializeWidgets();
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

    private boolean shareToInstagram(){
        return false;

    }

    private boolean postNewReport(){
        return false;
    }
}
