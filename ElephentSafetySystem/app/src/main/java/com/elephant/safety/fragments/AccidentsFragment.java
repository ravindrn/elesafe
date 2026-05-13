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
import com.elephant.safety.models.NewsItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AccidentsFragment extends Fragment {

    private static final String ARG_ACCIDENTS = "accidents";
    private List<NewsItem> accidentsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private AccidentsAdapter adapter;

    public static AccidentsFragment newInstance(List<NewsItem> accidentsList) {
        AccidentsFragment fragment = new AccidentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACCIDENTS, new ArrayList<>(accidentsList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accidentsList = (List<NewsItem>) getArguments().getSerializable(ARG_ACCIDENTS);
            if (accidentsList == null) accidentsList = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accidents, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AccidentsAdapter();
        recyclerView.setAdapter(adapter);

        if (accidentsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    class AccidentsAdapter extends RecyclerView.Adapter<AccidentsAdapter.AccidentViewHolder> {

        @NonNull
        @Override
        public AccidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_accident, parent, false);
            return new AccidentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AccidentViewHolder holder, int position) {
            NewsItem accident = accidentsList.get(position);
            holder.bind(accident);
        }

        @Override
        public int getItemCount() {
            return accidentsList.size();
        }

        class AccidentViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle, tvContent, tvSource, tvDate;
            private MaterialCardView cardView;

            AccidentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvContent = itemView.findViewById(R.id.tvContent);
                tvSource = itemView.findViewById(R.id.tvSource);
                tvDate = itemView.findViewById(R.id.tvDate);
                cardView = itemView.findViewById(R.id.cardView);
            }

            void bind(NewsItem accident) {
                tvTitle.setText("⚠️ " + accident.getTitle());
                tvContent.setText(accident.getContent());
                tvSource.setText("📰 " + accident.getSource());
                tvDate.setText("📅 " + accident.getDate());
            }
        }
    }
}