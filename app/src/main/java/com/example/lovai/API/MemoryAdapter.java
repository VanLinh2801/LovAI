package com.example.lovai.API;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lovai.DTO.Memory.MemoryResponse;
import com.example.lovai.R;

import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder> {
    private List<MemoryResponse> memoryList;
    private OnMemoryClickListener listener;

    public interface OnMemoryClickListener{
        void onMemoryClick(MemoryResponse memory);
    }

    public MemoryAdapter(List<MemoryResponse> memoryList, OnMemoryClickListener listener) {
        this.memoryList = memoryList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) { // tu dong lặp
        MemoryResponse memory = memoryList.get(position);
        holder.tvTitle.setText(memory.getTitle());
        holder.tvDate.setText(memory.getHappenedAt());
        holder.tvDesc.setText(memory.getDescription());
        holder.itemView.setOnClickListener(v ->
        {if(listener != null) listener.onMemoryClick(memory);}
        );
    }

    @Override
    public int getItemCount() {
        if (memoryList != null) {
            return memoryList.size();
        } else {
            return 0;
        }
    }

    public void setMemoryList(List<MemoryResponse> newList) {
        this.memoryList = newList;
        notifyDataSetChanged();
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvDesc;
        ImageView imgMemory;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMemoryTitle);
            tvDate = itemView.findViewById(R.id.tvMemoryDate);
            tvDesc = itemView.findViewById(R.id.tvMemoryDesc);
            imgMemory = itemView.findViewById(R.id.imgMemory);
        }
    }
}
