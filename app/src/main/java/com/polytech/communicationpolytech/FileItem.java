package com.polytech.communicationpolytech;

import android.graphics.Bitmap;
import android.view.View;

import java.io.File;

/**
 * Created by Jérémy on 21/04/2017.
 * Cette classe definit l'objet fileItem a afficher dans le recyclerview
 * doit gerer les fichiers PDF, PNG, et Video
 */

public class FileItem {

    String title;
    int iconID;
    File file;
    View.OnClickListener listener;
    Bitmap thumbnailImage;

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(Bitmap thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public FileItem(String title, int iconID, File file) {
        this.title = title;
        this.iconID = iconID;
        this.file = file;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public View.OnClickListener getOnClickListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
