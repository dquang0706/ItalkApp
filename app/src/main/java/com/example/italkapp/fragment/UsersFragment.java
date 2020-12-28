package com.example.italkapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.italkapp.ListBlockActivity;
import com.example.italkapp.MainActivity;
import com.example.italkapp.R;
import com.example.italkapp.adapter.AdapterBlock;
import com.example.italkapp.adapter.AdapterUser;
import com.example.italkapp.model.ModelUser;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class UsersFragment extends Fragment {

View view;
SearchView searchView;
    ShimmerFrameLayout placehoderLayout;

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    ArrayList<ModelUser> userList;
    private FirebaseAuth firebaseAuth;
    ImageView backIv;
    String hisUid;
    DatabaseReference reference;

    public UsersFragment() {
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
        view=inflater.inflate(R.layout.fragment_users, container, false);
        backIv = view.findViewById(R.id.backIv);
        searchView=view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerViewUser);
        placehoderLayout=view.findViewById(R.id.placehoderLayout);
        reference = FirebaseDatabase.getInstance().getReference("Users");

        customSearchView();

        firebaseAuth = FirebaseAuth.getInstance();
        userList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            getAllUser();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!TextUtils.isEmpty(query.trim())) {
                        searchUsers(query);
                    } else {
                        getAllUser();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        searchUsers(newText);
                    } else {
                        getAllUser();
                    }
                    return false;
                }
            });
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().onBackPressed();
                    getActivity().finish();
                }
            });

            if(userList.size()==0){
                placehoderLayout.setVisibility(View.GONE);
            }
        }catch (Exception e){
            Log.d("e", e.getMessage());
        }

        return view;
    }


    private void getAllUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    hisUid = "" + ds.getRef().getKey();

                    getListUser(hisUid);
                    userList.clear();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private void getListUser(final String hisUid) {

        reference.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser user = ds.getValue(ModelUser.class);
                            userList.add(user);
                        }

                        adapterUser = new AdapterUser(getContext(),R.layout.row_user, userList);
                        recyclerView.setAdapter(adapterUser);

                        adapterUser.notifyDataSetChanged();
                        placehoderLayout.stopShimmerAnimation();
                        placehoderLayout.setVisibility(View.GONE);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void searchUsers(final String s) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    hisUid = "" + ds.getRef().getKey();
                    userList.clear();
                    getListSearchUser(s,hisUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void getListSearchUser(final  String s,String hisUid) {
        reference.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser user = ds.getValue(ModelUser.class);
                            if (user.getName().toLowerCase().contains(s.toLowerCase())) {
                                userList.add(user);
                            }
                        }

                        adapterUser = new AdapterUser(getContext(),R.layout.row_user, userList);
                        recyclerView.setAdapter(adapterUser);
                        adapterUser.notifyDataSetChanged();
                        placehoderLayout.stopShimmerAnimation();
                        placehoderLayout.setVisibility(View.GONE);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void customSearchView() {
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #ACACAC>" + getString(R.string.Search) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
        searchView.clearFocus();
    }

    @Override
    public void onResume() {
        placehoderLayout.startShimmerAnimation();
        super.onResume();
    }

    @Override
    public void onPause() {
        placehoderLayout.stopShimmerAnimation();
        super.onPause();
    }
}
