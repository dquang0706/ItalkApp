package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.italkapp.adapter.AdapterUser;
import com.example.italkapp.model.ModelUser;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    SearchView searchView;
    RecyclerView recyclerView;

    AdapterUser adapterUser;
    ArrayList<ModelUser> userList;
    private FirebaseAuth firebaseAuth;
    RelativeLayout finishLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ColorStatusbar.setColorStatusBar(SearchActivity.this);
        searchView=findViewById(R.id.searchView);
        recyclerView=findViewById(R.id.recyclerView);
        finishLayout=findViewById(R.id.finishLayout);
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #5E5E5E>" + getString(R.string.Search) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();
        firebaseAuth = FirebaseAuth.getInstance();
        userList = new ArrayList<>();


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        try {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!TextUtils.isEmpty(query.trim())) {
                        searchUsers(query);
                    }else {
                        userList.clear();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        searchUsers(newText);
                    }else {
                        userList.clear();
                    }
                    return false;
                }
            });
            finishLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(SearchActivity.this,HomeActivity.class));
                    finish();
                }
            });
//            checkOnlineStatus("online");
        }catch (Exception e){
            e.getMessage();
        }


    }
    private void searchUsers(final String s) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    if (!modelUser.getUid().equals(firebaseAuth.getUid())) {
                        if (modelUser.getName().toLowerCase().contains(s.toLowerCase()) || modelUser.getEmail().toLowerCase().contains(s.toLowerCase())) {
                            userList.add(modelUser);
                        }
                    }
                    adapterUser = new AdapterUser(SearchActivity.this,R.layout.row_user, userList);
                    adapterUser.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUser);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    @Override
//    protected void onPause() {
//        String timeStamp =""+System.currentTimeMillis();
//        checkOnlineStatus(timeStamp);
//        super.onPause();
//    }
//
//    public void checkOnlineStatus(String status) {
//
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("onlineStatus", status);
//        //cap nhat trang thai cua user
//        dbRef.updateChildren(hashMap);
//
//
//    }
}