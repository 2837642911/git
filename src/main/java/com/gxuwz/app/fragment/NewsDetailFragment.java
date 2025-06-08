package com.gxuwz.app.fragment;

import static com.gxuwz.app.network.WebAPI.API_KEY;

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
import com.google.gson.Gson;
import com.gxuwz.app.R;
import com.gxuwz.app.activity.MainActivity;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.model.network.NewsDetailResponse;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.network.ResponseHandler;
import com.gxuwz.app.network.RetrofitClient;

import retrofit2.Call;

public class NewsDetailFragment extends Fragment {
    private static final String TAG = "NewsDetailFragment";
    private static final String ARG_NEWS = "arg_news";

    private NewsItem news;
    private NewsApi newsApi;
    private Call<NewsDetailResponse> currentCall;
    private boolean hasRetried = false;

    private TextView tvTitle, tvSource, tvTime, tvContent;
    private ImageView ivNews;

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

            // 加载详细内容
            loadNewsDetail();
        }
    }

    private void loadNewsDetail() {
        if (news == null || news.getUniquekey() == null) {
            Log.e(TAG, "loadNewsDetail: News or uniquekey is null");
            if (tvContent != null) tvContent.setText("无法加载新闻详情：新闻数据不完整");
            return;
        }

        Log.d(TAG, "loadNewsDetail: API_KEY = " + API_KEY + ", uniquekey = " + news.getUniquekey());
        currentCall = newsApi.getNewsDetail(API_KEY, news.getUniquekey());
        ResponseHandler.handleResponse(requireContext(),
            currentCall,
            new ResponseHandler.ResponseCallback<NewsDetailResponse>() {
                @Override
                public void onSuccess(NewsDetailResponse response) {
                    // 防止Fragment视图已销毁
                    if (getView() == null || getActivity() == null || tvTitle == null) {
                        Log.w(TAG, "onSuccess: getView() or getActivity() or tvTitle is null, fragment may be destroyed");
                        return;
                    }
                    Log.d(TAG, "onSuccess: News detail loaded successfully");
                    Log.d(TAG, "onSuccess: response JSON = " + new Gson().toJson(response));
                    Log.d(TAG, "onSuccess: error_code = " + response.getError_code());
                    Log.d(TAG, "onSuccess: reason = " + response.getReason());
                    NewsDetailResponse.Result result = response.getResult();
                    if (result != null) {
                        Log.d(TAG, "onSuccess: result.uniquekey = " + result.getUniquekey());
                        Log.d(TAG, "onSuccess: result.content = " + result.getContent());
                        NewsDetailResponse.Detail detail = result.getDetail();
                        if (detail != null) {
                            Log.d(TAG, "onSuccess: detail.title = " + detail.getTitle());
                            Log.d(TAG, "onSuccess: detail.date = " + detail.getDate());
                            Log.d(TAG, "onSuccess: detail.category = " + detail.getCategory());
                            Log.d(TAG, "onSuccess: detail.author_name = " + detail.getAuthor_name());
                            Log.d(TAG, "onSuccess: detail.url = " + detail.getUrl());
                            Log.d(TAG, "onSuccess: detail.thumbnail_pic_s = " + detail.getThumbnail_pic_s());
                        }
                        // 更新标题、来源和时间（如果详情中有更新）
                        if (detail != null) {
                            tvTitle.setText(detail.getTitle());
                            tvSource.setText(detail.getAuthor_name());
                            tvTime.setText(detail.getDate());
                            // 如果有新的图片，更新图片
                            if (detail.getThumbnail_pic_s() != null && !detail.getThumbnail_pic_s().isEmpty()) {
                                Glide.with(requireContext())
                                        //为了方便只显示一张
                                    .load(detail.getThumbnail_pic_s())

                                        .into(ivNews);
                            }
                        }
                        // 显示内容
                        String content = result.getContent();
                        if (content != null && !content.isEmpty()) {
                            tvContent.setText(android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tvContent.setText("暂无详细内容");
                        }
                    } else {
                        Log.w(TAG, "onSuccess: Result is null");
                        tvContent.setText("暂无详细内容");
                    }
                }
                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "onError: " + errorMsg);
                    if (getActivity() == null || tvContent == null) {
                        Log.w(TAG, "onError: Activity or tvContent is null");
                        return;
                    }
                    if (errorMsg.contains("网络错误") && !hasRetried) {
                        hasRetried = true;
                        Log.d(TAG, "onError: Retrying once...");
                        loadNewsDetail();
                        return;
                    }
                    String displayMsg = "加载详细内容失败";
                    if (errorMsg.contains("未知错误")) {
                        displayMsg += "：服务器返回未知错误";
                    } else {
                        displayMsg += "：" + errorMsg;
                    }
                    tvContent.setText(displayMsg);
                    Toast.makeText(getContext(), displayMsg, Toast.LENGTH_SHORT).show();
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
}