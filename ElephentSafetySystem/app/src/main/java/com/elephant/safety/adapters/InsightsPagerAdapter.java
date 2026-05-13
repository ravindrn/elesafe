package com.elephant.safety.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elephant.safety.fragments.AccidentsFragment;
import com.elephant.safety.fragments.NewsFragment;
import com.elephant.safety.fragments.StatsFragment;
import com.elephant.safety.fragments.VerifiedReportsFragment;
import com.elephant.safety.models.DashboardStats;
import com.elephant.safety.models.NewsItem;
import com.elephant.safety.models.VerifiedReport;

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
        this.verifiedReports = verifiedReports;
        this.newsList = newsList;
        this.accidentsList = accidentsList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return StatsFragment.newInstance(stats);
            case 1:
                return VerifiedReportsFragment.newInstance(verifiedReports);
            case 2:
                return NewsFragment.newInstance(newsList);
            case 3:
                return AccidentsFragment.newInstance(accidentsList);
            default:
                return StatsFragment.newInstance(stats);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}