package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gxuwz.app.R;
import com.gxuwz.app.adapter.ChannelPagerAdapter;
import com.gxuwz.app.utils.CategoryManager;

import java.util.List;

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

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ChannelPagerAdapter pagerAdapter;
    private List<String> tabTypes, tabTitles;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        tabTypes = CategoryManager.getTypes(requireContext());
        tabTitles = CategoryManager.getTitles(requireContext());
        pagerAdapter = new ChannelPagerAdapter(this, tabTypes);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles.get(position));
        }).attach();
        View btnEditCategory = view.findViewById(R.id.btn_edit_category);
        if (btnEditCategory != null) {
            btnEditCategory.setOnClickListener(v -> {
                CategoryEditBottomSheetDialogFragment dialog = new CategoryEditBottomSheetDialogFragment(() -> {
                    tabTypes = CategoryManager.getTypes(requireContext());
                    tabTitles = CategoryManager.getTitles(requireContext());
                    pagerAdapter.setTypes(tabTypes);
                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        tab.setText(tabTitles.get(position));
                    }).attach();
                });
                dialog.show(getChildFragmentManager(), "CategoryEditBottomSheetDialog");
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: Fragment view being destroyed");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Fragment being destroyed");
        super.onDestroy();
    }

}
