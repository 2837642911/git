package com.gxuwz.app.activity;

import static com.gxuwz.app.fragment.FragmentConstants.HomePositionGroup;
import static com.gxuwz.app.fragment.FragmentConstants.MePositionGroup;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gxuwz.app.R;
import com.gxuwz.app.fragment.FragmentConstants;
import com.gxuwz.app.fragment.HomeFragment;
import com.gxuwz.app.fragment.MeFragment;
import com.gxuwz.app.fragment.NewsDetailFragment;
import com.gxuwz.app.fragment.NewsProfileFragment;
import com.gxuwz.app.fragment.SettingFragment;
import com.gxuwz.app.fragment.UpdateFragment;
import com.gxuwz.app.fragment.VersionFragment;
import com.gxuwz.app.model.network.NewsItem;

public class MainActivity extends AppCompatActivity {

    private RadioGroup rg_bottom;
    private NewsItem currentNewsItem;
    private int currentFragmentType = FragmentConstants.HomeFragment;

    // 排除规则：不显示头条栏的Fragment类型
    private static final int[] EXCLUDE_TOP_BAR = {
        FragmentConstants.UpdateFragment,
        FragmentConstants.SettingFragment

    };

    // 排除规则：不显示底部栏的Fragment类型
    private static final int[] EXCLUDE_BOTTOM_BAR = {
        FragmentConstants.NewsDetailFragment

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_bottom = findViewById(R.id.rg_bottom);

        // 默认显示首页Fragment
        if (savedInstanceState == null) {
            replaceFragment(FragmentConstants.HomeFragment, false);
        }

        rg_bottom.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rb_home) {
                replaceFragment(FragmentConstants.HomeFragment, false);
            } else if (i == R.id.rb_me) {
                replaceFragment(FragmentConstants.MeFragment, false);
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fl_container);
            int fragmentType = getFragmentType(current);
            updateUIByPosition(fragmentType);
        });
    }

    public void replaceFragment(int fragmentType, boolean addToBackStack) {
            Fragment fragment = null;
        switch (fragmentType) {
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
                case FragmentConstants.ProfileRecordFragment:
                    fragment = new NewsProfileFragment();
                    break;
                case FragmentConstants.SettingFragment:
                    fragment = new SettingFragment();
                    break;
                case FragmentConstants.UpdateFragment:
                    fragment = new UpdateFragment();
                    break;
                default:
                    fragment = new HomeFragment();
                    break;
            }
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right
            );
            transaction.replace(R.id.fl_container, fragment);
            if (addToBackStack) {
                transaction.addToBackStack(null);
        }
            transaction.commit();
            currentFragmentType = fragmentType;
            updateUIByPosition(fragmentType);
        }
    }

    private void updateUIByPosition(int position) {
        View flTopBar = findViewById(R.id.fl_top_bar);
        boolean excludeTopBar = false;
        for (int exclude : EXCLUDE_TOP_BAR) {
            if (position == exclude) {
                excludeTopBar = true;
                break;
            }
        }
        if (excludeTopBar) {
            if (flTopBar != null) flTopBar.setVisibility(View.GONE);
        } else {
            if (flTopBar != null) flTopBar.setVisibility(View.VISIBLE);
        }
        // 底部栏排除规则
        boolean excludeBottomBar = false;
        for (int exclude : EXCLUDE_BOTTOM_BAR) {
            if (position == exclude) {
                excludeBottomBar = true;
                break;
            }
        }
        if (excludeBottomBar) {
            if (rg_bottom != null) rg_bottom.setVisibility(View.GONE);
        } else {
            if (rg_bottom != null) rg_bottom.setVisibility(View.VISIBLE);
            if (rg_bottom != null) {
                if (position < HomePositionGroup) {
                    rg_bottom.check(R.id.rb_home);
                } else if (position < MePositionGroup) {
                    rg_bottom.check(R.id.rb_me);
                } else {
                    rg_bottom.clearCheck();
                }
            }
        }
    }

    public void setCurrentNewsItem(NewsItem item) {
        this.currentNewsItem = item;
    }

    public NewsItem getCurrentNewsItem() {
        return currentNewsItem;
    }

    private int getFragmentType(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            return FragmentConstants.HomeFragment;
        } else if (fragment instanceof NewsDetailFragment) {
            return FragmentConstants.NewsDetailFragment;
        } else if (fragment instanceof MeFragment) {
            return FragmentConstants.MeFragment;
        } else if (fragment instanceof VersionFragment) {
            return FragmentConstants.VersionFragment;
        } else if (fragment instanceof NewsProfileFragment) {
            return FragmentConstants.ProfileRecordFragment;
        } else if (fragment instanceof SettingFragment) {
            return FragmentConstants.SettingFragment;
        } else if (fragment instanceof UpdateFragment) {
            return FragmentConstants.UpdateFragment;
        }
        return FragmentConstants.HomeFragment; // 默认
    }
}