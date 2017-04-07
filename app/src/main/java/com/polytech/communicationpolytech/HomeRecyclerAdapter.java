package com.polytech.communicationpolytech;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Jérémy on 07/04/2017.
 */

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder> {

    private List<HomeItem> objectList;
    private LayoutInflater inflater;

    public HomeRecyclerAdapter(Context context, List<HomeItem> objectList) {
        this.objectList=objectList;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.card_item,parent,false);
        HomeViewHolder holder=new HomeViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        HomeItem currentItem=objectList.get(position);
        holder.setData(currentItem,position);
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private ImageView imgThumb,imgIcon;
        private HomeItem currentItem;
        private View itemView;

        public HomeViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            title=(TextView) itemView.findViewById(R.id.title);
            imgThumb=(ImageView) itemView.findViewById(R.id.thumbnail);
            imgIcon=(ImageView) itemView.findViewById(R.id.type);

        }


        public void setData(HomeItem currentItem, int position) {

            int thumbid=currentItem.getThumbID();
            int iconid=currentItem.getIconID();
            String title=currentItem.getTitle();
            View.OnClickListener clickListener=currentItem.getOnClickListener();

            itemView.setOnClickListener(clickListener);
            this.title.setText(title);
            Glide.with(itemView.getContext()).load(thumbid).into(imgThumb);
            imgIcon.setImageResource(iconid);

        }
    }
}
