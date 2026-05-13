package com.elephant.safety.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elephant.safety.fragments.DangerZonesFragment;
import com.elephant.safety.fragments.EmergencyContactsMainFragment;
import com.elephant.safety.fragments.SafetyTipsPagerFragment;
import com.elephant.safety.models.EmergencyContact;
import com.elephant.safety.models.SafetyTip;

import java.util.List;

public class SafetyMainPagerAdapter extends FragmentStateAdapter {

    private List<SafetyTip> drivingTips;
    private List<SafetyTip> encounterTips;
    private List<SafetyTip> emergencyTips;
    private List<SafetyTip> generalTips;

    private List<EmergencyContact> policeContacts;
    private List<EmergencyContact> ambulanceContacts;
    private List<EmergencyContact> wildlifeContacts;
    private List<EmergencyContact> hospitalContacts;
    private List<EmergencyContact> forestContacts;

    public SafetyMainPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                  List<SafetyTip> drivingTips,
                                  List<SafetyTip> encounterTips,
                                  List<SafetyTip> emergencyTips,
                                  List<SafetyTip> generalTips,
                                  List<EmergencyContact> policeContacts,
                                  List<EmergencyContact> ambulanceContacts,
                                  List<EmergencyContact> wildlifeContacts,
                                  List<EmergencyContact> hospitalContacts,
                                  List<EmergencyContact> forestContacts) {
        super(fragmentActivity);
        this.drivingTips = drivingTips;
        this.encounterTips = encounterTips;
        this.emergencyTips = emergencyTips;
        this.generalTips = generalTips;
        this.policeContacts = policeContacts;
        this.ambulanceContacts = ambulanceContacts;
        this.wildlifeContacts = wildlifeContacts;
        this.hospitalContacts = hospitalContacts;
        this.forestContacts = forestContacts;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Safety Tips Tab - Categorized tips
                return SafetyTipsPagerFragment.newInstance(
                        drivingTips, encounterTips, emergencyTips, generalTips
                );
            case 1:
                // Danger Zones Tab
                return new DangerZonesFragment();
            case 2:
                // Emergency Contacts Tab - All emergency contacts in one place
                return EmergencyContactsMainFragment.newInstance(
                        policeContacts, ambulanceContacts, wildlifeContacts, hospitalContacts, forestContacts
                );
            default:
                return new DangerZonesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Safety Tips, Danger Zones, Emergency
    }
}