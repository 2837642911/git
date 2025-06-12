package com.gxuwz.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.app.R;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.pojo.NewsHistory;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<NewsItem> newsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NewsItem newsItem);
    }

    public NewsAdapter(List<NewsItem> newsList, OnItemClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.titleTextView.setText(newsItem.getTitle());
        holder.sourceTextView.setText(newsItem.getAuthor_name());
        holder.timeTextView.setText(newsItem.getDate());

        // Load image using Glide
        if (newsItem.getThumbnail_pic_s() != null && !newsItem.getThumbnail_pic_s().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(newsItem.getThumbnail_pic_s())
                    .centerCrop()
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(newsItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateNewsList(List<NewsItem> newList) {
        this.newsList = newList;
        notifyDataSetChanged();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView sourceTextView;
        TextView timeTextView;

        NewsViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_news_image);
            titleTextView = itemView.findViewById(R.id.tv_news_title);
            sourceTextView = itemView.findViewById(R.id.tv_news_source);
            timeTextView = itemView.findViewById(R.id.tv_news_time);
        }
    }


    // 用于刷新整个列表（比如下拉刷新、首次加载）
    public void updateNewsHistoryList(List<NewsHistory> newList) {
        // 你可以把 NewsHistory 转成 NewsItem，或者直接让 NewsAdapter 支持 NewsHistory
        this.newsList.clear();
        for (NewsHistory history : newList) {
            this.newsList.add(convertToNewsItem(history));
        }
        notifyDataSetChanged();
    }

    // 用于分页加载时追加数据
    public void addNewsHistoryList(List<NewsHistory> more) {
        int start = this.newsList.size();
        for (NewsHistory history : more) {
            this.newsList.add(convertToNewsItem(history));
        }
        notifyItemRangeInserted(start, more.size());
    }

    // 辅助方法：将 NewsHistory 转为 NewsItem
    private NewsItem convertToNewsItem(NewsHistory history) {
        NewsItem item = new NewsItem();
        item.setTitle(history.getTitle());
        item.setAuthor_name(history.getAuthor_name());
        item.setDate(history.getDate());
        item.setThumbnail_pic_s(history.getThumbnail_pic_s());
        item.setUniquekey(history.getUniquekey());
        item.setCategory(history.getCategory());
        item.setUrl(history.getUrl());

        return item;
    }
} 