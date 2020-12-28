package com.example.italkapp.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.italkapp.CreateGroupActivity;
import com.example.italkapp.HomeActivity;
import com.example.italkapp.R;
import com.example.italkapp.SettingActivity;
import com.example.italkapp.adapter.AdapterChatViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChatListContainerFragment extends Fragment {
    ViewPager viewPager;
    TabLayout tabLayout;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    ImageView avatarIv;
    ImageView createGroup;

    SearchView searchView;

    public ChatListContainerFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list_container, container, false);

        // Init layout list avatar user
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //recyclerView_User = view.findViewById(R.id.recyclerView_User);
        searchView = view.findViewById(R.id.searchView);
        avatarIv = view.findViewById(R.id.avatarIv);
        createGroup = view.findViewById(R.id.createGroupBtn);

        HomeActivity.getAvatar(getContext(), avatarIv);

        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });

        // Set viewpager and tablayout
        viewPager = view.findViewById(R.id.chatViewPager);
        tabLayout = view.findViewById(R.id.chatTabLayout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        AdapterChatViewPager adapter = new AdapterChatViewPager(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        setUntouchableTab();
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // intent to create a group
        intentToCreateGroup();
        return view;
    }


    private void intentToCreateGroup(){

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CreateGroupActivity.class));
            }
        });
    }
    private void setUntouchableTab(){
        tabLayout.setupWithViewPager(viewPager, true);
        tabLayout.clearOnTabSelectedListeners();
        for (View v : tabLayout.getTouchables()) {
            v.setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        checkOnlineStatus("online");
    }

    private void checkOnlineStatus(String status) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);


    }
}