package com.gxuwz.app.View.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.gxuwz.app.R;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.network.NewsResponse;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.network.RetrofitClient;
import com.gxuwz.app.network.WebAPI;
import com.gxuwz.app.utils.SessionManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class NewsListFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private String type;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private NewsApi newsApi;
    private NewsHistoryDao newsHistoryDao;

    public static NewsListFragment newInstance(String type) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE, "top");
        }
        newsApi = RetrofitClient.getNewsApi();
        newsHistoryDao = AppDatabase.getInstance(requireContext()).newsHistoryDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_record, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        newsAdapter = new NewsAdapter(new ArrayList<>(), newsItem -> {
            // 跳转到详情页
            if (getActivity() instanceof com.gxuwz.app.View.activity.MainActivity) {
                ((com.gxuwz.app.View.activity.MainActivity) getActivity()).setCurrentNewsItem(newsItem);
                ((com.gxuwz.app.View.activity.MainActivity) getActivity()).replaceFragment(com.gxuwz.app.View.fragment.FragmentConstants.NewsDetailFragment, true);
            }
            // 异步插入历史记录
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            new Thread(() -> {
                NewsHistory history = newsHistoryDao.getNewsHistory(newsItem.getUniquekey(), userId);
                if (history == null) {
                    history = new NewsHistory(userId, newsItem.getUniquekey(), newsItem.getTitle(), newsItem.getCategory(),
                            newsItem.getThumbnail_pic_s(), newsItem.getUrl(), newsItem.getAuthor_name(), newsItem.getDate());
                    newsHistoryDao.insert(history);
                } else {
                    history.setViewTime(System.currentTimeMillis());
                    newsHistoryDao.update(history);
                }
            }).start();
        });
        recyclerView.setAdapter(newsAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadNews);

        loadNews();
        return view;
    }

    public void loadNews() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            try {
                Call<NewsResponse> call = newsApi.getNewsList(WebAPI.API_KEY, type, 1, 15, 1);
                Response<NewsResponse> response = call.execute();
                NewsResponse newsResponse = response.isSuccessful() ? response.body() : null;
                requireActivity().runOnUiThread(() -> {
                    if (newsResponse != null && newsResponse.getResult() != null && newsResponse.getResult().getData() != null) {
                        newsAdapter.updateNewsList(newsResponse.getResult().getData());
                    } else {
                        Toast.makeText(requireContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(requireContext(), "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
} 