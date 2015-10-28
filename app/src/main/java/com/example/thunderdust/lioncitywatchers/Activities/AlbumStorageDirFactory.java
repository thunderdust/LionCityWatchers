package com.example.thunderdust.lioncitywatchers.Activities;

import java.io.File;

/**
 * Created by weiran.liu on 10/28/2015.
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
