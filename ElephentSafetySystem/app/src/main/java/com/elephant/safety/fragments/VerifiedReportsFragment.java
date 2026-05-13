package com.elephant.safety.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elephant.safety.R;
import com.elephant.safety.models.VerifiedReport;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VerifiedReportsFragment extends Fragment {

    private static final String ARG_REPORTS = "reports";
    private List<VerifiedReport> reports = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ReportsAdapter adapter;

    public static VerifiedReportsFragment newInstance(List<VerifiedReport> reports) {
        VerifiedReportsFragment fragment = new VerifiedReportsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORTS, new ArrayList<>(reports));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reports = (List<VerifiedReport>) getArguments().getSerializable(ARG_REPORTS);
            if (reports == null) reports = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verified_reports, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportsAdapter();
        recyclerView.setAdapter(adapter);

        if (reports.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_verified_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            VerifiedReport report = reports.get(position);
            holder.bind(report);
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            private TextView tvReporter, tvLocation, tvNote, tvElephantCount, tvDate;
            private MaterialCardView cardView;

            ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                tvReporter = itemView.findViewById(R.id.tvReporter);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvNote = itemView.findViewById(R.id.tvNote);
                tvElephantCount = itemView.findViewById(R.id.tvElephantCount);
                tvDate = itemView.findViewById(R.id.tvDate);
                cardView = itemView.findViewById(R.id.cardView);
            }

            void bind(VerifiedReport report) {
                tvReporter.setText("✓ " + report.getUserName());
                tvLocation.setText(String.format("📍 %.4f, %.4f", report.getLatitude(), report.getLongitude()));
                tvNote.setText(report.getNote());
                tvElephantCount.setText("🐘 " + report.getElephantCount() + " elephant(s)");
                tvDate.setText("📅 " + formatDate(report.getCreatedAt()));

                // Highlight based on elephant count
                if (report.getElephantCount() >= 5) {
                    if (cardView != null) {
                        cardView.setStrokeColor(getResources().getColor(R.color.danger));
                        cardView.setStrokeWidth(2);
                    }
                }
            }

            private String formatDate(String dateStr) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    Date date = inputFormat.parse(dateStr);
                    return outputFormat.format(date);
                } catch (Exception e) {
                    return dateStr;
                }
            }
        }
    }
}