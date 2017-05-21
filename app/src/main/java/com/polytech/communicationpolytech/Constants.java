package com.polytech.communicationpolytech;

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

    String EXTENSION_PDF="PDF";
    String EXTENSION_MP4="MP4";
    String EXTENSION_PNG="PNG";
    String EXTENSION_JPG="JPG";
    String EXTENSION_GIF="GIF";

    String EXTRA_VIDEO_PATH="lien_vers_video";
    String EXTRA_VIDEO_MILLIS="state_video";
    String EXTRA_IMAGE_PATH="path_image";

    String PATH_POLYTECH="/Application Communication/Polytech Tours";
    String PATH_CANDIDAT="/Application Communication/Espace Candidat";
    String PATH_RESERVED="/Application Communication/Espace Réservé";
    String PATH_RESEAU="/Application Communication/Réseau Polytech";
}
