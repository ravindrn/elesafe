package com.elephant.safety.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elephant.safety.R;
import com.elephant.safety.models.NewsItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private static final String ARG_NEWS = "news";
    private List<NewsItem> newsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private NewsAdapter adapter;

    public static NewsFragment newInstance(List<NewsItem> newsList) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NEWS, new ArrayList<>(newsList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsList = (List<NewsItem>) getArguments().getSerializable(ARG_NEWS);
            if (newsList == null) newsList = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NewsAdapter();
        recyclerView.setAdapter(adapter);

        if (newsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            NewsItem news = newsList.get(position);
            holder.bind(news);
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }

        class NewsViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle, tvContent, tvSource, tvDate;
            private ImageView ivIcon;
            private MaterialCardView cardView;

            NewsViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvContent = itemView.findViewById(R.id.tvContent);
                tvSource = itemView.findViewById(R.id.tvSource);
                tvDate = itemView.findViewById(R.id.tvDate);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                cardView = itemView.findViewById(R.id.cardView);
            }

            void bind(NewsItem news) {
                tvTitle.setText(news.getTitle());
                tvContent.setText(news.getContent());
                tvSource.setText("📰 " + news.getSource());
                tvDate.setText("📅 " + news.getDate());

                // Set icon based on type
                if (news.getType().equals("CONSERVATION")) {
                    ivIcon.setImageResource(R.drawable.ic_verified);
                    cardView.setStrokeColor(getResources().getColor(R.color.success));
                } else if (news.getType().equals("ALERT")) {
                    ivIcon.setImageResource(R.drawable.ic_warning_alert);
                    cardView.setStrokeColor(getResources().getColor(R.color.danger));
                } else {
                    ivIcon.setImageResource(R.drawable.ic_info);
                }
            }
        }
    }
}