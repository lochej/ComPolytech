package com.polytech.communicationpolytech;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.number42.subsampling_pdf_decoder.PDFDecoder;
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP;


/**
 * Created by Jérémy on 07/04/2017.
 */

public class FileRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int WHAT_PREEXECUTE=-1;
    private static int WHAT_IMAGELOADED=-2;
    private static int WHAT_CANCEL=-3;
    private static int WHAT_LOADING=-4;

    public static View.OnClickListener getClickListener(final File fileToOpen, int type){


        switch (type){
            case Constants.TYPE_IMAGE:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewer=new Intent(v.getContext(),ImageViewerActivity.class);
                        viewer.putExtra(Constants.EXTRA_IMAGE_PATH,fileToOpen);

                        v.getContext().startActivity(viewer);
                    }
                };

            case Constants.TYPE_PDF:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setDataAndType(
                                Uri.parse("file://" + fileToOpen.getAbsolutePath()),
                                "application/pdf");

                        v.getContext().startActivity(intent);
                    }
                };

            case Constants.TYPE_VIDEO:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //videoView.pause();
                        //mediaPlayer.pause();
                        //textureVideoView.pause();
                        Intent videoPlayer=new Intent(v.getContext(),VideoViewerActivity.class);
                        //videoPlayer.putExtra(Constants.EXTRA_VIDEO_MILLIS,textureVideoView.getCurrentPosition());
                        videoPlayer.putExtra(Constants.EXTRA_VIDEO_MILLIS,0);
                        videoPlayer.putExtra(Constants.EXTRA_VIDEO_PATH,fileToOpen);

                        //On eteint la video dans la card et on la lance dans l'activité donc on reset le playButton
                        //playFab.setVisibility(View.VISIBLE);

                        v.getContext().startActivity(videoPlayer);
                    }
                };

                default:
                    return null;
        }
    }

    public class PdfViewHolder extends RecyclerView.ViewHolder{

        final String TAG=getClass().getSimpleName();

        private TextView title;
        private TextView placeholder;
        private ImageView imgThumb,imgIcon;
        private FileItem currentItem;
        private View itemView;
        private LoadPDFThumbTask pdfThumbTask;
        private GenerateBitmapThread generateBitmapThread;
        private SubsamplingScaleImageView pdfThumb;

        Context context;

        Handler handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what){
                    case Constants.WHAT_PREEXECUTE:
                        placeholder.setVisibility(View.VISIBLE);
                        PdfViewHolder.this.setIsRecyclable(false);

                        return true;
                    case Constants.WHAT_IMAGELOADED:

                        currentItem.setThumbnailImage((Bitmap) msg.obj);
                        placeholder.setVisibility(View.GONE);
                        //Sauvegarde du Bitmap dans le cache
                        currentItem.setThumbnailImage(currentItem.getThumbnailImage());
                        imgThumb.setImageBitmap(currentItem.getThumbnailImage());
                        PdfViewHolder.this.setIsRecyclable(true);

                        return true;
                    case Constants.WHAT_LOADING:


                        return true;
                    case Constants.WHAT_CANCEL:


                        return true;
                }

                return false;
            }
        });

        public GenerateBitmapThread getGenerateBitmapThread() {
            return generateBitmapThread;
        }

        public void setGenerateBitmapThread(GenerateBitmapThread generateBitmapThread) {
            this.generateBitmapThread = generateBitmapThread;
        }

        public PdfViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            imgThumb = (ImageView) itemView.findViewById(R.id.card_thumbnail);
            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);
            placeholder = (TextView) itemView.findViewById(R.id.card_placeholder);
            //pdfThumb = (SubsamplingScaleImageView)itemView.findViewById(R.id.card_pdfThumbnail);
            context=itemView.getContext();
        }

        public void setData(final FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            //View.OnClickListener clickListener = currentItem.getOnClickListener();
            final Bitmap thumbBitmap=currentItem.getThumbnailImage();
            final File pdfFile=currentItem.getFile();

            itemView.setOnClickListener(getClickListener(pdfFile,currentItem.getType()));
            this.title.setText(title);

            imgIcon.setImageResource(iconid);

            //Glide.with(itemView.getContext()).load(iconid).into(imgIcon);

            /*
            pdfView.fromFile(pdfFile)
                    .pages(0) // all pages are displayed by default
                    .enableSwipe(false) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(false)
                    .defaultPage(0)
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    //.enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .load();
            */
            /*
            int minimumTileDpi = 120;
            float scale=1;
            pdfThumb.setMinimumTileDpi(minimumTileDpi);
            pdfThumb.setMinimumScaleType(SCALE_TYPE_CENTER_CROP);
            pdfThumb.setPanEnabled(false);
            pdfThumb.setZoomEnabled(false);
            pdfThumb.setOnClickListener(clickListener);
            //pdfThumb.setClickable(false);

            //sets the PDFDecoder for the imageView
            pdfThumb.setBitmapDecoderFactory(() -> new PDFDecoder(0, pdfFile, scale));

            //sets the PDFRegionDecoder for the imageView
            pdfThumb.setRegionDecoderFactory(() -> new PDFRegionDecoder(0, pdfFile, scale));

            ImageSource source = ImageSource.uri(pdfFile.getAbsolutePath());

            pdfThumb.setImage(source);
            */


            imgThumb.post(new Runnable() {

                //Runs after layout
                @Override
                public void run() {
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
                        pdfThumbTask=new LoadPDFThumbTask(itemView.getContext(),PdfViewHolder.this,currentItem.getFile());
                        pdfThumbTask.execute();


                        //generateBitmapThread=new GenerateBitmapThread(handler,context,Constants.TYPE_PDF,currentItem.getFile());
                        //generateBitmapThread.start();

                        Log.d(TAG,"Generating Bitmap");

                    }
                }
            });








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
        Context context;


        public ImageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            imgThumb = (ImageView) itemView.findViewById(R.id.card_thumbnail);
            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);
            //placeholder = (TextView) itemView.findViewById(R.id.card_placeholder);
            context=itemView.getContext();
        }


        public void setData(FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            //View.OnClickListener clickListener = currentItem.getOnClickListener();
            final File imageFile=currentItem.getFile();

            Glide.with(itemView.getContext()).fromFile().asBitmap().load(imageFile).into(imgThumb);

            itemView.setOnClickListener(getClickListener(imageFile,currentItem.getType()));
            this.title.setText(title);

            imgIcon.setImageResource(iconid);

            //Glide.with(itemView.getContext()).load(iconid).into(imgIcon);

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
        private MediaPlayer mediaPlayer;
        private TextureView textureView;
        private TextureVideoView textureVideoView;
        private TextView placeholder;
        private LoadVideoThumbTask videoThumbTask;
        Context context;
        GenerateBitmapThread generateBitmapThread;

        Handler handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what){
                    case Constants.WHAT_PREEXECUTE:
                        placeholder.setVisibility(View.VISIBLE);
                        VideoViewHolder.this.setIsRecyclable(false);

                        return true;
                    case Constants.WHAT_IMAGELOADED:

                        currentItem.setThumbnailImage((Bitmap) msg.obj);
                        placeholder.setVisibility(View.GONE);
                        //Sauvegarde du Bitmap dans le cache
                        currentItem.setThumbnailImage(currentItem.getThumbnailImage());
                        imgThumb.setImageBitmap(currentItem.getThumbnailImage());
                        VideoViewHolder.this.setIsRecyclable(true);

                        return true;
                    case Constants.WHAT_LOADING:


                        return true;
                    case Constants.WHAT_CANCEL:


                        return true;
                }

                return false;
            }
        });

        public GenerateBitmapThread getGenerateBitmapThread() {
            return generateBitmapThread;
        }

        public void setGenerateBitmapThread(GenerateBitmapThread generateBitmapThread) {
            this.generateBitmapThread = generateBitmapThread;
        }

        public LoadVideoThumbTask getVideoThumbTask(){
            return videoThumbTask;
        }

        public VideoViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = (TextView) itemView.findViewById(R.id.title);

            imgIcon = (ImageView) itemView.findViewById(R.id.card_icon);

            imgThumb=(ImageView) itemView.findViewById(R.id.card_video_thumbnail);

            placeholder = (TextView) itemView.findViewById(R.id.card_placeholder);

            //videoView = (VideoView) itemView.findViewById(R.id.card_video);

            //textureView = (TextureView) itemView.findViewById(R.id.card_textureview);
            //textureView.setSurfaceTextureListener(this);

            //textureVideoView=(TextureVideoView) itemView.findViewById(R.id.card_texturevideoview);

            //mediaController = new MediaController(itemView.getContext());


            //mediaPlayer = new MediaPlayer();

            //fullscreen = (ImageButton) itemView.findViewById(R.id.card_fullscreen);

            playFab = (FloatingActionButton) itemView.findViewById(R.id.card_videoPlayFab);

            /*
            playFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //videoView.start();
                    //mediaPlayer.start();
                    //textureVideoView.start();
                    playFab.setVisibility(View.GONE);
                }
            });
            */

            /*
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playFab.setVisibility(View.VISIBLE);
                }
            });

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(0);
                }
            });

            videoView.setMediaController(mediaController);*/

            /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playFab.setVisibility(View.VISIBLE);
                }
            });*/

            /*
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(0);
                }
            });*/

            /*
            textureVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playFab.setVisibility(View.VISIBLE);
                }
            });

            textureVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(1);
                }
            });

            textureVideoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textureVideoView.pause();
                    playFab.setVisibility(View.VISIBLE);
                }
            });
            */


        }

        public void setData(final FileItem currentItem, int position) {

            this.currentItem=currentItem;
            int iconid = currentItem.getIconID();
            String title = currentItem.getTitle();
            //View.OnClickListener clickListener = currentItem.getOnClickListener();
            File videoFile=currentItem.getFile();
            final Bitmap thumbBitmap=currentItem.getThumbnailImage();

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

            /*
            fullscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //videoView.pause();
                    //mediaPlayer.pause();
                    textureVideoView.pause();
                    Intent videoPlayer=new Intent(v.getContext(),VideoViewerActivity.class);
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_MILLIS,textureVideoView.getCurrentPosition());
                    videoPlayer.putExtra(Constants.EXTRA_VIDEO_PATH,currentItem.getFile());

                    //On eteint la video dans la card et on la lance dans l'activité donc on reset le playButton
                    playFab.setVisibility(View.VISIBLE);

                    v.getContext().startActivity(videoPlayer);
                }
            });
            */

            playFab.setOnClickListener(getClickListener(videoFile,currentItem.getType()));


            this.title.setText(title);

            imgIcon.setImageResource(iconid);

            //Glide.with(itemView.getContext()).load(iconid).into(imgIcon); //Ne marche pas a cause des ressources vectorielles



            //imgThumb.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));


            imgThumb.post(new Runnable() {
                @Override
                public void run() {
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

                        /*
                        //Chargement de la miniatur
                        videoThumbTask=new LoadVideoThumbTask(itemView.getContext(),VideoViewHolder.this,currentItem.getFile());
                        videoThumbTask.execute();
                        */


                        generateBitmapThread=new GenerateBitmapThread(handler,itemView.getContext(),Constants.TYPE_VIDEO,currentItem.getFile());
                        generateBitmapThread.start();

                        Log.d(TAG,"Generating Bitmap");
                    }
                }
            });



            //videoView.setVideoPath(videoFile.getAbsolutePath());

            //textureVideoView.setDataSource(Uri.parse(videoFile.getAbsolutePath()));

            /*
            if(mediaPlayer!=null){
                try {
                    mediaPlayer.setDataSource(videoFile.getAbsolutePath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d(TAG,"MediaPlayerNULL");
            }
            */


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

    public static Comparator<FileItem> pullUpVideoTypes =new Comparator<FileItem>() {
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

    //Descend les dossier dans la liste et les trie par ordre alphabétique
    public static Comparator<File> pullDownFoldersAlpha= new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {

            if(o1.isDirectory() && o2.isDirectory()){
                return o1.getName().compareTo(o2.getName());
            }

            //On descend les dossiers
            if(o1.isDirectory() || o2.isDirectory()){
                return 1;
            }


            return o1.getName().compareTo(o2.getName());
        }
    };

    private List<FileItem> objectList;
    private LayoutInflater inflater;

    public FileRecyclerAdapter(Context context, List<FileItem> objectList) {
        this.objectList=objectList;
        //Collections.sort(objectList, pullUpVideoTypes);
        inflater=LayoutInflater.from(context);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
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

                view = inflater.inflate(R.layout.pdf_card_item,parent,false);
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

        if(holder instanceof VideoViewHolder){
            //Set a white background during loading of the new Task
            VideoViewHolder Holder=(VideoViewHolder) holder;
            Holder.imgThumb.setImageDrawable(new ColorDrawable(ContextCompat.getColor(Holder.imgThumb.getContext(),android.R.color.black)));
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
    public static Bitmap getPdfThumbnail(Context context, PdfiumCore pdfiumCore,File pdfFile,ImageView intoView) throws IOException {


        double resizefactor=6.0;

        int minimum_height=300;
        int minimum_width=300;

        long time_now= System.currentTimeMillis();


        ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(Uri.fromFile(pdfFile), "r");
        int pageNum = 0;
        PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

        pdfiumCore.openPage(pdfDocument, pageNum);



        int REQ_WIDTH=pdfiumCore.getPageWidth(pdfDocument,0);
        int REQ_HEIGHT=pdfiumCore.getPageHeight(pdfDocument,0);

        REQ_HEIGHT=(int)(REQ_HEIGHT/resizefactor);
        REQ_WIDTH=(int)(REQ_WIDTH/resizefactor);


        Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH,REQ_HEIGHT,
                Bitmap.Config.ARGB_4444);



        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0,
                REQ_WIDTH, REQ_HEIGHT);

        pdfiumCore.closeDocument(pdfDocument); // important!

        Log.d("PDF RENDERING",(System.currentTimeMillis()-time_now)+"");


        /*
        PdfRenderer renderer= null;
        try {

            time_now= System.currentTimeMillis();


            bitmap=Bitmap.createBitmap(REQ_WIDTH,REQ_HEIGHT, Bitmap.Config.ARGB_4444);

            renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile,ParcelFileDescriptor.MODE_READ_ONLY));

            Matrix m= intoView.getImageMatrix();
            Rect rect= new Rect(0,0,REQ_WIDTH,REQ_HEIGHT);

            PdfRenderer.Page page=renderer.openPage(0);
            page.render(bitmap,rect,m,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();

            renderer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        */



        return bitmap;

    }

    public static Bitmap getVideoThumbnail(Context context,File videoFile){
        return ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
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

    public class LoadVideoThumbTask extends AsyncTask<Void,Void,Bitmap>{

        String TAG=getClass().getSimpleName();
        Context context;
        ImageView intoView;
        VideoViewHolder viewHolder;
        View placeHolder;
        File videoFile;
        FileItem currentItem;


        public LoadVideoThumbTask(Context context, VideoViewHolder viewHolder, File pdfFile) {
            this.context = context;
            this.intoView = viewHolder.imgThumb;
            this.viewHolder=viewHolder;
            this.videoFile = pdfFile;
            this.placeHolder=viewHolder.placeholder;
            this.currentItem=viewHolder.currentItem;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            placeHolder.setVisibility(View.VISIBLE);
            viewHolder.setIsRecyclable(false);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            Bitmap thumb = currentItem.getThumbnailImage();
            //Si il y a deja une miniature on la reutilise tout de suite
            if(thumb!=null){
                Log.d(TAG,"recycled thumb");
                return thumb;
            }

            thumb = getVideoThumbnail(context,videoFile);
            //Log.d("LoadPDftask",pdfFile.getName());
            return thumb;


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

    public class GenerateBitmapThread extends Thread{

        Handler mHandler;
        int type;
        Context context;
        File inputFile;

        public GenerateBitmapThread(Handler mHandler,Context context,int type,File inputFile) {
            this.mHandler=mHandler;
            this.type=type;
            this.context=context;
            this.inputFile=inputFile;
        }

        @Override
        public synchronized void start() {
            super.start();
        }

        @Override
        public void run() {

            mHandler.obtainMessage(WHAT_PREEXECUTE).sendToTarget();

            mHandler.obtainMessage(WHAT_LOADING).sendToTarget();

            Bitmap image=null;
            if(type == Constants.TYPE_PDF){

                try {
                    PdfiumCore core=new PdfiumCore(context);
                    image=getPdfThumbnail(context,core,inputFile,null);
                    mHandler.obtainMessage(WHAT_IMAGELOADED,image).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(WHAT_CANCEL);
                }


            }
            if(type == Constants.TYPE_VIDEO){

                image=getVideoThumbnail(context,inputFile);
                mHandler.obtainMessage(WHAT_IMAGELOADED,image).sendToTarget();
            }

            //mHandler.obtainMessage(WHAT_CANCEL);




        }

        @Override
        public void interrupt() {
            super.interrupt();
            mHandler.obtainMessage(WHAT_CANCEL);
        }
    }


}
