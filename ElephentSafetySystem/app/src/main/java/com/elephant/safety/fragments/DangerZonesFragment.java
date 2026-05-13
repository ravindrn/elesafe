package com.elephant.safety.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elephant.safety.R;
import com.elephant.safety.activities.MainActivity;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.DangerZone;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangerZonesFragment extends Fragment {

    private EditText etSearch;
    private AutoCompleteTextView autoCompleteDistrict;
    private Button btnSearch, btnReset, btnViewMap;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvStats;
    private LinearLayout layoutDetails;
    private NestedScrollView scrollView;

    private ApiService apiService;
    private List<DangerZone> allDangerZones;
    private List<DangerZone> filteredZones;
    private DangerZoneAdapter adapter;

    private String[] districts = {"All Districts", "Polonnaruwa", "Anuradhapura", "Matale", "Kurunegala",
            "Batticaloa", "Ampara", "Monaragala", "Badulla", "Hambantota", "Ratnapura",
            "Puttalam", "Kegalle", "Colombo", "Gampaha", "Kalutara", "Galle", "Matara"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danger_zones, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadDangerZones();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        autoCompleteDistrict = view.findViewById(R.id.autoCompleteDistrict);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnReset = view.findViewById(R.id.btnReset);
        btnViewMap = view.findViewById(R.id.btnViewMap);
        rvResults = view.findViewById(R.id.rvResults);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        tvStats = view.findViewById(R.id.tvStats);
        layoutDetails = view.findViewById(R.id.layoutDetails);
        scrollView = view.findViewById(R.id.scrollView);

        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        allDangerZones = new ArrayList<>();
        filteredZones = new ArrayList<>();

        // Setup district dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, districts);
        autoCompleteDistrict.setAdapter(adapter);
        autoCompleteDistrict.setText("All Districts", false);
    }

    private void setupRecyclerView() {
        adapter = new DangerZoneAdapter();
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResults.setAdapter(adapter);
    }

    private void setupSearch() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnReset.setOnClickListener(v -> resetSearch());

        // FIXED: View on Map button - Navigate to MainActivity which has the map
        btnViewMap.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Navigate to MainActivity which contains the map
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Real-time search as user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDangerZones() {
        progressBar.setVisibility(View.VISIBLE);

        if (!SharedPrefManager.getInstance(getContext()).isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            CustomToast.showWarning(getContext(), "Please login to view danger zones");
            return;
        }

        apiService.getAllZones().enqueue(new Callback<List<DangerZone>>() {
            @Override
            public void onResponse(Call<List<DangerZone>> call, Response<List<DangerZone>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allDangerZones = response.body();
                    filteredZones = new ArrayList<>(allDangerZones);
                    adapter.updateData(filteredZones);

                    int highRisk = 0, critical = 0;
                    for (DangerZone zone : allDangerZones) {
                        if ("CRITICAL".equals(zone.getRiskLevel())) critical++;
                        else if ("HIGH".equals(zone.getRiskLevel())) highRisk++;
                    }

                    tvStats.setText(String.format("Total: %d zones | 🔴 Critical: %d | 🟠 High Risk: %d",
                            allDangerZones.size(), critical, highRisk));

                    tvNoResults.setVisibility(filteredZones.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    CustomToast.showError(getContext(), "Failed to load danger zones");
                }
            }

            @Override
            public void onFailure(Call<List<DangerZone>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                CustomToast.showError(getContext(), "Network error: " + t.getMessage());
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        String selectedDistrict = autoCompleteDistrict.getText().toString();

        filteredZones.clear();

        for (DangerZone zone : allDangerZones) {
            boolean matchesSearch = query.isEmpty() ||
                    zone.getZoneName().toLowerCase().contains(query) ||
                    (zone.getRoadName() != null && zone.getRoadName().toLowerCase().contains(query));

            boolean matchesDistrict = selectedDistrict.equals("All Districts") ||
                    selectedDistrict.equals(zone.getDistrict());

            if (matchesSearch && matchesDistrict) {
                filteredZones.add(zone);
            }
        }

        adapter.updateData(filteredZones);
        tvNoResults.setVisibility(filteredZones.isEmpty() ? View.VISIBLE : View.GONE);

        // Update stats
        int highRisk = 0, critical = 0;
        for (DangerZone zone : filteredZones) {
            if ("CRITICAL".equals(zone.getRiskLevel())) critical++;
            else if ("HIGH".equals(zone.getRiskLevel())) highRisk++;
        }
        tvStats.setText(String.format("Showing: %d zones | 🔴 Critical: %d | 🟠 High Risk: %d",
                filteredZones.size(), critical, highRisk));
    }

    private void resetSearch() {
        etSearch.setText("");
        autoCompleteDistrict.setText("All Districts", false);
        performSearch();
    }

    // ViewHolder for Danger Zone items
    class DangerZoneAdapter extends RecyclerView.Adapter<DangerZoneAdapter.ViewHolder> {

        private List<DangerZone> zones = new ArrayList<>();

        public void updateData(List<DangerZone> newZones) {
            this.zones = newZones;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_danger_zone_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DangerZone zone = zones.get(position);
            holder.bind(zone);
        }

        @Override
        public int getItemCount() {
            return zones.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvZoneName, tvDistrict, tvRiskLevel, tvRadius, tvCoordinates;
            private CardView cardView;
            private ImageView ivExpand;
            private LinearLayout layoutDetails;
            private boolean isExpanded = false;

            ViewHolder(View itemView) {
                super(itemView);
                tvZoneName = itemView.findViewById(R.id.tvZoneName);
                tvDistrict = itemView.findViewById(R.id.tvDistrict);
                tvRiskLevel = itemView.findViewById(R.id.tvRiskLevel);
                tvRadius = itemView.findViewById(R.id.tvRadius);
                tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
                cardView = itemView.findViewById(R.id.cardView);
                ivExpand = itemView.findViewById(R.id.ivExpand);
                layoutDetails = itemView.findViewById(R.id.layoutDetails);

                itemView.setOnClickListener(v -> toggleExpand());
                ivExpand.setOnClickListener(v -> toggleExpand());
            }

            void bind(DangerZone zone) {
                tvZoneName.setText(zone.getZoneName());
                tvDistrict.setText(zone.getDistrict());
                tvRadius.setText("Radius: " + zone.getRadius() + " meters");
                tvCoordinates.setText(String.format("📍 %.4f, %.4f", zone.getLatitude(), zone.getLongitude()));

                // Set risk level color
                int color;
                String riskText;
                switch (zone.getRiskLevel()) {
                    case "CRITICAL":
                        color = getResources().getColor(android.R.color.holo_red_dark);
                        riskText = "🔴 CRITICAL";
                        break;
                    case "HIGH":
                        color = getResources().getColor(android.R.color.holo_orange_dark);
                        riskText = "🟠 HIGH RISK";
                        break;
                    case "MEDIUM":
                        color = getResources().getColor(android.R.color.holo_orange_light);
                        riskText = "🟡 MEDIUM RISK";
                        break;
                    default:
                        color = getResources().getColor(android.R.color.holo_green_light);
                        riskText = "🟢 LOW RISK";
                }
                tvRiskLevel.setText(riskText);
                tvRiskLevel.setTextColor(color);

                // Reset expanded state
                layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                ivExpand.setImageResource(isExpanded ? R.drawable.ic_collapse : R.drawable.ic_expand);
            }

            void toggleExpand() {
                isExpanded = !isExpanded;
                layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                ivExpand.setImageResource(isExpanded ? R.drawable.ic_collapse : R.drawable.ic_expand);
            }
        }
    }
}