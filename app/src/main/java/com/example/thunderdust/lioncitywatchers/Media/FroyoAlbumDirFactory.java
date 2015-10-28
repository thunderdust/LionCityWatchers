package com.example.thunderdust.lioncitywatchers.Media;

import android.os.Environment;

import java.io.File;

/**
 * Created by thunderdust on 28/10/15.
 */
public class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}
