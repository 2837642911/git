package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gxuwz.app.R;
import com.gxuwz.app.activity.MainActivity;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.model.network.NewsResponse;
import com.gxuwz.app.network.RetrofitClient;
import com.gxuwz.app.network.WebAPI;

import java.util.ArrayList;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;

public class NewsListFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private String type;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private NewsApi newsApi;
    private Random random = new Random();

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_record, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        newsAdapter = new NewsAdapter(new ArrayList<>(), newsItem -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setCurrentNewsItem(newsItem);
                ((MainActivity) getActivity()).replaceFragment(com.gxuwz.app.fragment.FragmentConstants.NewsDetailFragment, true);
            }
        });
        recyclerView.setAdapter(newsAdapter);
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        loadNews();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 控制TabLayout显示/隐藏
        View tabLayout = view.findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            if ("history".equals(type) || "favorite".equals(type)) {
                tabLayout.setVisibility(View.VISIBLE);
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }
        loadNews();
    }

    public void loadNews() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            try {
                int page = random.nextInt(50) + 1;
                Call<NewsResponse> call = newsApi.getNewsList(WebAPI.API_KEY, type, page, 15, 1);
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