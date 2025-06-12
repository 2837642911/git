package com.gxuwz.app.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.gxuwz.app.R;
import com.gxuwz.app.utils.CategoryManager;

import java.util.ArrayList;
import java.util.List;

public class CategoryEditFragment extends Fragment {
    private ChipGroup chipGroup;
    private Button btnSave;
    private List<String> allTypes;
    private List<String> allTitles;
    private List<String> selectedTypes;
    private List<String> selectedTitles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_edit, container, false);
        chipGroup = view.findViewById(R.id.chip_group);
        btnSave = view.findViewById(R.id.btn_save);

        allTypes = CategoryManager.getAllTypes();
        allTitles = CategoryManager.getAllTitles();
        selectedTypes = new ArrayList<>(CategoryManager.getTypes(requireContext()));
        selectedTitles = new ArrayList<>(CategoryManager.getTitles(requireContext()));

        chipGroup.removeAllViews();
        for (int i = 0; i < allTypes.size(); i++) {
            String type = allTypes.get(i);
            String title = allTitles.get(i);
            Chip chip = new Chip(requireContext());
            chip.setText(title);
            chip.setCheckable(true);
            chip.setChecked(selectedTypes.contains(type));
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    if (!selectedTypes.contains(type)) {
                        selectedTypes.add(type);
                        selectedTitles.add(title);
                    }
                } else {
                    int idx = selectedTypes.indexOf(type);
                    if (idx >= 0) {
                        selectedTypes.remove(idx);
                        selectedTitles.remove(idx);
                    }
                }
            });
            chipGroup.addView(chip);
        }

        btnSave.setOnClickListener(v -> {
            if (selectedTypes.isEmpty()) {
                Toast.makeText(requireContext(), "至少选择一个分类", Toast.LENGTH_SHORT).show();
                return;
            }
            CategoryManager.saveCategories(requireContext(), selectedTypes, selectedTitles);
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        return view;
    }
} 