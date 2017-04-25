package com.polytech.communicationpolytech;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Jérémy on 07/04/2017.
 */

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder> {

    private List<FileItem> objectList;
    private LayoutInflater inflater;

    public FileRecyclerAdapter(Context context, List<FileItem> objectList) {
        this.objectList=objectList;
        inflater=LayoutInflater.from(context);
    }



    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.file_card_item,parent,false);
        FileViewHolder holder=new FileViewHolder(view);

        return holder;
    }

    @Override
    public void onViewDetachedFromWindow(FileViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //If the view is detached from the screen, kill the asynctask no longer needed
        /*
        if(holder.pdfThumbTask !=null && holder.pdfThumbTask.getStatus() == AsyncTask.Status.RUNNING){
            holder.pdfThumbTask.cancel(true);
        }*/
    }

    @Override
    public void onViewRecycled(FileViewHolder holder) {
        super.onViewRecycled(holder);



        //Kill the old asyntask if it is running
        if(holder.pdfThumbTask !=null && holder.pdfThumbTask.getStatus() == AsyncTask.Status.RUNNING){
            holder.pdfThumbTask.cancel(true);
        }


        //Set a white background during loading of the new Task
        holder.imgThumb.setImageDrawable(new ColorDrawable(ContextCompat.getColor(holder.imgThumb.getContext(),R.color.cardBackground)));
    }

    @Override
    public void onViewAttachedToWindow(FileViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        FileItem currentItem=objectList.get(position);
        holder.setData(currentItem,position);
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder{

        final String TAG=getClass().getSimpleName();

        private TextView title;
        private TextView placeholder;
        private ImageView imgThumb,imgIcon;
        private FileItem currentItem;
        private View itemView;
        private LoadPDFThumbTask pdfThumbTask;

        public FileViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            imgThumb = (ImageView) itemView.findViewById(R.id.card_thumbnail);
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
                //Chargement de la miniature
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
        FileViewHolder viewHolder;
        View placeHolder;
        File pdfFile;
        FileItem currentItem;
        PdfiumCore pdfiumCore;

        public LoadPDFThumbTask(Context context,FileViewHolder viewHolder,File pdfFile) {
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
        }
    }


}
