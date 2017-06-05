package com.polytech.communicationpolytech;

import java.io.File;
import java.util.Comparator;

/**
 * Created by Jérémy on 12/04/2017.
 */

interface Constants {



    int passHash=1253867675;

    int TYPE_PDF=0;
    int TYPE_IMAGE=1;
    int TYPE_VIDEO=2;
    int TYPE_FOLDER=3;
    int TYPE_UNKNOWN=4;
    int TYPE_SEPARATOR=5;

    int WHAT_PREEXECUTE=-1;
    int WHAT_IMAGELOADED=-2;
    int WHAT_CANCEL=-3;
    int WHAT_LOADING=-4;

    String EXTENSION_PDF="PDF";
    String EXTENSION_MP4="MP4";
    String EXTENSION_PNG="PNG";
    String EXTENSION_JPG="JPG";
    String EXTENSION_GIF="GIF";

    String EXTRA_VIDEO_PATH="lien_vers_video";
    String EXTRA_VIDEO_MILLIS="state_video";
    String EXTRA_IMAGE_PATH="path_image";

    String PATH_POLYTECH="/Application Communication/Polytech Tours";
    String PATH_CANDIDAT="/Application Communication/Espace candidat";
    String PATH_RESERVED="/Application Communication/Espace reserve";
    String PATH_RESEAU="/Application Communication/Reseau Polytech";

    String CSV_FILENAME="formulaire.csv";

    Comparator<File> alphaComp=new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };




}
