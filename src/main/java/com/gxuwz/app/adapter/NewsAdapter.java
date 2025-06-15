package com.gxuwz.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gxuwz.app.R;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.pojo.NewsHistory;

import java.util.List;
import java.util.Random;

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

        // 绑定文本数据
        bindTextData(holder, newsItem);

        // 加载图片
        loadImages(holder, newsItem, position);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(newsItem);
            }
        });
    }

    /**
     * 绑定文本数据到ViewHolder
     */
    private void bindTextData(NewsViewHolder holder, NewsItem newsItem) {
        holder.titleTextView.setText(newsItem.getTitle());
        holder.sourceTextView.setText(newsItem.getAuthor_name());
        holder.timeTextView.setText(newsItem.getDate());
        holder.categoryTextView.setText(newsItem.getCategory());
    }

    /**
     * 加载主图和来源图标
     */
    private void loadImages(NewsViewHolder holder, NewsItem newsItem, int position) {
        // 加载主图
        if (newsItem.getThumbnail_pic_s() != null && !newsItem.getThumbnail_pic_s().isEmpty()) {
            // 等待ImageView测量完成后再加载
            holder.imageView.post(() -> {
                int width = holder.imageView.getWidth();
                int height = holder.imageView.getHeight();

                // 若测量失败， fallback到默认尺寸（如100x75）
                if (width == 0 || height == 0) {
                    width = 100;
                    height = 75;
                }

                Glide.with(holder.itemView.getContext())
                        .load(newsItem.getThumbnail_pic_s())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height) // 强制匹配测量尺寸
                        .placeholder(R.drawable.ic_news_placeholder) // 加载中显示占位图
                        .error(R.drawable.ic_404) // 加载失败也显示占位图
                        .into(holder.imageView);
            });
        }else {
            // 无图时隐藏ImageView，并调整布局权重
            holder.imageView.setVisibility(View.GONE);
            adjustLayoutForNoImage(holder);
        }

        // 加载来源图标（使用position生成稳定的随机数）
        int randomNum = new Random(position).nextInt(1000); // 基于position生成确定性随机数
        String randomIconUrl = "https://picsum.photos/32/32?random=" + randomNum;

        Glide.with(holder.itemView.getContext())
                .load(randomIconUrl)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_source_placeholder)
                .error(R.drawable.ic_source_error)
                .into(holder.sourceIconView);
    }
    // 无图时调整布局权重，让文字区域填满空间
    private void adjustLayoutForNoImage(NewsViewHolder holder) {
        // 获取文字区域的LinearLayout
        LinearLayout textLayout = holder.textContainer;

        // 无图时，文字区域权重从2改为1（填满剩余空间）
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textLayout.getLayoutParams();
        params.weight = 1;
        textLayout.setLayoutParams(params);
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
        TextView categoryTextView ;
        ImageView sourceIconView;

        LinearLayout textContainer;
        NewsViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_news_image);
            titleTextView = itemView.findViewById(R.id.tv_news_title);
            sourceTextView = itemView.findViewById(R.id.tv_news_source);
            timeTextView = itemView.findViewById(R.id.tv_news_time);
            categoryTextView = itemView.findViewById(R.id.news_category);
            sourceIconView = itemView.findViewById(R.id.iv_source_icon);
            textContainer = itemView.findViewById(R.id.text_container);
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