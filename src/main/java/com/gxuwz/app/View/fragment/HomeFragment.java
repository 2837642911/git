package com.gxuwz.app.View.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gxuwz.app.R;
import com.gxuwz.app.View.activity.MainActivity;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.network.NewsResponse;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.network.RetrofitClient;
import com.gxuwz.app.network.WebAPI;
import com.gxuwz.app.utils.CategoryManager;
import com.gxuwz.app.utils.SessionManager;
import com.gxuwz.app.View.fragment.CategoryEditBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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


    private static final int PAGE_SIZE = 15;
    private static final int IS_FILTER = 1;

    private ViewPager2 viewPager;
    private ChannelPagerAdapter pagerAdapter;
    private NewsApi newsApi;
    private NewsHistoryDao newsHistoryDao;
    private Random random = new Random();
    private TabLayout tabLayout;
    private List<String> tabTypes;
    private List<String> tabTitles;
    private String currentType = "top";
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

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
        newsHistoryDao = AppDatabase.getInstance(requireContext()).newsHistoryDao();
        tabTypes = CategoryManager.getTypes(requireContext());
        tabTitles = CategoryManager.getTitles(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        ImageButton btnEditCategory = view.findViewById(R.id.btn_edit_category);
        btnEditCategory.setOnClickListener(v -> {
            CategoryEditBottomSheetDialogFragment dialog = new CategoryEditBottomSheetDialogFragment(() -> {
                // 保存后刷新频道
                tabTypes = CategoryManager.getTypes(requireContext());
                tabTitles = CategoryManager.getTitles(requireContext());
                pagerAdapter.setTypes(tabTypes);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    tab.setText(tabTitles.get(position));
                }).attach();
            });
            dialog.show(getChildFragmentManager(), "CategoryEditBottomSheetDialog");
        });

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        viewPager = view.findViewById(R.id.view_pager);
        pagerAdapter = new ChannelPagerAdapter(this, tabTypes);
        viewPager.setAdapter(pagerAdapter);

        // TabLayout与ViewPager2联动
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles.get(position));
        }).attach();

        // 外层下拉刷新，刷新当前频道
        swipeRefreshLayout.setOnRefreshListener(() -> {
            int pos = viewPager.getCurrentItem();
            NewsListFragment fragment = (NewsListFragment) getChildFragmentManager().findFragmentByTag("f" + pos);
            if (fragment != null) {
                fragment.loadNews();
            } else {
                // 兼容ViewPager2 fragment未attach时
                pagerAdapter.forceRefresh(pos);
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次返回都刷新Tab
        if (tabLayout != null) {
            refreshTabs();
        }
    }

    private void refreshTabs() {
        tabLayout.removeAllTabs();
        for (String title : tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                currentType = tabTypes.get(pos);
                loadNews(currentType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        // 默认只加载第一个Tab
        if (!tabTypes.isEmpty()) {
            currentType = tabTypes.get(0);
            loadNews(currentType);
        }
    }

    private void loadNews(String type) {
        Log.d(TAG, "loadNews: type=" + type);
        int page = random.nextInt(50) + 1;
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            try {
                Call<NewsResponse> call = newsApi.getNewsList(WebAPI.API_KEY, type, page, PAGE_SIZE, IS_FILTER);
                retrofit2.Response<NewsResponse> response = call.execute();
                NewsResponse newsResponse = response.isSuccessful() ? response.body() : null;
                requireActivity().runOnUiThread(() -> {
                    if (getActivity() == null) {
                        Log.w(TAG, "onSuccess: Activity is null");
                        return;
                    }
                    if (newsResponse != null && newsResponse.getResult() != null && newsResponse.getResult().getData() != null) {
                        Log.d(TAG, "onSuccess: Updating adapter with " + newsResponse.getResult().getData().size() + " items");
                        // 更新适配器逻辑
                    } else {
                        Log.w(TAG, "onSuccess: Result or data is null");
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(requireContext(), "加载新闻失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: Fragment view being destroyed");
        super.onDestroyView();
        // 清除引用
        viewPager = null;
        swipeRefreshLayout = null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Fragment being destroyed");
        super.onDestroy();
    }

    private void saveNewsToHistory(NewsItem newsItem) {
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
    }
}

// 新增频道适配器
class ChannelPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
    private List<String> types;
    public ChannelPagerAdapter(@NonNull Fragment fragment, List<String> types) {
        super(fragment);
        this.types = types;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return NewsListFragment.newInstance(types.get(position));
    }
    @Override
    public int getItemCount() {
        return types.size();
    }
    // 可选：强制刷新某一页
    public void forceRefresh(int position) {
        notifyItemChanged(position);
    }
    public void setTypes(List<String> types) {
        this.types = types;
        notifyDataSetChanged();
    }
}
