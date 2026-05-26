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
import com.elephant.safety.models.SafetyTip;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class SafetyTipsCategoryFragment extends Fragment {

    private static final String ARG_TIPS = "tips";
    private static final String ARG_CATEGORY_TITLE = "category_title";

    private List<SafetyTip> tips = new ArrayList<>();
    private String categoryTitle;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private TipsAdapter adapter;

    public static SafetyTipsCategoryFragment newInstance(List<SafetyTip> tips, String categoryTitle) {
        SafetyTipsCategoryFragment fragment = new SafetyTipsCategoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIPS, new ArrayList<>(tips));
        args.putString(ARG_CATEGORY_TITLE, categoryTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tips = (List<SafetyTip>) getArguments().getSerializable(ARG_TIPS);
            categoryTitle = getArguments().getString(ARG_CATEGORY_TITLE);
            if (tips == null) tips = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safety_tips_category, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TipsAdapter();
        recyclerView.setAdapter(adapter);

        if (tips.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No " + categoryTitle + " available");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipViewHolder> {

        @NonNull
        @Override
        public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_safety_tip, parent, false);
            return new TipViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
            SafetyTip tip = tips.get(position);
            holder.bind(tip);
        }

        @Override
        public int getItemCount() {
            return tips.size();
        }

        class TipViewHolder extends RecyclerView.ViewHolder {
            private TextView tvNumber, tvTitle, tvDescription;
            private MaterialCardView cardView;

            TipViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNumber = itemView.findViewById(R.id.tvNumber);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                cardView = itemView.findViewById(R.id.cardView);
            }

            void bind(SafetyTip tip) {
                tvNumber.setText(String.valueOf(getAdapterPosition() + 1));
                tvTitle.setText(tip.getTitle());
                tvDescription.setText(tip.getDescription());

                // Set card background color based on category
                if (categoryTitle.contains("DRIVING")) {
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.driving_color_light));
                } else if (categoryTitle.contains("ENCOUNTER")) {
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.encounter_color_light));
                } else if (categoryTitle.contains("EMERGENCY")) {
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.emergency_color_light));
                } else {
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.general_color_light));
                }
            }
        }
    }
}