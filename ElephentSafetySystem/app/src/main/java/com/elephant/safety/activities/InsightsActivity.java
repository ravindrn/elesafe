package com.elephant.safety.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.elephant.safety.R;
import com.elephant.safety.adapters.InsightsPagerAdapter;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.DashboardStats;
import com.elephant.safety.models.NewsItem;
import com.elephant.safety.models.VerifiedReport;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsightsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private ApiService apiService;

    // Stats TextViews
    private TextView tvTotalReports, tvApprovedReports, tvTotalUsers, tvDangerZones, tvReportsThisWeek, tvTopDistrict;

    private List<VerifiedReport> verifiedReports = new ArrayList<>();
    private List<NewsItem> newsList = new ArrayList<>();
    private List<NewsItem> accidentsList = new ArrayList<>();
    private DashboardStats stats;

    private boolean dataLoaded = false;

    private final String[] tabTitles = {"✅ VERIFIED REPORTS", "📰 NEWS", "⚠️ ACCIDENTS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Insights Dashboard");
        }

        // Initialize Stats Views
        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvApprovedReports = findViewById(R.id.tvApprovedReports);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvDangerZones = findViewById(R.id.tvDangerZones);
        tvReportsThisWeek = findViewById(R.id.tvReportsThisWeek);
        tvTopDistrict = findViewById(R.id.tvTopDistrict);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getClient(this).create(ApiService.class);

        progressBar.setVisibility(View.VISIBLE);

        // Fetch all data
        fetchVerifiedReports();
        fetchNews();
        fetchAccidents();
        fetchStats();
    }

    private void fetchVerifiedReports() {
        apiService.getVerifiedReports().enqueue(new Callback<List<VerifiedReport>>() {
            @Override
            public void onResponse(Call<List<VerifiedReport>> call, Response<List<VerifiedReport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    verifiedReports = response.body();
                }
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<List<VerifiedReport>> call, Throwable t) {
                checkAndSetupViewPager();
            }
        });
    }

    private void fetchNews() {
        apiService.getNews().enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(Call<List<NewsItem>> call, Response<List<NewsItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    newsList = response.body();
                }
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<List<NewsItem>> call, Throwable t) {
                checkAndSetupViewPager();
            }
        });
    }

    private void fetchAccidents() {
        apiService.getRecentAccidents().enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(Call<List<NewsItem>> call, Response<List<NewsItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accidentsList = response.body();
                }
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<List<NewsItem>> call, Throwable t) {
                checkAndSetupViewPager();
            }
        });
    }

    private void fetchStats() {
        apiService.getDashboardStats().enqueue(new Callback<DashboardStats>() {
            @Override
            public void onResponse(Call<DashboardStats> call, Response<DashboardStats> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    stats = response.body();
                    updateStatsUI();
                }
                dataLoaded = true;
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<DashboardStats> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                dataLoaded = true;
                checkAndSetupViewPager();
            }
        });
    }

    private void updateStatsUI() {
        if (stats != null) {
            tvTotalReports.setText(String.valueOf(stats.getTotalReports()));
            tvApprovedReports.setText(String.valueOf(stats.getApprovedReports()));
            tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
            tvDangerZones.setText(String.valueOf(stats.getDangerZones()));
            tvReportsThisWeek.setText(String.valueOf(stats.getReportsThisWeek()));
            tvTopDistrict.setText(stats.getTopDistrict() != null ? stats.getTopDistrict() : "N/A");
        }
    }

    private void checkAndSetupViewPager() {
        if (dataLoaded) {
            setupViewPager();
        }
    }

    private void setupViewPager() {
        InsightsPagerAdapter adapter = new InsightsPagerAdapter(this, stats, verifiedReports, newsList, accidentsList);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}