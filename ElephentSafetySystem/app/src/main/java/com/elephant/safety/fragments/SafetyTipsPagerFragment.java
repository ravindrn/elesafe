package com.elephant.safety.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.elephant.safety.R;
import com.elephant.safety.adapters.SafetyTipsCategoryPagerAdapter;
import com.elephant.safety.models.SafetyTip;

import java.util.ArrayList;
import java.util.List;

public class SafetyTipsPagerFragment extends Fragment {

    private static final String ARG_DRIVING_TIPS = "driving_tips";
    private static final String ARG_ENCOUNTER_TIPS = "encounter_tips";
    private static final String ARG_EMERGENCY_TIPS = "emergency_tips";
    private static final String ARG_GENERAL_TIPS = "general_tips";

    private List<SafetyTip> drivingTips = new ArrayList<>();
    private List<SafetyTip> encounterTips = new ArrayList<>();
    private List<SafetyTip> emergencyTips = new ArrayList<>();
    private List<SafetyTip> generalTips = new ArrayList<>();

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private final String[] categoryTitles = {
            "🚗 DRIVING",
            "🐘 ENCOUNTER",
            "🚨 EMERGENCY",
            "📋 GENERAL"
    };

    public static SafetyTipsPagerFragment newInstance(List<SafetyTip> drivingTips,
                                                      List<SafetyTip> encounterTips,
                                                      List<SafetyTip> emergencyTips,
                                                      List<SafetyTip> generalTips) {
        SafetyTipsPagerFragment fragment = new SafetyTipsPagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DRIVING_TIPS, new ArrayList<>(drivingTips));
        args.putSerializable(ARG_ENCOUNTER_TIPS, new ArrayList<>(encounterTips));
        args.putSerializable(ARG_EMERGENCY_TIPS, new ArrayList<>(emergencyTips));
        args.putSerializable(ARG_GENERAL_TIPS, new ArrayList<>(generalTips));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            drivingTips = (List<SafetyTip>) getArguments().getSerializable(ARG_DRIVING_TIPS);
            encounterTips = (List<SafetyTip>) getArguments().getSerializable(ARG_ENCOUNTER_TIPS);
            emergencyTips = (List<SafetyTip>) getArguments().getSerializable(ARG_EMERGENCY_TIPS);
            generalTips = (List<SafetyTip>) getArguments().getSerializable(ARG_GENERAL_TIPS);

            if (drivingTips == null) drivingTips = new ArrayList<>();
            if (encounterTips == null) encounterTips = new ArrayList<>();
            if (emergencyTips == null) emergencyTips = new ArrayList<>();
            if (generalTips == null) generalTips = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safety_tips_pager, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        setupViewPager();

        return view;
    }

    private void setupViewPager() {
        List<List<SafetyTip>> categories = new ArrayList<>();
        categories.add(drivingTips);
        categories.add(encounterTips);
        categories.add(emergencyTips);
        categories.add(generalTips);

        // Convert List<List<SafetyTip>> to List<Object>
        List<Object> objectCategories = new ArrayList<>(categories);

        SafetyTipsCategoryPagerAdapter adapter = new SafetyTipsCategoryPagerAdapter(this, objectCategories, categoryTitles);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(categoryTitles[position])
        ).attach();
    }
}