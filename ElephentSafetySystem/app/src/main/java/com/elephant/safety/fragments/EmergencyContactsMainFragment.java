package com.elephant.safety.fragments;

import android.content.Intent;
import android.net.Uri;
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
import com.elephant.safety.models.EmergencyContact;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsMainFragment extends Fragment {

    private static final String ARG_POLICE = "police";
    private static final String ARG_AMBULANCE = "ambulance";
    private static final String ARG_WILDLIFE = "wildlife";
    private static final String ARG_HOSPITAL = "hospital";
    private static final String ARG_FOREST = "forest";

    private List<EmergencyContact> policeContacts = new ArrayList<>();
    private List<EmergencyContact> ambulanceContacts = new ArrayList<>();
    private List<EmergencyContact> wildlifeContacts = new ArrayList<>();
    private List<EmergencyContact> hospitalContacts = new ArrayList<>();
    private List<EmergencyContact> forestContacts = new ArrayList<>();

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ChipGroup chipGroup;
    private ContactsAdapter adapter;
    private List<EmergencyContact> allContacts = new ArrayList<>();

    public static EmergencyContactsMainFragment newInstance(List<EmergencyContact> police,
                                                            List<EmergencyContact> ambulance,
                                                            List<EmergencyContact> wildlife,
                                                            List<EmergencyContact> hospital,
                                                            List<EmergencyContact> forest) {
        EmergencyContactsMainFragment fragment = new EmergencyContactsMainFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POLICE, new ArrayList<>(police));
        args.putSerializable(ARG_AMBULANCE, new ArrayList<>(ambulance));
        args.putSerializable(ARG_WILDLIFE, new ArrayList<>(wildlife));
        args.putSerializable(ARG_HOSPITAL, new ArrayList<>(hospital));
        args.putSerializable(ARG_FOREST, new ArrayList<>(forest));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            policeContacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_POLICE);
            ambulanceContacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_AMBULANCE);
            wildlifeContacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_WILDLIFE);
            hospitalContacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_HOSPITAL);
            forestContacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_FOREST);

            if (policeContacts == null) policeContacts = new ArrayList<>();
            if (ambulanceContacts == null) ambulanceContacts = new ArrayList<>();
            if (wildlifeContacts == null) wildlifeContacts = new ArrayList<>();
            if (hospitalContacts == null) hospitalContacts = new ArrayList<>();
            if (forestContacts == null) forestContacts = new ArrayList<>();
        }

        // Combine all contacts
        allContacts.clear();
        allContacts.addAll(policeContacts);
        allContacts.addAll(ambulanceContacts);
        allContacts.addAll(wildlifeContacts);
        allContacts.addAll(hospitalContacts);
        allContacts.addAll(forestContacts);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_contacts_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        chipGroup = view.findViewById(R.id.chipGroup);

        setupChips();
        setupRecyclerView();

        return view;
    }

    private void setupChips() {
        // Add chips for filtering
        String[] categories = {"ALL", "🚓 POLICE", "🚑 AMBULANCE", "🐘 WILDLIFE", "🏥 HOSPITAL", "🌳 FOREST"};

        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setClickable(true);
            chip.setCheckable(true);

            if (category.equals("ALL")) {
                chip.setChecked(true);
            }

            final String selectedCategory = category;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterByCategory(selectedCategory);
                }
            });

            chipGroup.addView(chip);
        }
    }

    private void filterByCategory(String category) {
        List<EmergencyContact> filtered = new ArrayList<>();

        if (category.equals("ALL")) {
            filtered.addAll(allContacts);
        } else if (category.contains("POLICE")) {
            filtered.addAll(policeContacts);
        } else if (category.contains("AMBULANCE")) {
            filtered.addAll(ambulanceContacts);
        } else if (category.contains("WILDLIFE")) {
            filtered.addAll(wildlifeContacts);
        } else if (category.contains("HOSPITAL")) {
            filtered.addAll(hospitalContacts);
        } else if (category.contains("FOREST")) {
            filtered.addAll(forestContacts);
        }

        adapter.updateData(filtered);

        if (filtered.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);

        if (allContacts.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No emergency contacts available");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateData(allContacts);
        }
    }

    class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

        private List<EmergencyContact> contacts = new ArrayList<>();

        public void updateData(List<EmergencyContact> newContacts) {
            this.contacts = newContacts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emergency_contact, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            EmergencyContact contact = contacts.get(position);
            holder.bind(contact);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvPhoneNumber, tvDescription, tvCategory;
            private MaterialCardView cardView;

            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                cardView = itemView.findViewById(R.id.cardView);
            }

            void bind(EmergencyContact contact) {
                tvName.setText(contact.getName());
                tvPhoneNumber.setText(contact.getPhoneNumber());

                if (contact.getDescription() != null && !contact.getDescription().isEmpty()) {
                    tvDescription.setText(contact.getDescription());
                    tvDescription.setVisibility(View.VISIBLE);
                } else {
                    tvDescription.setVisibility(View.GONE);
                }

                // Set category badge
                String category = contact.getCategory();
                if (category != null) {
                    if (category.equals("POLICE")) {
                        tvCategory.setText("🚓 POLICE");
                        tvCategory.setBackgroundColor(getResources().getColor(R.color.driving_color));
                    } else if (category.equals("AMBULANCE")) {
                        tvCategory.setText("🚑 AMBULANCE");
                        tvCategory.setBackgroundColor(getResources().getColor(R.color.emergency_color));
                    } else if (category.equals("WILDLIFE")) {
                        tvCategory.setText("🐘 WILDLIFE");
                        tvCategory.setBackgroundColor(getResources().getColor(R.color.encounter_color));
                    } else if (category.equals("HOSPITAL")) {
                        tvCategory.setText("🏥 HOSPITAL");
                        tvCategory.setBackgroundColor(getResources().getColor(R.color.info));
                    } else if (category.equals("FOREST")) {
                        tvCategory.setText("🌳 FOREST");
                        tvCategory.setBackgroundColor(getResources().getColor(R.color.general_color));
                    }
                    tvCategory.setVisibility(View.VISIBLE);
                } else {
                    tvCategory.setVisibility(View.GONE);
                }

                // Set click listener to call the number
                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
                    startActivity(intent);
                });
            }
        }
    }
}