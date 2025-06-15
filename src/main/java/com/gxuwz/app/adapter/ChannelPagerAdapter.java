package com.gxuwz.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gxuwz.app.fragment.NewsListFragment;

import java.util.List;

// 新增频道适配器
public class ChannelPagerAdapter extends FragmentStateAdapter {
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
    public void setTypes(List<String> types) {
        this.types = types;
        notifyDataSetChanged();
    }
}