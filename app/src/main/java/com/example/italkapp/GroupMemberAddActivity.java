package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.adapter.AdapterMemberAdd;
import com.example.italkapp.model.ModelUser;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupMemberAddActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView groupNameTv,groupRoleTv;
    private ImageView backIv;

    private FirebaseAuth firebaseAuth;

    // Group id to get extra from intent
    private String groupId;
    private String myGroupRole;

    // List user to load to recyclerview
    private List<ModelUser> userList;
    private AdapterMemberAdd adapterMemberAdd;

    // Add searchview
    private SearchView searchView;

    String hisUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);
        ColorStatusbar.setColorStatusBar(GroupMemberAddActivity.this);
        init();
        customSeachView();
        firebaseAuth = FirebaseAuth.getInstance();
        userList=new ArrayList<>();
        // Get group id from intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        try {
            loadGroupInfo();
            //search user

            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!TextUtils.isEmpty(query.trim())) {
                        searchUserToAdd(query);
                    } else {
                        getAllUser();
                    }
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        searchUserToAdd(newText);
                    } else {
                        getAllUser();
                    }
                    return false;
                }
            });
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }


        // back to chat group activity
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getAllUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    hisUid = "" + ds.getRef().getKey();
                    userList.clear();
                    getListUser(hisUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
//        checkOnlineStatus("online");
    }
    private void getListUser(String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelUser user = ds.getValue(ModelUser.class);

                            if (!firebaseAuth.getCurrentUser().getUid().equals(user.getUid())){
                                userList.add(user);
                            }
                        }
                        adapterMemberAdd = new AdapterMemberAdd(GroupMemberAddActivity.this,userList,""+groupId,""+myGroupRole);
                        recyclerView.setAdapter(adapterMemberAdd);
                        recyclerView.hasFixedSize();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GroupMemberAddActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            String groupId = ""+ds.child("groupId").getValue();
                            final String groupName = ""+ds.child("groupName").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timeStamp = ""+ds.child("timeStamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();
                            ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                myGroupRole = ""+dataSnapshot.child("role").getValue();
                                                groupNameTv.setText(groupName);
                                                if(myGroupRole.equals("creator")){
                                                    groupRoleTv.setText(R.string.Creator);
                                                }else if(myGroupRole.equals("member")){
                                                    groupRoleTv.setText(R.string.Member);
                                                }else if((myGroupRole.equals("admin"))){
                                                    groupRoleTv.setText(R.string.Admin);
                                                }
                                                getAllUser();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);
        groupNameTv = findViewById(R.id.groupNameTv);
        groupRoleTv = findViewById(R.id.groupRoleTv);
        backIv = findViewById(R.id.backIv);
        searchView = findViewById(R.id.searchView);
    }
    public void customSeachView() {
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #ACACAC>" + getString(R.string.Search) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
    }
    public void searchUserToAdd(final String s){
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    if (!firebaseAuth.getCurrentUser().getUid().equals(user.getUid())){
                        if(user.getName().toLowerCase().contains(s.toLowerCase())){
                            userList.add(user);
                            break;
                        }
                    }
                }
                adapterMemberAdd = new AdapterMemberAdd(GroupMemberAddActivity.this,userList,""+groupId,""+myGroupRole);
                recyclerView.setAdapter(adapterMemberAdd);
                recyclerView.hasFixedSize();
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