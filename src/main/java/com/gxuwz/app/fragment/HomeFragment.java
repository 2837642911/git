package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gxuwz.app.R;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.model.bean.NewsResponse;
import com.gxuwz.app.network.ResponseHandler;
import com.gxuwz.app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Random;

import retrofit2.Call;

/**
 * 空白模板Fragment
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    // 声明向Fragment传递的参数对应的Key常量
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private static final String API_KEY = "d08d5410e2b92d647a6b17978d0fd649";
    private static final int PAGE_SIZE = 15;
    private static final int IS_FILTER = 1;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private NewsApi newsApi;
    private Random random = new Random();
    private Call<NewsResponse> currentCall;

    public HomeFragment() {
        super();
    }

    /**
     * 创建带给定Bundle参数的Fragment实例的类方法
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment created");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        newsApi = RetrofitClient.getNewsApi();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        
        // 初始化适配器
        newsAdapter = new NewsAdapter(new ArrayList<>(), newsItem -> {
            Log.d(TAG, "onItemClick: News item clicked: " + newsItem.getTitle());
            // 点击新闻项时，切换到详情页
            NewsDetailFragment detailFragment = NewsDetailFragment.newInstance(newsItem);
            // 使用父容器作为目标容器
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.viewPager, detailFragment)
                    .addToBackStack(null)
                    .commit();
                Log.d(TAG, "onItemClick: Fragment transaction committed");
            } else {
                Log.e(TAG, "onItemClick: Activity is null");
            }
        });
        recyclerView.setAdapter(newsAdapter);
        
        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        
        // 首次加载数据
        loadNews();
        
        return view;
    }

    private void loadNews() {
        Log.d(TAG, "loadNews: Starting to load news");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // 取消之前的请求
        if (currentCall != null && !currentCall.isCanceled()) {
            Log.d(TAG, "loadNews: Cancelling previous request");
            currentCall.cancel();
        }
        
        // 随机页码
        int randomPage = random.nextInt(50) + 1;
        Log.d(TAG, "loadNews: Loading page " + randomPage);
        
        // 加载推荐新闻
        currentCall = newsApi.getNewsList(API_KEY, "top", randomPage, PAGE_SIZE, IS_FILTER);
        ResponseHandler.handleResponse(requireContext(), 
            currentCall,
            new ResponseHandler.ResponseCallback<NewsResponse>() {
                @Override
                public void onSuccess(NewsResponse response) {
                    Log.d(TAG, "onSuccess: News loaded successfully");
                    if (getActivity() == null) {
                        Log.w(TAG, "onSuccess: Activity is null");
                        return;
                    }
                    NewsResponse.Result result = response.getData();
                    if (result != null && result.getData() != null) {
                        Log.d(TAG, "onSuccess: Updating adapter with " + result.getData().size() + " items");
                        newsAdapter.updateNewsList(result.getData());
                    } else {
                        Log.w(TAG, "onSuccess: Result or data is null");
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "onError: " + errorMsg);
                    if (getActivity() == null) {
                        Log.w(TAG, "onError: Activity is null");
                        return;
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: Fragment view being destroyed");
        super.onDestroyView();
        // 取消网络请求
        if (currentCall != null && !currentCall.isCanceled()) {
            Log.d(TAG, "onDestroyView: Cancelling network request");
            currentCall.cancel();
        }
        // 清除引用
        recyclerView = null;
        swipeRefreshLayout = null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Fragment being destroyed");
        super.onDestroy();
    }
}