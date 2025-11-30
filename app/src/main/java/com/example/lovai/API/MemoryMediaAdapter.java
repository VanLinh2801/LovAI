package com.example.lovai.API;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lovai.DTO.Memory.MemoryMediaResponse;
import com.example.lovai.R;

import java.util.List;

public class MemoryMediaAdapter extends RecyclerView.Adapter<MemoryMediaAdapter.ViewHolder> {
    private List<MemoryMediaResponse> mediaList;
    private OnMediaClickListener listener;

    public interface OnMediaClickListener {
        void onMediaClick(String imageUrl);
    }

    public void setOnMediaClickListener(OnMediaClickListener listener){
        this.listener = listener;
    }
    public MemoryMediaAdapter(List<MemoryMediaResponse> mediaList){
        this.mediaList = mediaList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemoryMediaResponse media = mediaList.get(position);
        Glide.with(holder.imageView.getContext())
                .load(media.getUrl())
                .placeholder(R.drawable.couplemiy)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            if (listener != null) listener.onMediaClick(media.getUrl());
        });

    }
    @Override
    public int getItemCount() { return mediaList.size(); }

    public void setMediaList(List<MemoryMediaResponse> mediaList){
        this.mediaList = mediaList;
        notifyDataSetChanged(); // gọi lại tuần tự getItemCount(), onCreateView..., onBindViewHolder
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgMedia);
        }
    }
}
