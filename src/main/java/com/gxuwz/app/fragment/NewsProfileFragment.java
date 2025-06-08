package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.gxuwz.app.R;
import com.gxuwz.app.activity.MainActivity;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NewsProfileFragment extends Fragment {
    private static final String TAG = "NewsProfileFragment";
    private static final String ARG_TYPE = "type";
    public static final String TYPE_HISTORY = "history";
    public static final String TYPE_FAVORITE = "favorite";
    private String mType = TYPE_HISTORY;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private TabLayout tabLayout;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private List<NewsHistory> allData = new ArrayList<>();

    private int selectedTab = 0; // 0: 历史记录, 1: 收藏

    public NewsProfileFragment() {
        super();
    }

    public static NewsProfileFragment newInstance(String type) {
        NewsProfileFragment fragment = new NewsProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE, TYPE_HISTORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_record, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.VISIBLE);

        // 添加Tab
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("历史记录"));
        tabLayout.addTab(tabLayout.newTab().setText("收藏记录"));
        tabLayout.selectTab(tabLayout.getTabAt(selectedTab));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                mType = (selectedTab == 0) ? TYPE_HISTORY : TYPE_FAVORITE;
                currentPage = 0;
                isLastPage = false;
                allData.clear();
                newsAdapter.updateNewsHistoryList(new ArrayList<>());
                loadLocalNews();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        newsAdapter = new NewsAdapter(new ArrayList<>(), newsItem -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setCurrentNewsItem(newsItem);
                ViewPager2 viewPager = mainActivity.findViewById(R.id.viewPager);
                if (viewPager != null) {
                    viewPager.setCurrentItem(FragmentConstants.NewsDetailFragment, true);
                }
            }
        });
        recyclerView.setAdapter(newsAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            allData.clear();
            newsAdapter.updateNewsHistoryList(new ArrayList<>());
            loadLocalNews();
        });

        // 分页加载监听
        recyclerView.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && !isLoading && !isLastPage) {
                    loadNextPage();
                }
            }
        });

        loadLocalNews();
        return view;
    }

    private void loadLocalNews() {
        isLoading = true;
        new Thread(() -> {
            int userId = SessionManager.getInstance(requireContext()).getUserId();
            AppDatabase db = AppDatabase.getInstance(requireContext());
            NewsHistoryDao dao = db.newsHistoryDao();
            List<NewsHistory> list;
            if (TYPE_FAVORITE.equals(mType)) {
                list = dao.getFavoriteNewsByUserId(userId);
            } else {
                list = dao.getNewsHistoryByUserId(userId);
            }
            allData.clear();
            allData.addAll(list);
            getActivity().runOnUiThread(() -> {
                newsAdapter.updateNewsHistoryList(getPageData(0));
                currentPage = 1;
                isLoading = false;
                isLastPage = allData.size() <= PAGE_SIZE;
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private void loadNextPage() {
        isLoading = true;
        recyclerView.post(() -> {
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, allData.size());
            if (start < end) {
                List<NewsHistory> nextPage = allData.subList(start, end);
                newsAdapter.addNewsHistoryList(nextPage);
                currentPage++;
                isLastPage = end == allData.size();
            } else {
                isLastPage = true;
            }
            isLoading = false;
        });
    }

    private List<NewsHistory> getPageData(int page) {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allData.size());
        if (start < end) {
            return new ArrayList<>(allData.subList(start, end));
        } else {
            return new ArrayList<>();
        }
    }
}
