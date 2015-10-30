package com.example.thunderdust.lioncitywatchers.Media;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by weiran.liu on 10/30/2015.
 * A <Singleton> class to find real path of image file by its URI
 * As this finder shall be utilized universally for all activities,
 * it's best to keep a singleton class and used it for all activities.
 */
public class ImagePathFinder {

    private static ImagePathFinder mFinder = null;

    private ImagePathFinder() {
    }

    public static ImagePathFinder getInstance(){
        if (mFinder==null){
            mFinder = new ImagePathFinder();
        }
        return mFinder;
    }

    @TargetApi(19)
    public String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public String getRealPathFromURI_API11To18 (Context context, Uri contentUri){


        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    contentUri, proj, null, null, null);
            cursor = cursorLoader.loadInBackground();

            if (cursor != null) {
                int column_index =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
            }
            return result;
        } finally {
            if (cursor!=null){
                cursor.close();
            }
        }
    }

    public String getRealPathFromURI_APIBefore11(Context context, Uri contentUri){
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index
                    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor!=null){
                cursor.close();
            }
        }
    }








}
