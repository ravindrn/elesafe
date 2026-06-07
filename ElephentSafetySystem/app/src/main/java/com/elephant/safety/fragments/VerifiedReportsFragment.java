package com.elephant.safety.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VerifiedReportsFragment extends Fragment {

    private static final String ARG_REPORTS = "reports";
    private List<VerifiedReport> reports = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ReportsAdapter adapter;
    private ExecutorService executorService;
    private Handler mainHandler;

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
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
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

                // Show "Loading location..." while fetching address
                tvLocation.setText("📍 Loading location...");

                // Fetch location name in background
                fetchLocationName(report.getLatitude(), report.getLongitude(), tvLocation);

                tvNote.setText(report.getNote());
                tvElephantCount.setText("🐘 " + report.getElephantCount() + " elephant(s)");
                tvDate.setText("📅 " + formatDate(report.getCreatedAt()));
            }

            private void fetchLocationName(double latitude, double longitude, TextView textView) {
                executorService.execute(() -> {
                    String locationName = getSriLankanLocationName(latitude, longitude);
                    mainHandler.post(() -> {
                        if (locationName != null && !locationName.isEmpty()) {
                            textView.setText("📍 " + locationName);
                        } else {
                            textView.setText(String.format("📍 %.4f, %.4f", latitude, longitude));
                        }
                    });
                });
            }

            /**
             * Get Sri Lankan location name based on coordinates
             * This method maps coordinates to recognizable Sri Lankan locations
             */
            private String getSriLankanLocationName(double latitude, double longitude) {
                // Sri Lanka major locations mapping based on danger zones

                // Polonnaruwa District - Minneriya, Kaudulla, Somawathiya
                if (latitude >= 7.9 && latitude <= 8.2 && longitude >= 80.8 && longitude <= 81.1) {
                    if (latitude >= 8.0 && latitude <= 8.1 && longitude >= 80.8 && longitude <= 80.95) {
                        return "Minneriya National Park Area";
                    } else if (latitude >= 8.1 && latitude <= 8.2 && longitude >= 80.85 && longitude <= 81.0) {
                        return "Kaudulla National Park Area";
                    } else if (latitude >= 8.05 && latitude <= 8.15 && longitude >= 81.0 && longitude <= 81.15) {
                        return "Somawathiya National Park Area";
                    } else {
                        return "Polonnaruwa District Area";
                    }
                }

                // Anuradhapura District - Wilpattu, Mihintale
                else if (latitude >= 8.2 && latitude <= 8.5 && longitude >= 80.2 && longitude <= 80.6) {
                    if (latitude >= 8.3 && latitude <= 8.5 && longitude >= 80.2 && longitude <= 80.4) {
                        return "Wilpattu National Park Area";
                    } else if (latitude >= 8.3 && latitude <= 8.4 && longitude >= 80.4 && longitude <= 80.5) {
                        return "Mihintale Area";
                    } else {
                        return "Anuradhapura District Area";
                    }
                }

                // Matale District - Dambulla, Sigiriya
                else if (latitude >= 7.7 && latitude <= 8.0 && longitude >= 80.5 && longitude <= 80.8) {
                    if (latitude >= 7.8 && latitude <= 7.9 && longitude >= 80.6 && longitude <= 80.7) {
                        return "Dambulla - Habarana Road";
                    } else if (latitude >= 7.9 && latitude <= 8.0 && longitude >= 80.7 && longitude <= 80.8) {
                        return "Sigiriya Area";
                    } else {
                        return "Matale District Area";
                    }
                }

                // Kurunegala District
                else if (latitude >= 7.5 && latitude <= 7.8 && longitude >= 80.2 && longitude <= 80.5) {
                    return "Kurunegala District Area";
                }

                // Ampara District - Gal Oya, Lahugala
                else if (latitude >= 6.8 && latitude <= 7.5 && longitude >= 81.5 && longitude <= 81.9) {
                    if (latitude >= 7.2 && latitude <= 7.4 && longitude >= 81.5 && longitude <= 81.7) {
                        return "Gal Oya National Park Area";
                    } else if (latitude >= 6.8 && latitude <= 7.0 && longitude >= 81.7 && longitude <= 81.9) {
                        return "Lahugala - Arugam Bay Area";
                    } else {
                        return "Ampara District Area";
                    }
                }

                // Monaragala District - Lunugamvehera
                else if (latitude >= 6.3 && latitude <= 6.7 && longitude >= 81.1 && longitude <= 81.4) {
                    if (latitude >= 6.3 && latitude <= 6.5 && longitude >= 81.2 && longitude <= 81.4) {
                        return "Lunugamvehera National Park Area";
                    } else {
                        return "Monaragala District Area";
                    }
                }

                // Badulla District
                else if (latitude >= 6.6 && latitude <= 7.2 && longitude >= 80.9 && longitude <= 81.2) {
                    return "Badulla District Area";
                }

                // Hambantota District - Yala, Bundala
                else if (latitude >= 6.0 && latitude <= 6.5 && longitude >= 80.8 && longitude <= 81.3) {
                    if (latitude >= 6.3 && latitude <= 6.5 && longitude >= 81.4 && longitude <= 81.6) {
                        return "Yala National Park Area";
                    } else if (latitude >= 6.2 && latitude <= 6.3 && longitude >= 81.2 && longitude <= 81.4) {
                        return "Bundala National Park Area";
                    } else {
                        return "Hambantota District Area";
                    }
                }

                // Ratnapura District - Udawalawe
                else if (latitude >= 6.3 && latitude <= 6.6 && longitude >= 80.7 && longitude <= 80.95) {
                    if (latitude >= 6.4 && latitude <= 6.5 && longitude >= 80.8 && longitude <= 80.9) {
                        return "Udawalawe National Park Area";
                    } else {
                        return "Ratnapura District Area";
                    }
                }

                // Kegalle District
                else if (latitude >= 6.8 && latitude <= 7.4 && longitude >= 80.2 && longitude <= 80.5) {
                    return "Kegalle District Area";
                }

                // Puttalam District
                else if (latitude >= 7.9 && latitude <= 8.3 && longitude >= 79.7 && longitude <= 80.0) {
                    return "Puttalam District Area";
                }

                // Batticaloa District
                else if (latitude >= 7.4 && latitude <= 8.0 && longitude >= 81.4 && longitude <= 81.8) {
                    return "Batticaloa District Area";
                }

                // Colombo/Gampaha/Kalutara District (Western Province)
                else if (latitude >= 6.5 && latitude <= 7.2 && longitude >= 79.8 && longitude <= 80.1) {
                    return "Western Province Area";
                }

                // Galle/Matara District (Southern Province)
                else if (latitude >= 5.9 && latitude <= 6.3 && longitude >= 80.2 && longitude <= 80.6) {
                    return "Southern Province Area";
                }

                // Default: Use Geocoder as fallback
                else {
                    return getLocationNameFromCoordinates(latitude, longitude);
                }
            }

            private String getLocationNameFromCoordinates(double latitude, double longitude) {
                if (getContext() == null) return null;

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        String city = address.getLocality();
                        String subAdminArea = address.getSubAdminArea();
                        String adminArea = address.getAdminArea();

                        if (city != null && !city.isEmpty()) {
                            return city;
                        } else if (subAdminArea != null && !subAdminArea.isEmpty()) {
                            return subAdminArea;
                        } else if (adminArea != null && !adminArea.isEmpty()) {
                            return adminArea;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private String formatDate(String dateStr) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    Date date = inputFormat.parse(dateStr);
                    return outputFormat.format(date);
                } catch (Exception e) {
                    return dateStr != null ? dateStr : "Unknown date";
                }
            }
        }
    }
}