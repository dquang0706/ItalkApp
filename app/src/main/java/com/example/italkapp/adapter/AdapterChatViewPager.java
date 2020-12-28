package com.example.italkapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.italkapp.fragment.ChatGroupFragment;
import com.example.italkapp.fragment.ChatOneUserFragment;

public class AdapterChatViewPager extends FragmentStatePagerAdapter {
    int numberTab = 2;

    public AdapterChatViewPager(@NonNull FragmentManager fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatOneUserFragment chatOneUserFragment = new ChatOneUserFragment();

                return chatOneUserFragment;
            case 1:
                ChatGroupFragment chatGroupFragment = new ChatGroupFragment();
                return chatGroupFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return numberTab;
    }

}
