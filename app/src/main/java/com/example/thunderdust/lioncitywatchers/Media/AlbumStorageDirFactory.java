package com.example.thunderdust.lioncitywatchers.Media;

import java.io.File;

/**
 * Created by weiran.liu on 10/28/2015.
 */
public abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
