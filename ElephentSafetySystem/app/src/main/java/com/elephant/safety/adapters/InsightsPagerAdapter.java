package com.elephant.safety.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elephant.safety.fragments.AccidentsFragment;
import com.elephant.safety.fragments.NewsFragment;
import com.elephant.safety.fragments.VerifiedReportsFragment;
import com.elephant.safety.models.DashboardStats;
import com.elephant.safety.models.NewsItem;
import com.elephant.safety.models.VerifiedReport;

import java.util.ArrayList;
import java.util.List;

public class InsightsPagerAdapter extends FragmentStateAdapter {

    private DashboardStats stats;
    private List<VerifiedReport> verifiedReports;
    private List<NewsItem> newsList;
    private List<NewsItem> accidentsList;

    public InsightsPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                DashboardStats stats,
                                List<VerifiedReport> verifiedReports,
                                List<NewsItem> newsList,
                                List<NewsItem> accidentsList) {
        super(fragmentActivity);
        this.stats = stats;
        this.verifiedReports = verifiedReports != null ? verifiedReports : new ArrayList<>();
        this.newsList = newsList != null ? newsList : new ArrayList<>();
        this.accidentsList = accidentsList != null ? accidentsList : new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return VerifiedReportsFragment.newInstance(verifiedReports);
            case 1:
                return NewsFragment.newInstance(newsList);
            case 2:
                return AccidentsFragment.newInstance(accidentsList);
            default:
                return VerifiedReportsFragment.newInstance(verifiedReports);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 3 fragments: Verified Reports, News, Accidents
    }
}