package com.polytech.communicationpolytech;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import java.io.File;
import java.io.FilterInputStream;
import java.io.Serializable;

/**
 * Created by Jérémy on 21/04/2017.
 * Cette classe definit l'objet fileItem a afficher dans le recyclerview
 * doit gerer les fichiers PDF, PNG, et Video
 */

public class FileItem implements Parcelable {

    String title;
    int iconID;
    File file;
    //View.OnClickListener listener;
    Bitmap thumbnailImage;
    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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

    /*
    public View.OnClickListener getOnClickListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }
    */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(iconID);
        dest.writeSerializable(file);
        dest.writeInt(type);
        if(thumbnailImage!=null){
            thumbnailImage.writeToParcel(dest,flags);
        }


    }

    public static final Parcelable.Creator<FileItem> CREATOR
            = new Parcelable.Creator<FileItem>() {
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };

    private FileItem(Parcel in) {
        title = in.readString();
        iconID = in.readInt();
        file=(File) in.readSerializable();
        type=in.readInt();
        thumbnailImage=Bitmap.CREATOR.createFromParcel(in);
    }
}
