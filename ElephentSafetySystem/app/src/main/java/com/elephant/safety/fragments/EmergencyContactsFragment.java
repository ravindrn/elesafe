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

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsFragment extends Fragment {

    private static final String ARG_CONTACTS = "contacts";
    private static final String ARG_CATEGORY_TITLE = "category_title";

    private List<EmergencyContact> contacts = new ArrayList<>();
    private String categoryTitle;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ContactsAdapter adapter;

    public static EmergencyContactsFragment newInstance(List<EmergencyContact> contacts, String categoryTitle) {
        EmergencyContactsFragment fragment = new EmergencyContactsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACTS, new ArrayList<>(contacts));
        args.putString(ARG_CATEGORY_TITLE, categoryTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contacts = (List<EmergencyContact>) getArguments().getSerializable(ARG_CONTACTS);
            categoryTitle = getArguments().getString(ARG_CATEGORY_TITLE);
            if (contacts == null) contacts = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);

        if (contacts.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No emergency contacts available");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

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
            private TextView tvName, tvPhoneNumber, tvDescription;
            private MaterialCardView cardView;

            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
                tvDescription = itemView.findViewById(R.id.tvDescription);
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