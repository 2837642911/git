package com.gxuwz.app.activity;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.app.R;
import com.gxuwz.app.fragment.HomeFragment;
import com.gxuwz.app.fragment.MeFragment;


/**
 * 主页
 */
public class MainActivity extends AppCompatActivity {
    private static final int NUM_PAGES = 2;
    private ViewPager2 viewPager;
    private RadioGroup rg_bottom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager=findViewById(R.id.viewPager);
        rg_bottom=findViewById(R.id.rg_bottom);
        viewPager.setAdapter(new PagerAdapter(this));
        rg_bottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_home) {
                    viewPager.setCurrentItem(0);
                } else if (i == R.id.rb_me) {
                    viewPager.setCurrentItem(1);
                }
            }
        });
        //设置页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        rg_bottom.check(R.id.rb_home);
                        break;
                    case 1:
                        rg_bottom.check(R.id.rb_me);
                        break;


                }
            }
        });
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
            Fragment fragment=null;
            switch (position){
                case 0:
                    fragment=new HomeFragment();
                    break;
                case 1:
                    fragment=new MeFragment();
                    break;
                default:
                    fragment=new HomeFragment();

            }
            return fragment;
        }
    }
}