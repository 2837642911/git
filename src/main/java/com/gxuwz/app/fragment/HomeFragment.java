package com.gxuwz.app.fragment;

import static com.gxuwz.app.network.WebAPI.API_KEY;

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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.gxuwz.app.R;
import com.gxuwz.app.activity.MainActivity;
import com.gxuwz.app.adapter.NewsAdapter;
import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.network.NewsItem;
import com.gxuwz.app.model.network.NewsResponse;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.network.ResponseHandler;
import com.gxuwz.app.network.RetrofitClient;
import com.gxuwz.app.utils.SessionManager;

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


    private static final int PAGE_SIZE = 15;
    private static final int IS_FILTER = 1;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private NewsApi newsApi;
    private Random random = new Random();
    private Call<NewsResponse> currentCall;
    private TabLayout tabLayout;
    private String[] tabTypes = {"top", "guonei", "guoji"};
    private String[] tabTitles = {"推荐", "国内", "国际"};
    private String currentType = "top";

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

        tabLayout = view.findViewById(R.id.tab_layout);
        for (String title : tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                currentType = tabTypes[pos];
                loadNews(currentType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        recyclerView = view.findViewById(R.id.recycler_view_news);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // 初始化适配器
        newsAdapter = new NewsAdapter(new ArrayList<>(), newsItem -> {
            Log.d(TAG, "onItemClick: News item clicked: " + newsItem.getTitle());
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setCurrentNewsItem(newsItem);


                // 保存新闻到历史记录（异步操作）
                saveNewsToHistory(newsItem);


                ViewPager2 viewPager = mainActivity.findViewById(R.id.viewPager);
                if (viewPager != null) {
                    viewPager.setCurrentItem(FragmentConstants.NewsDetailFragment, true); // 切换到详情页
                }
            }
        });
        recyclerView.setAdapter(newsAdapter);

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> loadNews(currentType));

        // 首次加载数据
        loadNews();

        return view;
    }

    private void loadNews(String type) {
        Log.d(TAG, "loadNews: type=" + type);
        int page = random.nextInt(50) + 1;
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        swipeRefreshLayout.setRefreshing(true);
        currentCall = newsApi.getNewsList(API_KEY, type, page, PAGE_SIZE, IS_FILTER);
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
                    NewsResponse.Result result = response.getResult();
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

    private void loadNews() {
        loadNews(currentType);
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




    private void saveNewsToHistory(NewsItem newsItem) {
        // 获取当前用户ID（从SessionManager或其他地方获取）
        int userId = SessionManager.getInstance(requireContext()).getUserId();

        // 创建NewsHistory对象
        NewsHistory newsHistory = new NewsHistory(
                userId,
                newsItem.getUniquekey(), // 假设NewsItem有getUniquekey()方法
                newsItem.getTitle(),
                newsItem.getCategory(),
                newsItem.getThumbnail_pic_s(),
                newsItem.getUrl(),
                newsItem.getAuthor_name(),
                newsItem.getDate()
        );


                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    NewsHistoryDao dao = db.newsHistoryDao();

                    // 检查新闻是否已存在（避免重复记录）
                    NewsHistory existingNews = dao.getNewsHistory(
                            newsItem.getUniquekey(),
                            userId
                    );

                    if (existingNews == null) {
                        // 新闻不存在，插入新记录
                        dao.insert(newsHistory);
                        Log.d(TAG, "saveNewsToHistory: 新闻已保存到历史记录");
                    } else {
                        // 新闻已存在，更新浏览时间
                        existingNews.setViewTime(System.currentTimeMillis());
                        dao.update(existingNews);
                        Log.d(TAG, "saveNewsToHistory: 新闻已存在，更新浏览时间");
                    }
                }
    }
