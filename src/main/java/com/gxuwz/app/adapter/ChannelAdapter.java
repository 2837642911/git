package com.gxuwz.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.gxuwz.app.R;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {
    public  List<String> data;
    public boolean isMy;
    public  OnItemClickListener listener;
    public ChannelAdapter(List<String> data, boolean isMy) {
        this.data = data;
        this.isMy = isMy;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_chip, parent, false);
        return new ChannelAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ChannelAdapter.ViewHolder holder, int position) {
        holder.chip.setText(data.get(position));
        holder.chip.setChecked(isMy);
        holder.chip.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }
    @Override
    public int getItemCount() { return data.size(); }
    public void setOnItemClickListener(ChannelAdapter.OnItemClickListener l) { this.listener = l; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;
        ViewHolder(View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_channel);
        }
    }
    public interface OnItemClickListener { void onItemClick(int pos); }
}