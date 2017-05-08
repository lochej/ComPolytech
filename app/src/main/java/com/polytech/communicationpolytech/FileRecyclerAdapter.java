package com.polytech.communicationpolytech;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Jérémy on 07/04/2017.
 */

public class FileRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class PdfViewHolder extends RecyclerView.ViewHolder{

        final String TAG=getClass().getSimpleName();

        private TextView title;
        private TextView placeholder;
        private ImageView imgThumb,imgIcon;
        private FileItem currentItem;
        private View itemView;
        private LoadPDFThumbTask pdfThumbTask;

        public PdfViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            imgThumb = (ImageView) itemView.findViewById(R.id.card_video);
            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);
            placeholder = (TextView) itemView.findViewById(R.id.card_placeholder);
        }


        public void setData(FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            View.OnClickListener clickListener = currentItem.getOnClickListener();
            Bitmap thumbBitmap=currentItem.getThumbnailImage();

            itemView.setOnClickListener(clickListener);
            this.title.setText(title);

            imgIcon.setImageResource(iconid);


            //Si le bitmap est valide
            if(thumbBitmap!=null){
                //cacher le placeholder
                if(placeholder.getVisibility()==View.VISIBLE){
                    placeholder.setVisibility(View.GONE);
                }
                //charger l'image dans l'imageview
                imgThumb.setImageBitmap(thumbBitmap);
                Log.d(TAG,"recycled Bitmap");
            }
            //Sinon on lance le chargement
            else{
                //Chargement de la miniatur
                pdfThumbTask=new LoadPDFThumbTask(itemView.getContext(),this,currentItem.getFile());
                pdfThumbTask.execute();

                Log.d(TAG,"Generating Bitmap");
            }

        }

        public LoadPDFThumbTask getPdfThumbTask() {
            return pdfThumbTask;
        }

        public void setPdfThumbTask(LoadPDFThumbTask pdfThumbTask) {
            this.pdfThumbTask = pdfThumbTask;
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{


        final String TAG=getClass().getSimpleName();

        private TextView title;
        //private TextView placeholder;
        private ImageView imgThumb,imgIcon;
        private FileItem currentItem;
        private View itemView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            imgThumb = (ImageView) itemView.findViewById(R.id.card_video);
            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);
            //placeholder = (TextView) itemView.findViewById(R.id.card_placeholder);
        }


        public void setData(FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            View.OnClickListener clickListener = currentItem.getOnClickListener();
            final File imageFile=currentItem.getFile();

            Glide.with(itemView.getContext()).fromFile().asBitmap().load(imageFile).into(imgThumb);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewer=new Intent(v.getContext(),ImageViewerActivity.class);
                    viewer.putExtra(Constants.EXTRA_IMAGE_PATH,imageFile);

                    v.getContext().startActivity(viewer);
                }
            });
            this.title.setText(title);

            imgIcon.setImageResource(iconid);

        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{


        final String TAG=getClass().getSimpleName();

        private TextView title;
        //private TextView placeholder;
        private ImageView imgThumb,imgIcon;
        private VideoView videoView;
        private FileItem currentItem;
        private View itemView;
        private MediaController mediaController;
        private ImageButton fullscreen;
        private FloatingActionButton playFab;

        public VideoViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = (TextView) itemView.findViewById(R.id.title);

            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);

            videoView=(VideoView) itemView.findViewById(R.id.card_video);

            mediaController = new MediaController(itemView.getContext());

            fullscreen = (ImageButton) itemView.findViewById(R.id.card_fullscreen);

            playFab=(FloatingActionButton) itemView.findViewById(R.id.card_videoPlayFab);

            playFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoView.start();
                    playFab.setVisibility(View.GONE);
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playFab.setVisibility(View.VISIBLE);
                }
            });

            //videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(0);
                }
            });



        }


        public void setData(final FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            View.OnClickListener clickListener = currentItem.getOnClickListener();
            File videoFile=currentItem.getFile();

            /* Ajout du bouton fulllscreen
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    videoView.pause();
                    Intent videoPlayer=new Intent(videoView.getContext(),VideoViewerActivity.class);
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_MILLIS,videoView.getCurrentPosition());
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_PATH,currentItem.getFile().getAbsolutePath());

                    videoView.getContext().startActivity(videoPlayer);
                }
            });
            */

            fullscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoView.pause();
                    Intent videoPlayer=new Intent(videoView.getContext(),VideoViewerActivity.class);
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_MILLIS,videoView.getCurrentPosition());
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_PATH,currentItem.getFile());

                    //On eteint la video dans la card et on la lance dans l'activité donc on reset le playButton
                    playFab.setVisibility(View.VISIBLE);

                    videoView.getContext().startActivity(videoPlayer);
                }
            });


            this.title.setText(title);

            imgIcon.setImageResource(iconid);


            videoView.setVideoPath(videoFile.getAbsolutePath());






        }
    }

    public class SeparatorViewHolder extends RecyclerView.ViewHolder{


        final String TAG=getClass().getSimpleName();

        private TextView title;
        private FileItem currentItem;
        private View itemView;

        public SeparatorViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.separator_title);
        }


        public void setData(FileItem currentItem, int position) {

            this.currentItem=currentItem;
            String title=currentItem.getTitle();

            this.title.setText(title);

        }
    }

    public static class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup{

        FileRecyclerAdapter adapter;

        public SpanSizeLookup(FileRecyclerAdapter adapter) {
            this.adapter=adapter;
        }

        @Override
        public int getSpanSize(int position) {
            return (adapter.objectList.get(position).getType() == Constants.TYPE_VIDEO) || (adapter.objectList.get(position).getType() == Constants.TYPE_SEPARATOR) ? 2 : 1 ;
        }
    }

    public static Comparator<FileItem> fileItemComparator=new Comparator<FileItem>() {
        @Override
        public int compare(FileItem o1, FileItem o2) {

            int type1=o1.getType();
            int type2=o2.getType();
            /*
            if(type1==type2){
                return 0;
            }
            */
            //Remonte les vidéos en haut de la liste et laisse les autres inchang
            if(type1==Constants.TYPE_VIDEO){
                return -1;
            }
            if(type2==Constants.TYPE_VIDEO){
                return 1;
            }
            return 0;
        }
    };

    private List<FileItem> objectList;
    private LayoutInflater inflater;

    public FileRecyclerAdapter(Context context, List<FileItem> objectList) {
        this.objectList=objectList;
        Collections.sort(objectList,fileItemComparator);
        inflater=LayoutInflater.from(context);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        RecyclerView.ViewHolder holder;

        switch (viewType){
            case Constants.TYPE_IMAGE:

                view = inflater.inflate(R.layout.file_card_item,parent,false);
                holder=new ImageViewHolder(view);

                return holder;

            case Constants.TYPE_PDF:

                view = inflater.inflate(R.layout.file_card_item,parent,false);
                holder=new PdfViewHolder(view);

                return holder;

            case Constants.TYPE_VIDEO:

                view = inflater.inflate(R.layout.video_card_item,parent,false);
                holder=new VideoViewHolder(view);

                return holder;

            case Constants.TYPE_SEPARATOR:

                view = inflater.inflate(R.layout.titled_separator,parent,false);
                holder=new SeparatorViewHolder(view);

                return holder;

            default:
                return null;
        }

    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return objectList.get(position).getType();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        if(holder instanceof PdfViewHolder){
            //Set a white background during loading of the new Task
            PdfViewHolder pdfHolder=(PdfViewHolder) holder;
            pdfHolder.imgThumb.setImageDrawable(new ColorDrawable(ContextCompat.getColor(pdfHolder.imgThumb.getContext(),R.color.cardBackground)));
        }


    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        FileItem currentItem=objectList.get(position);

        switch (currentItem.getType()){
            case Constants.TYPE_IMAGE:

                ImageViewHolder imageHolder=(ImageViewHolder) holder;
                imageHolder.setData(currentItem,position);

                break;
            case Constants.TYPE_PDF:

                PdfViewHolder pdfHolder=(PdfViewHolder) holder;
                pdfHolder.setData(currentItem,position);

                break;
            case Constants.TYPE_VIDEO:

                VideoViewHolder videoHolder=(VideoViewHolder) holder;

                GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) videoHolder.itemView.getLayoutParams();


                videoHolder.itemView.setLayoutParams(layoutParams);

                videoHolder.setData(currentItem,position);

                break;

            case Constants.TYPE_SEPARATOR:

                SeparatorViewHolder separatorHolder=(SeparatorViewHolder) holder;

                GridLayoutManager.LayoutParams separatorlayoutParams = (GridLayoutManager.LayoutParams) separatorHolder.itemView.getLayoutParams();


                separatorHolder.itemView.setLayoutParams(separatorlayoutParams);

                separatorHolder.setData(currentItem,position);

                break;
        }

    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }



    /**
     * Genere la miniature dans un objet Bitmap à partir du fichier PDF donné dans pdfFile
     * @param context
     * @param pdfiumCore
     * @param pdfFile
     * @return
     * @throws IOException
     */
    public static Bitmap getPdfThumbnail(Context context, PdfiumCore pdfiumCore,File pdfFile,View intoView) throws IOException {

        ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(Uri.fromFile(pdfFile), "r");;
        int pageNum = 0;
        PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

        pdfiumCore.openPage(pdfDocument, pageNum);

        int pdfwidth = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
        int pdfheight = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);


        //int vwidth=intoView.getMeasuredWidth();
        //double temp = (1.0*pdfheight*vwidth) / vwidth;
        //int vheight=(int)temp;

        Bitmap bitmap = Bitmap.createBitmap(pdfwidth,pdfheight,
                Bitmap.Config.ARGB_8888);

        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0,
                pdfwidth, pdfheight);

        pdfiumCore.closeDocument(pdfDocument); // important!

        return bitmap;

    }

    /**
     * Charge la miniature dans une ImageView de manière asynchrone pour gagner en performance
     */
    public class LoadPDFThumbTask extends AsyncTask<Void,Void,Bitmap>{

        String TAG=getClass().getSimpleName();
        Context context;
        ImageView intoView;
        PdfViewHolder viewHolder;
        View placeHolder;
        File pdfFile;
        FileItem currentItem;
        PdfiumCore pdfiumCore;

        public LoadPDFThumbTask(Context context, PdfViewHolder viewHolder, File pdfFile) {
            this.context = context;
            this.intoView = viewHolder.imgThumb;
            this.viewHolder=viewHolder;
            this.pdfFile = pdfFile;
            this.placeHolder=viewHolder.placeholder;
            this.currentItem=viewHolder.currentItem;
            pdfiumCore=new PdfiumCore(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            placeHolder.setVisibility(View.VISIBLE);
            viewHolder.setIsRecyclable(false);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            Bitmap pdfthumb = currentItem.getThumbnailImage();
            //Si il y a deja une miniature on la reutilise tout de suite
            if(pdfthumb!=null){
                Log.d(TAG,"recycled thumb");
                return pdfthumb;
            }

            try {
                pdfthumb = getPdfThumbnail(context,pdfiumCore,pdfFile,intoView);
                //Log.d("LoadPDftask",pdfFile.getName());
                return pdfthumb;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            placeHolder.setVisibility(View.GONE);
            //Sauvegarde du Bitmap dans le cache
            currentItem.setThumbnailImage(bitmap);
            intoView.setImageBitmap(bitmap);
            viewHolder.setIsRecyclable(true);
        }
    }


}
