package com.gxuwz.app.fragment;

import static com.gxuwz.app.network.WebAPI.API_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gxuwz.app.R;
import com.gxuwz.app.activity.MainActivity;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.network.NewsDetailResponse;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.model.pojo.NewsItem;
import com.gxuwz.app.network.ResponseHandler;
import com.gxuwz.app.network.RetrofitClient;
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
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
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
        // 防御式编程：验证必要参数
        if (news == null || news.getUniquekey() == null) {
            Log.e(TAG, "News data is incomplete, cannot load detail");
            setContentText("无法加载新闻详情：新闻数据不完整");
            return;
        }

        Log.d(TAG, "Loading news detail: " + news.getUniquekey());
        currentCall = newsApi.getNewsDetail(API_KEY, news.getUniquekey());

        ResponseHandler.handleResponse(requireContext(), currentCall,
                new ResponseHandler.ResponseCallback<NewsDetailResponse>() {
                    @Override
                    public void onSuccess(NewsDetailResponse response) {
                        if (!isViewValid()) {
                            Log.w(TAG, "Fragment view is destroyed, skipping update");
                            return;
                        }

                        NewsDetailResponse.Result result = response.getResult();
                        if (result == null) {
                            Log.w(TAG, "Empty result received from API");
                            setContentText("暂无详细内容");
                            return;
                        }

                        updateNewsInfo(result.getDetail());
                        displayContent(result.getContent());
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (!isActivityValid()) {
                            Log.w(TAG, "Activity is not available, skipping error handling");
                            return;
                        }

                        // 智能重试逻辑：仅对网络错误进行一次重试
                        if (errorMsg.contains("网络错误") && !hasRetried) {
                            hasRetried = true;
                            Log.d(TAG, "Network error detected, retrying request");
                            loadNewsDetail();
                            return;
                        }

                        String userMessage = formatErrorMessage(errorMsg);
                        setContentText(userMessage);
                        showToast(userMessage);
                    }
                }
        );
    }

    // 视图有效性检查（防止Fragment已销毁）
    private boolean isViewValid() {
        return getView() != null && getActivity() != null && tvTitle != null;
    }

    // Activity有效性检查
    private boolean isActivityValid() {
        return getActivity() != null && tvContent != null;
    }

    // 更新新闻基本信息
    private void updateNewsInfo(NewsDetailResponse.Detail detail) {
        if (detail == null) return;

        tvTitle.setText(detail.getTitle());
        tvSource.setText(detail.getAuthor_name());
        tvTime.setText(detail.getDate());

        if (!TextUtils.isEmpty(detail.getThumbnail_pic_s())) {
            Glide.with(requireContext())
                    .load(detail.getThumbnail_pic_s())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(ivNews);
        }
    }

    // 显示新闻内容
    private void displayContent(String content) {
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvContent.setText("暂无详细内容");
        }
    }

    // 设置内容文本（带空安全）
    private void setContentText(String text) {
        if (tvContent != null) {
            tvContent.setText(text);
        }
    }

    // 显示Toast消息
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // 格式化错误消息
    private String formatErrorMessage(String errorMsg) {
        String displayMsg = "加载详细内容失败";
        if (errorMsg.contains("未知错误")) {
            displayMsg += "：服务器返回未知错误";
        } else {
            displayMsg += "：" + errorMsg;
        }
        return displayMsg;
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
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .into(ivNews);
            }
            loadNewsDetail();
        }
    }

    private void checkFavoriteStatus() {
        new Thread(() -> {
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            NewsHistoryDao dao = AppDatabase.getInstance(requireContext()).newsHistoryDao();
            NewsHistory history = dao.getNewsHistory(news.getUniquekey(), userId);
            isFavorite = history != null && history.isFavorite();
            requireActivity().runOnUiThread(() -> updateFavoriteUI());
        }).start();
    }

    private void updateFavoriteUI() {
        ivFavorite.setSelected(isFavorite);
        tvFavorite.setText(isFavorite ? "已收藏" : "收藏");
    }

    private void toggleFavorite() {
        new Thread(() -> {
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            NewsHistoryDao dao = AppDatabase.getInstance(requireContext()).newsHistoryDao();
            NewsHistory history = dao.getNewsHistory(news.getUniquekey(), userId);
            if (history == null) {
                // 没有历史，插入并收藏
                history = new NewsHistory(userId, news.getUniquekey(), news.getTitle(), news.getCategory(),
                        news.getThumbnail_pic_s(), news.getUrl(), news.getAuthor_name(), news.getDate());
                history.setFavorite(true);
                dao.insert(history);
                isFavorite = true;
            } else {
                // 已有历史，切换收藏状态
                isFavorite = !history.isFavorite();
                dao.updateFavorite(news.getUniquekey(), userId, isFavorite);
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