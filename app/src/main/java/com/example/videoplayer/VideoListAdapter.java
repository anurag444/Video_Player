package com.example.videoplayer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private List<String> data= new ArrayList<>();
    private VideoListAdapter.OnItemClickListener onItemClickListener;
    private VideoListAdapter.OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position,View view);
    }
    public void setOnItemClickListener(VideoListAdapter.OnItemClickListener listener){
        onItemClickListener=listener;
    }
    public void setOnItemLongClickListener(VideoListAdapter.OnItemLongClickListener listener){
        onItemLongClickListener=listener;
    }

    public void setData(List<String> mdata){
        data.clear();
        data.addAll(mdata);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        ViewHolder holder = new ViewHolder(view,onItemClickListener,onItemLongClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final String item = data.get(position);
        holder.info.setText(item.substring(item.lastIndexOf('/')+1));

    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView info;


        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener, final OnItemLongClickListener mlistener) {
            super(itemView);

            info= itemView.findViewById(R.id.myid);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (listener!=null){
                        int position = getAdapterPosition();
                        if (position!= RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mlistener!=null){
                        int position = getAdapterPosition();
                        if (position!= RecyclerView.NO_POSITION){
                            mlistener.onItemLongClicked(position,v);
                        }
                    }
                    return false;
                }
            });

        }
    }

}
