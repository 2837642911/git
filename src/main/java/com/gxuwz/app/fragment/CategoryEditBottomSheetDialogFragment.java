package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gxuwz.app.R;
import com.gxuwz.app.utils.CategoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryEditBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private Runnable onSaveCallback;
    public CategoryEditBottomSheetDialogFragment(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
    }

    private ChannelAdapter myAdapter, moreAdapter;
    private List<String> myTypes, myTitles, moreTypes, moreTitles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_category_edit_bottom_sheet, container, false);
        RecyclerView rvMy = view.findViewById(R.id.rv_my_channels);
        RecyclerView rvMore = view.findViewById(R.id.rv_more_channels);
        Button btnSave = view.findViewById(R.id.btn_save);
        TextView tvMy = view.findViewById(R.id.tv_my_channels);
        TextView tvMore = view.findViewById(R.id.tv_more_channels);

        myTypes = new ArrayList<>(CategoryManager.getTypes(requireContext()));
        myTitles = new ArrayList<>(CategoryManager.getTitles(requireContext()));
        List<String> allTypes = CategoryManager.getAllTypes();
        List<String> allTitles = CategoryManager.getAllTitles();
        moreTypes = new ArrayList<>();
        moreTitles = new ArrayList<>();
        for (int i = 0; i < allTypes.size(); i++) {
            if (!myTypes.contains(allTypes.get(i))) {
                moreTypes.add(allTypes.get(i));
                moreTitles.add(allTitles.get(i));
            }
        }

        myAdapter = new ChannelAdapter(myTitles, true);
        moreAdapter = new ChannelAdapter(moreTitles, false);
        rvMy.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMore.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMy.setAdapter(myAdapter);
        rvMore.setAdapter(moreAdapter);

        // 拖拽排序
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (from == 0 || to == 0) return false; // 第一个频道不可拖动
                Collections.swap(myTypes, from, to);
                Collections.swap(myTitles, from, to);
                myAdapter.notifyItemMoved(from, to);
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
            @Override
            public boolean isLongPressDragEnabled() { return true; }
        });
        helper.attachToRecyclerView(rvMy);

        myAdapter.setOnItemClickListener(pos -> {
            if (myTypes.size() <= 1) {
                Toast.makeText(requireContext(), "至少保留一个频道", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pos == 0) return; // 第一个频道不可移除
            moreTypes.add(myTypes.get(pos));
            moreTitles.add(myTitles.get(pos));
            myTypes.remove(pos);
            myTitles.remove(pos);
            myAdapter.notifyDataSetChanged();
            moreAdapter.notifyDataSetChanged();
        });
        moreAdapter.setOnItemClickListener(pos -> {
            myTypes.add(moreTypes.get(pos));
            myTitles.add(moreTitles.get(pos));
            moreTypes.remove(pos);
            moreTitles.remove(pos);
            myAdapter.notifyDataSetChanged();
            moreAdapter.notifyDataSetChanged();
        });

        btnSave.setOnClickListener(v -> {
            if (myTypes.isEmpty()) {
                Toast.makeText(requireContext(), "至少选择一个频道", Toast.LENGTH_SHORT).show();
                return;
            }
            CategoryManager.saveCategories(requireContext(), myTypes, myTitles);
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
            if (onSaveCallback != null) onSaveCallback.run();
            dismiss();
        });
        return view;
    }

    // 频道适配器
    private static class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {
        private List<String> data;
        private boolean isMy;
        private OnItemClickListener listener;
        public ChannelAdapter(List<String> data, boolean isMy) {
            this.data = data;
            this.isMy = isMy;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_chip, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.chip.setText(data.get(position));
            holder.chip.setChecked(isMy);
            holder.chip.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(position);
            });
        }
        @Override
        public int getItemCount() { return data.size(); }
        public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }
        static class ViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.chip.Chip chip;
            ViewHolder(View itemView) {
                super(itemView);
                chip = itemView.findViewById(R.id.chip_channel);
            }
        }
        interface OnItemClickListener { void onItemClick(int pos); }
    }
} 