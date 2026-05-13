package com.elephant.safety.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.elephant.safety.R;
import com.elephant.safety.models.DashboardStats;

public class StatsFragment extends Fragment {

    private static final String ARG_STATS = "stats";
    private DashboardStats stats;

    private TextView tvTotalReports, tvApprovedReports, tvTotalUsers, tvDangerZones, tvReportsThisWeek, tvTopDistrict;

    public static StatsFragment newInstance(DashboardStats stats) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATS, stats);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stats = (DashboardStats) getArguments().getSerializable(ARG_STATS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvTotalReports = view.findViewById(R.id.tvTotalReports);
        tvApprovedReports = view.findViewById(R.id.tvApprovedReports);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvDangerZones = view.findViewById(R.id.tvDangerZones);
        tvReportsThisWeek = view.findViewById(R.id.tvReportsThisWeek);
        tvTopDistrict = view.findViewById(R.id.tvTopDistrict);

        if (stats != null) {
            tvTotalReports.setText(String.valueOf(stats.getTotalReports()));
            tvApprovedReports.setText(String.valueOf(stats.getApprovedReports()));
            tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
            tvDangerZones.setText(String.valueOf(stats.getDangerZones()));
            tvReportsThisWeek.setText(String.valueOf(stats.getReportsThisWeek()));
            tvTopDistrict.setText(stats.getTopDistrict() != null ? stats.getTopDistrict() : "N/A");
        }

        return view;
    }
}