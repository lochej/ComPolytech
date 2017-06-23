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

    public FileItem(int type, File file) {
        this.type=type;
        this.file = file;

    }

    public String getTitle() {
        return file.getName();
    }


    public int getIconID() {

        switch(type){
            case Constants.TYPE_VIDEO:
            return R.drawable.ic_video_library_black_24dp;
            case Constants.TYPE_IMAGE:
                return R.drawable.ic_image_black_24dp;
            case Constants.TYPE_PDF:
                return R.drawable.ic_picture_as_pdf_black_24dp;
        }

        return 0;
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
        file=(File) in.readSerializable();
        type=in.readInt();
        thumbnailImage=Bitmap.CREATOR.createFromParcel(in);
    }
}
