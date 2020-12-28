package com.example.italkapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.example.italkapp.R;
import com.example.italkapp.adapter.AdapterGroupChatList;
import com.example.italkapp.model.ModelGroupChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupFragment extends Fragment {

    private RecyclerView recyclerViewGroup;
    private AdapterGroupChatList adapterGroupChatList;
    private FirebaseAuth firebaseAuth;
    private List<ModelGroupChatList> modelGroupChatLists;
    View view;
    private SearchView searchView;
    DatabaseReference groupRef;
    public ChatGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_group, container, false);
        recyclerViewGroup = view.findViewById(R.id.recyclerViewChatGroup);
        firebaseAuth = FirebaseAuth.getInstance();
        groupRef = FirebaseDatabase.getInstance().getReference("Groups");
        modelGroupChatLists = new ArrayList<>();
        try {
            loadGroupChatList();
        }catch (Exception e){
            Log.d("Error",e.getMessage());
        }
        customSeachView();
        // Search user
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchGroupChatList(query);
                } else {
                    loadGroupChatList();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchGroupChatList(newText);
                } else {
                    loadGroupChatList();
                }
                return false;
            }
        });
        return view;
    }

    private void loadGroupChatList() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupChatLists.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList modelGroupChatList = ds.getValue(ModelGroupChatList.class);
                        modelGroupChatLists.add(modelGroupChatList);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(),modelGroupChatLists);
                recyclerViewGroup.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchGroupChatList(final String query) {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupChatLists.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if (ds.child("groupName").toString().toLowerCase().contains(query.toLowerCase())){
                            //search by group name
                            ModelGroupChatList modelGroupChatList = ds.getValue(ModelGroupChatList.class);
                            modelGroupChatLists.add(modelGroupChatList);
                        }
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(),modelGroupChatLists);
                recyclerViewGroup.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void customSeachView() {
        searchView = view.findViewById(R.id.searchView);
        //noMessageLayout = view.findViewById(R.id.noMessageLayout);
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #ACACAC>" + getContext().getString(R.string.Search_group) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
    }
}