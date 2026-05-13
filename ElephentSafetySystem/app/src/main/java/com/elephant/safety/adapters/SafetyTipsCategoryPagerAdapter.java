package com.elephant.safety.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elephant.safety.fragments.EmergencyContactsFragment;
import com.elephant.safety.fragments.SafetyTipsCategoryFragment;
import com.elephant.safety.models.EmergencyContact;
import com.elephant.safety.models.SafetyTip;

import java.util.ArrayList;
import java.util.List;

public class SafetyTipsCategoryPagerAdapter extends FragmentStateAdapter {

    private List<Object> categories;
    private String[] titles;

    public SafetyTipsCategoryPagerAdapter(@NonNull Fragment fragment,
                                          List<Object> categories,
                                          String[] titles) {
        super(fragment);
        this.categories = categories;
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Object category = categories.get(position);

        if (category instanceof List) {
            List<?> list = (List<?>) category;

            // Check if this is emergency contacts (contains EmergencyContact objects)
            if (!list.isEmpty() && list.get(0) instanceof EmergencyContact) {
                return EmergencyContactsFragment.newInstance(
                        (List<EmergencyContact>) list,
                        titles[position]
                );
            }

            // Otherwise it's safety tips
            return SafetyTipsCategoryFragment.newInstance(
                    (List<SafetyTip>) list,
                    titles[position]
            );
        }

        // Fallback with empty list
        return SafetyTipsCategoryFragment.newInstance(new ArrayList<SafetyTip>(), titles[position]);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}