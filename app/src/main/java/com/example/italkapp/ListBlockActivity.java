package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.italkapp.adapter.AdapterBlock;
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

public class ListBlockActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ModelUser> userList;
    public static AdapterBlock adapterBlock;
    DatabaseReference reference;
    ImageView backIv;
    String myUid, hisUid;
    SearchView searchView;

    RelativeLayout noBlockLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_block);
        ColorStatusbar.setColorStatusBar(ListBlockActivity.this);

        recyclerView = findViewById(R.id.recyclerViewBlock);
        recyclerView.setHasFixedSize(true);
        noBlockLayout = findViewById(R.id.noBlockLayout);
        backIv = findViewById(R.id.backIv);
        searchView = findViewById(R.id.searchView);
        customSearchView();

        myUid = FirebaseAuth.getInstance().getUid();
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        try {
            getAllUserBlock();
        }catch (Exception e){

        }

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchUsers(query);
                } else {
                    getAllUserBlock();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUsers(newText);
                } else {
                    getAllUserBlock();
                }
                return false;
            }
        });
        checkOnlineStatus("online");
    }

    private void getAllUserBlock() {
        reference.child(myUid).child("BlockedHisUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    hisUid = "" + ds.getRef().getKey();
                    userList.clear();
                    getListBlock(hisUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void getListBlock(String hisUid) {
        reference.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser user = ds.getValue(ModelUser.class);
                            userList.add(user);

                        }
                       if(userList.size()==0){
                            noBlockLayout.setVisibility(View.VISIBLE);
                        }else {
                            noBlockLayout.setVisibility(View.GONE);
                        }
                        adapterBlock = new AdapterBlock(ListBlockActivity.this, userList);
                        recyclerView.setAdapter(adapterBlock);
                        adapterBlock.notifyDataSetChanged();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ListBlockActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchUsers(final String s) {
        reference.child(myUid).child("BlockedHisUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userList.clear();
                    hisUid = "" + ds.getRef().getKey();
                    reference.orderByChild("uid").equalTo(hisUid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        ModelUser modelUser = ds.getValue(ModelUser.class);
                                        if (modelUser.getName().toLowerCase().contains(s.toLowerCase()) ) {
                                            userList.add(modelUser);
                                        }
                                    }
                                    adapterBlock = new AdapterBlock(ListBlockActivity.this, userList);
                                    recyclerView.setAdapter(adapterBlock);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void customSearchView() {
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #ACACAC>" + getString(R.string.enter_friends_need_to_find) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);

    }
    @Override
    protected void onPause() {
        String timeStamp =""+System.currentTimeMillis();
        checkOnlineStatus(timeStamp);
        super.onPause();
    }

    public void checkOnlineStatus(String status) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);


    }


}