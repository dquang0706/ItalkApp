package com.example.italkapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.adapter.AdapterMemberAdd;
import com.example.italkapp.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListMemberActivity extends AppCompatActivity {
    private String myGroupRole;
    private String groupId;
    private FirebaseAuth firebaseAuth;
    // List user to load to recyclerview
    private List<ModelUser> userList;
    private AdapterMemberAdd adapterMemberAdd;
    private RecyclerView recyclerViewListMember;
    private SearchView searchView;
    private ImageView backIv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_member);
        init();
        firebaseAuth = FirebaseAuth.getInstance();
        // get groupId
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        try {
            loadGroupRole();
            loadMemberList();

            //search user
            customSeachView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!TextUtils.isEmpty(query.trim())) {
                        searchUser(query);
                    } else {
                        loadMemberList();
                    }
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        searchUser(newText);
                    } else {
                        loadMemberList();
                    }
                    return false;
                }
            });
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }

        // back to info group activity
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void searchUser(final String s) {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                ModelUser user = ds.getValue(ModelUser.class);
                                if(user.getName().toLowerCase().contains(s.toLowerCase())){
                                    userList.add(user);
                                    break;
                                }
                            }
                            adapterMemberAdd = new AdapterMemberAdd(ListMemberActivity.this,userList,groupId,myGroupRole);
                            recyclerViewListMember.setAdapter(adapterMemberAdd);
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

    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            myGroupRole = ""+dataSnapshot.child("role").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMemberList() {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             for (DataSnapshot ds:dataSnapshot.getChildren()){
                                 ModelUser user = ds.getValue(ModelUser.class);

                                 userList.add(user);
                             }
                             adapterMemberAdd = new AdapterMemberAdd(ListMemberActivity.this,userList,groupId,myGroupRole);
                             recyclerViewListMember.setAdapter(adapterMemberAdd);
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
    private void init() {
        recyclerViewListMember = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        backIv = findViewById(R.id.backIv);
    }
}