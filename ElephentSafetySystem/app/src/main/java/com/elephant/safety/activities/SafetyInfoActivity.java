package com.elephant.safety.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.elephant.safety.R;
import com.elephant.safety.adapters.SafetyMainPagerAdapter;
import com.elephant.safety.api.ApiClient;
import com.elephant.safety.api.ApiService;
import com.elephant.safety.models.EmergencyContact;
import com.elephant.safety.models.SafetyTip;
import com.elephant.safety.utils.CustomToast;
import com.elephant.safety.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafetyInfoActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private ApiService apiService;

    // Lists to store fetched tips by category
    private List<SafetyTip> drivingTips = new ArrayList<>();
    private List<SafetyTip> encounterTips = new ArrayList<>();
    private List<SafetyTip> emergencyTips = new ArrayList<>();
    private List<SafetyTip> generalTips = new ArrayList<>();

    // Lists to store emergency contacts by category
    private List<EmergencyContact> policeContacts = new ArrayList<>();
    private List<EmergencyContact> ambulanceContacts = new ArrayList<>();
    private List<EmergencyContact> wildlifeContacts = new ArrayList<>();
    private List<EmergencyContact> hospitalContacts = new ArrayList<>();
    private List<EmergencyContact> forestContacts = new ArrayList<>();

    // Main Tab titles - 3 tabs now
    private final String[] tabTitles = {
            "🛡️ SAFETY TIPS",
            "🗺️ DANGER ZONES",
            "📞 EMERGENCY"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Safety Information");
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);

        // Fetch both safety tips and emergency contacts
        fetchAllData();
    }

    private void fetchAllData() {
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            CustomToast.showWarning(this, "Please login to view safety information");
            loadLocalData();
            return;
        }

        // Fetch safety tips
        fetchSafetyTips();

        // Fetch emergency contacts
        fetchEmergencyContacts();
    }

    private void fetchSafetyTips() {
        apiService.getCategorizedSafetyTips().enqueue(new Callback<ApiService.CategorizedSafetyTips>() {
            @Override
            public void onResponse(Call<ApiService.CategorizedSafetyTips> call, Response<ApiService.CategorizedSafetyTips> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategorizedSafetyTips tips = response.body();

                    drivingTips = tips.getDrivingTips() != null ? tips.getDrivingTips() : new ArrayList<>();
                    encounterTips = tips.getEncounterTips() != null ? tips.getEncounterTips() : new ArrayList<>();
                    emergencyTips = tips.getEmergencyTips() != null ? tips.getEmergencyTips() : new ArrayList<>();
                    generalTips = tips.getGeneralTips() != null ? tips.getGeneralTips() : new ArrayList<>();
                } else {
                    loadLocalSafetyTips();
                }
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<ApiService.CategorizedSafetyTips> call, Throwable t) {
                loadLocalSafetyTips();
                checkAndSetupViewPager();
            }
        });
    }

    private void fetchEmergencyContacts() {
        apiService.getCategorizedEmergencyContacts().enqueue(new Callback<ApiService.CategorizedEmergencyContacts>() {
            @Override
            public void onResponse(Call<ApiService.CategorizedEmergencyContacts> call, Response<ApiService.CategorizedEmergencyContacts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategorizedEmergencyContacts contacts = response.body();

                    policeContacts = contacts.getPolice() != null ? contacts.getPolice() : new ArrayList<>();
                    ambulanceContacts = contacts.getAmbulance() != null ? contacts.getAmbulance() : new ArrayList<>();
                    wildlifeContacts = contacts.getWildlife() != null ? contacts.getWildlife() : new ArrayList<>();
                    hospitalContacts = contacts.getHospital() != null ? contacts.getHospital() : new ArrayList<>();
                    forestContacts = contacts.getForest() != null ? contacts.getForest() : new ArrayList<>();
                } else {
                    loadLocalEmergencyContacts();
                }
                checkAndSetupViewPager();
            }

            @Override
            public void onFailure(Call<ApiService.CategorizedEmergencyContacts> call, Throwable t) {
                loadLocalEmergencyContacts();
                checkAndSetupViewPager();
            }
        });
    }

    private void checkAndSetupViewPager() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        setupViewPager();
    }

    private void loadLocalData() {
        loadLocalSafetyTips();
        loadLocalEmergencyContacts();
        setupViewPager();
    }

    private void loadLocalSafetyTips() {
        drivingTips = getLocalDrivingTips();
        encounterTips = getLocalEncounterTips();
        emergencyTips = getLocalEmergencyTips();
        generalTips = getLocalGeneralTips();
    }

    private void loadLocalEmergencyContacts() {
        policeContacts = getLocalPoliceContacts();
        ambulanceContacts = getLocalAmbulanceContacts();
        wildlifeContacts = getLocalWildlifeContacts();
        hospitalContacts = getLocalHospitalContacts();
        forestContacts = getLocalForestContacts();
    }

    private void setupViewPager() {
        // Create adapter with 3 main tabs
        SafetyMainPagerAdapter adapter = new SafetyMainPagerAdapter(this,
                drivingTips, encounterTips, emergencyTips, generalTips,
                policeContacts, ambulanceContacts, wildlifeContacts, hospitalContacts, forestContacts);
        viewPager.setAdapter(adapter);

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        // Customize tab colors
        tabLayout.setTabTextColors(
                getColor(R.color.gray_dark),
                getColor(R.color.primary)
        );
        tabLayout.setSelectedTabIndicatorColor(getColor(R.color.primary));
    }

    // ========== LOCAL SAFETY TIPS (FALLBACK) ==========

    private List<SafetyTip> getLocalDrivingTips() {
        List<SafetyTip> tips = new ArrayList<>();
        tips.add(new SafetyTip(1L, "Reduce Speed Immediately", "When approaching known elephant crossing zones, reduce your speed to below 30km/h.", "DRIVING", "ic_speed", 1));
        tips.add(new SafetyTip(2L, "Never Honk Your Horn", "Loud noises can startle elephants and cause unpredictable behavior.", "DRIVING", "ic_horn", 2));
        tips.add(new SafetyTip(3L, "Use Hazard Lights", "When stopped for elephants, turn on hazard lights to warn other drivers.", "DRIVING", "ic_hazard", 3));
        return tips;
    }

    private List<SafetyTip> getLocalEncounterTips() {
        List<SafetyTip> tips = new ArrayList<>();
        tips.add(new SafetyTip(4L, "Maintain Safe Distance", "Keep at least 50 meters distance from elephants. Never try to drive between elephant groups.", "ENCOUNTER", "ic_distance", 1));
        tips.add(new SafetyTip(5L, "Turn Off Engine", "If you see an elephant near the road, turn off your engine and headlights.", "ENCOUNTER", "ic_engine", 2));
        tips.add(new SafetyTip(6L, "Wait Patiently", "Give elephants time to cross the road naturally.", "ENCOUNTER", "ic_wait", 3));
        return tips;
    }

    private List<SafetyTip> getLocalEmergencyTips() {
        List<SafetyTip> tips = new ArrayList<>();
        tips.add(new SafetyTip(7L, "Emergency Preparedness", "Always keep emergency contacts saved in your phone.", "EMERGENCY", "ic_emergency", 1));
        tips.add(new SafetyTip(8L, "Stay Calm", "In any emergency situation, staying calm helps you make better decisions.", "EMERGENCY", "ic_calm", 2));
        return tips;
    }

    private List<SafetyTip> getLocalGeneralTips() {
        List<SafetyTip> tips = new ArrayList<>();
        tips.add(new SafetyTip(9L, "Report Elephant Sightings", "Use the app to report elephant sightings immediately.", "GENERAL", "ic_report", 1));
        tips.add(new SafetyTip(10L, "Avoid Night Travel", "Elephants are most active during dawn and dusk.", "GENERAL", "ic_night", 2));
        return tips;
    }

    // ========== LOCAL EMERGENCY CONTACTS (FALLBACK) ==========

    private List<EmergencyContact> getLocalPoliceContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        contacts.add(new EmergencyContact("Police Emergency", "119", "National police emergency hotline", "POLICE"));
        contacts.add(new EmergencyContact("Police Headquarters", "011-2444444", "Sri Lanka Police Headquarters Colombo", "POLICE"));
        return contacts;
    }

    private List<EmergencyContact> getLocalAmbulanceContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        contacts.add(new EmergencyContact("National Ambulance Service", "110", "Emergency ambulance service nationwide", "AMBULANCE"));
        contacts.add(new EmergencyContact("St. John Ambulance", "011-2369396", "St. John Ambulance Service", "AMBULANCE"));
        return contacts;
    }

    private List<EmergencyContact> getLocalWildlifeContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        contacts.add(new EmergencyContact("Wildlife Department", "198", "Department of Wildlife Conservation", "WILDLIFE"));
        contacts.add(new EmergencyContact("Wildlife Emergency", "011-2888585", "24/7 wildlife emergency hotline", "WILDLIFE"));
        return contacts;
    }

    private List<EmergencyContact> getLocalHospitalContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        contacts.add(new EmergencyContact("National Hospital Kandy", "081-2222261", "Teaching Hospital Kandy", "HOSPITAL"));
        contacts.add(new EmergencyContact("National Hospital Colombo", "011-2323261", "Colombo National Hospital", "HOSPITAL"));
        return contacts;
    }

    private List<EmergencyContact> getLocalForestContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        contacts.add(new EmergencyContact("Forest Department", "011-2866625", "Forest Department Colombo", "FOREST"));
        return contacts;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}