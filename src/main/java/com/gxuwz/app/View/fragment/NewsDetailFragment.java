package com.gxuwz.app.View.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.gxuwz.app.R;
import com.gxuwz.app.View.activity.MainActivity;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.model.network.NewsDetailResponse;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.network.RetrofitClient;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.utils.SessionManager;

import retrofit2.Call;

public class NewsDetailFragment extends Fragment {
    private static final String TAG = "NewsDetailFragment";
    private static final String ARG_NEWS = "arg_news";

    private NewsItem news;
    private NewsApi newsApi;
    private Call<NewsDetailResponse> currentCall;
    private boolean hasRetried = false;

    private TextView tvTitle, tvSource, tvTime, tvContent;
    private ImageView ivNews, ivFavorite, ivShare;
    private TextView tvFavorite;
    private boolean isFavorite = false;

    private NewsHistoryDao newsHistoryDao;

    public static NewsDetailFragment newInstance(NewsItem news) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NEWS, news);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            news = (NewsItem) getArguments().getSerializable(ARG_NEWS);
        }
        newsApi = RetrofitClient.getNewsApi();
        newsHistoryDao = AppDatabase.getInstance(requireContext()).newsHistoryDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            news = ((MainActivity) getActivity()).getCurrentNewsItem();
        }

        tvTitle = view.findViewById(R.id.tv_title);
        tvSource = view.findViewById(R.id.tv_source);
        tvTime = view.findViewById(R.id.tv_time);
        ivNews = view.findViewById(R.id.iv_news);
        tvContent = view.findViewById(R.id.tv_content);
        ivFavorite = view.findViewById(R.id.iv_favorite);
        tvFavorite = view.findViewById(R.id.tv_favorite_count);
        ivShare = view.findViewById(R.id.iv_share);

        if (news != null) {
            // 先显示基本信息
            tvTitle.setText(news.getTitle());
            tvSource.setText(news.getAuthor_name());
            tvTime.setText(news.getDate());

            if (news.getThumbnail_pic_s() != null && !news.getThumbnail_pic_s().isEmpty()) {
                Glide.with(this)
                    .load(news.getThumbnail_pic_s())
                    .into(ivNews);
            }

            // 查询收藏状态并刷新UI
            checkFavoriteStatus();

            ivFavorite.setOnClickListener(v -> {
                toggleFavorite();
            });

            ivShare.setOnClickListener(v -> {
                shareToQQ();
            });

            // 加载详细内容
            loadNewsDetail();
        }
    }

    private void loadNewsDetail() {
        currentCall = newsApi.getNewsDetail(com.gxuwz.app.network.WebAPI.API_KEY, news.getUniquekey());
        currentCall.enqueue(new retrofit2.Callback<NewsDetailResponse>() {
            @Override
            public void onResponse(retrofit2.Call<NewsDetailResponse> call, retrofit2.Response<NewsDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: 更新UI显示新闻详情内容
                } else {
                    Toast.makeText(requireContext(), "加载新闻详情失败", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<NewsDetailResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "加载新闻详情失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消网络请求
        if (currentCall != null && !currentCall.isCanceled()) {
            Log.d(TAG, "onDestroyView: Cancelling network request");
            currentCall.cancel();
        }
        tvTitle = null;
        tvSource = null;
        tvTime = null;
        ivNews = null;
        tvContent = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        // 每次可见时都获取最新的news
        if (getActivity() instanceof MainActivity) {
            news = ((MainActivity) getActivity()).getCurrentNewsItem();
            // 刷新UI
            updateNewsUI();
        }
    }

    private void updateNewsUI() {
        if (news != null && tvTitle != null) {
            tvTitle.setText(news.getTitle());
            tvSource.setText(news.getAuthor_name());
            tvTime.setText(news.getDate());
            if (news.getThumbnail_pic_s() != null && !news.getThumbnail_pic_s().isEmpty()) {
                Glide.with(this)
                        .load(news.getThumbnail_pic_s())
                        .into(ivNews);
            }
            loadNewsDetail();
        }
    }

    private void checkFavoriteStatus() {
        new Thread(() -> {
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            NewsHistory history = newsHistoryDao.getNewsHistory(news.getUniquekey(), userId);
            isFavorite = history != null && history.isFavorite();
            requireActivity().runOnUiThread(this::updateFavoriteUI);
        }).start();
    }

    private void updateFavoriteUI() {
        ivFavorite.setSelected(isFavorite);
        tvFavorite.setText(isFavorite ? "已收藏" : "收藏");
    }

    private void toggleFavorite() {
        new Thread(() -> {
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            NewsHistory history = newsHistoryDao.getNewsHistory(news.getUniquekey(), userId);
            if (history == null) {
                history = new NewsHistory(userId, news.getUniquekey(), news.getTitle(), news.getCategory(),
                        news.getThumbnail_pic_s(), news.getUrl(), news.getAuthor_name(), news.getDate());
                history.setFavorite(true);
                newsHistoryDao.insert(history);
                isFavorite = true;
            } else {
                isFavorite = !history.isFavorite();
                history.setFavorite(isFavorite);
                newsHistoryDao.update(history);
            }
            requireActivity().runOnUiThread(() -> {
                updateFavoriteUI();
                Toast.makeText(requireContext(), isFavorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void shareToQQ() {
        if (news == null) {
            Toast.makeText(requireContext(), "分享失败：新闻数据不完整", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setPackage("com.tencent.mobileqq");
            intent.setType("text/plain");

            // 构建分享内容
            String shareContent = String.format("标题：%s\n作者：%s\n链接：%s",
                    news.getTitle(),
                    news.getAuthor_name(),
                    news.getUrl());

            intent.putExtra(Intent.EXTRA_TEXT, shareContent);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "分享失败：请确保已安装QQ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "shareToQQ: Error sharing to QQ", e);
        }
    }
}