package com.gxuwz.app.activity;


import static com.gxuwz.app.fragment.FragmentConstants.HomePositionGroup;
import static com.gxuwz.app.fragment.FragmentConstants.MePositionGroup;
import static com.gxuwz.app.fragment.FragmentConstants.NUM_PAGES;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.app.R;
import com.gxuwz.app.fragment.FragmentConstants;
import com.gxuwz.app.fragment.HomeFragment;
import com.gxuwz.app.fragment.MeFragment;
import com.gxuwz.app.fragment.NewsDetailFragment;
import com.gxuwz.app.fragment.VersionFragment;
import com.gxuwz.app.model.network.NewsItem;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private RadioGroup rg_bottom;
    private NewsItem currentNewsItem; // 用于详情页展示



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        rg_bottom = findViewById(R.id.rg_bottom);
        viewPager.setAdapter(new PagerAdapter(this));

        rg_bottom.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rb_home) {
                viewPager.setCurrentItem(FragmentConstants.HomeFragment);
            } else if (i == R.id.rb_me) {
                viewPager.setCurrentItem(FragmentConstants.MeFragment);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position <=HomePositionGroup) {
                    rg_bottom.check(R.id.rb_home);
                } else if (position <=MePositionGroup) {
                    rg_bottom.check(R.id.rb_me);
                } else {
                    rg_bottom.clearCheck(); // 版本页时底部栏不高亮
                }
            }
        });
    }

    public void setCurrentNewsItem(NewsItem item) {
        this.currentNewsItem = item;
    }
    public NewsItem getCurrentNewsItem() {
        return currentNewsItem;
    }

    class PagerAdapter extends FragmentStateAdapter {
        public PagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            switch (position) {
                case FragmentConstants.HomeFragment:
                    fragment = new HomeFragment();
                    break;
                case FragmentConstants.NewsDetailFragment:
                    fragment = new NewsDetailFragment();
                    break;
                case FragmentConstants.MeFragment:
                    fragment = new MeFragment();
                    break;
                case FragmentConstants.VersionFragment:
                    fragment = new VersionFragment();
                    break;

                default:
                    fragment = new HomeFragment();
                    break;
            }
            return fragment;
        }
    }
}