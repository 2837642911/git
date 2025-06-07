package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.gxuwz.app.R;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.model.bean.NewsItem;
import com.gxuwz.app.model.bean.NewsDetailResponse;
import com.gxuwz.app.network.ResponseHandler;
import com.gxuwz.app.network.RetrofitClient;

import retrofit2.Call;

public class NewsDetailFragment extends Fragment {
    private static final String TAG = "NewsDetailFragment";
    private static final String ARG_NEWS = "arg_news";
    private static final String API_KEY = "d08d5410e2b92d647a6b17978d0fd649";

    private NewsItem news;
    private NewsApi newsApi;
    private Call<NewsDetailResponse> currentCall;

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

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvSource = view.findViewById(R.id.tv_source);
        TextView tvTime = view.findViewById(R.id.tv_time);
        ImageView ivNews = view.findViewById(R.id.iv_news);
        TextView tvContent = view.findViewById(R.id.tv_content);

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
            loadNewsDetail(tvContent);
        }
    }

    private void loadNewsDetail(TextView tvContent) {
        if (news == null || news.getUniquekey() == null) {
            Log.e(TAG, "loadNewsDetail: News or uniquekey is null");
            return;
        }

        Log.d(TAG, "loadNewsDetail: Loading news detail for uniquekey: " + news.getUniquekey());
        currentCall = newsApi.getNewsDetail(API_KEY, news.getUniquekey());
        ResponseHandler.handleResponse(requireContext(),
            currentCall,
            new ResponseHandler.ResponseCallback<NewsDetailResponse>() {
                @Override
                public void onSuccess(NewsDetailResponse response) {
                    Log.d(TAG, "onSuccess: News detail loaded successfully");
                    if (getActivity() == null) {
                        Log.w(TAG, "onSuccess: Activity is null");
                        return;
                    }
                    NewsDetailResponse.Result result = response.getData();
                    if (result != null && result.getContent() != null) {
                        tvContent.setText(result.getContent());
                    } else {
                        Log.w(TAG, "onSuccess: Result or content is null");
                        tvContent.setText("暂无详细内容");
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "onError: " + errorMsg);
                    if (getActivity() == null) {
                        Log.w(TAG, "onError: Activity is null");
                        return;
                    }
                    tvContent.setText("加载详细内容失败：" + errorMsg);
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
    }
}