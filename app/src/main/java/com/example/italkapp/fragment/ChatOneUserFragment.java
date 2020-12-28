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
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.R;
import com.example.italkapp.adapter.AdapterOneChatlist;
import com.example.italkapp.adapter.AdapterUser;
import com.example.italkapp.model.ModelChat;
import com.example.italkapp.model.ModelChatlist;
import com.example.italkapp.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatOneUserFragment extends Fragment {
    RecyclerView recyclerView_ChatList;
    View view;
    List<ModelChatlist> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    DatabaseReference refUser;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    LinearLayout noMessageLayout,listAvatarUser;

    RecyclerView recyclerView_User;
   public static AdapterUser adapterUser;
    ArrayList<ModelUser> userListAvatar;
    SearchView searchView;
    TextView onlineTv, onlineNumberTv;
    LinearLayout onlineLayout;
   public static AdapterOneChatlist adapterChatlist;

    String hisUid;

    public ChatOneUserFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_one_user, container, false);

        recyclerView_ChatList = view.findViewById(R.id.recyclerView);
        recyclerView_User = view.findViewById(R.id.recyclerView_User);
        onlineNumberTv = view.findViewById(R.id.onlineNumberTv);
        onlineTv = view.findViewById(R.id.onlineTv);
        onlineLayout = view.findViewById(R.id.onlineLayout);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatlistList = new ArrayList<>();
        userList = new ArrayList<>();
        noMessageLayout = view.findViewById(R.id.noMessageLayout);
        listAvatarUser = view.findViewById(R.id.listAvatarUser);

        // Tạo list user nằm ngang
        userListAvatar = new ArrayList<>();

        refUser = FirebaseDatabase.getInstance().getReference("Users");
        customSeachView();
        try {
            getAllUserHolizontal();

            reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatlistList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                        chatlistList.add(chatlist);
                    }
                    if (chatlistList.size() == 0) {
                        noMessageLayout.setVisibility(View.VISIBLE);
                    } else {
                        noMessageLayout.setVisibility(View.GONE);
                    }
                    loadChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            e.getMessage();
        }


        // Search user
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchChat(query);
                } else {
                    loadChats();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchChat(newText);
                } else {
                    loadChats();
                }
                return false;
            }
        });
        return view;
    }

    private void loadChats() {
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);
                    //  duyệt từng id user có trong bảng user với list id có trong bảng chat list
//                    CVNR755JIpRLnM94zE6EUm0ykf12------ - dzAZNw7EBJchnky8eJnrFepjBU73
//                    CqUIkjNjcHgtjVQ33PGqnc3TYCF3------ - dzAZNw7EBJchnky8eJnrFepjBU73
//                    NQ3fClrTM0QeGwS3tmtF9SnEI882------ - dzAZNw7EBJchnky8eJnrFepjBU73
//                    dzAZNw7EBJchnky8eJnrFepjBU73------ - dzAZNw7EBJchnky8eJnrFepjBU73
//                    jta45sK9NDOAwGPSmrC6a2ztmQX2------ - dzAZNw7EBJchnky8eJnrFepjBU73
//                    rcZRSRrO0Ah7bgfXQuvm5apzMsb2------ - dzAZNw7EBJchnky8eJnrFepjBU73
                    for (ModelChatlist chatlist : chatlistList) {
                        Log.d("ggggggggggggg", user.getUid() + "-------" + chatlist.getId());
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            //  dzAZNw7EBJchnky8eJnrFepjBU73------ - dzAZNw7EBJchnky8eJnrFepjBU73
                            userList.add(user);
                            Log.d("ggggggggggggg", " userList.size() " + userList.size() + "=1");
                            break;
                        }
                    }
                    adapterChatlist = new AdapterOneChatlist(getContext(), userList); //add user id  dzAZNw7EBJchnky8eJnrFepjBU73
                    recyclerView_ChatList.setAdapter(adapterChatlist);

                    //set last message
                    for (int i = 0; i < userList.size(); i++) {
                        Log.d("ggggggggggggg", "uid : " + userList.get(i).getUid());
                        lastMessage(userList.get(i).getUid()); // id  dzAZNw7EBJchnky8eJnrFepjBU73
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) { // id  dzAZNw7EBJchnky8eJnrFepjBU73
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    Log.d("ggggggggggggg", "Duyệt  :  " + chat.getMessage());

                    if (sender == null || receiver == null) {
                        continue;
                    }
                    int i = 0;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(firebaseUser.getUid())) {
                        i++;
                        Log.d("ggggggggggggg", "chat.getReceiver()_chat.getSender() :  i" + i + " " + chat.getReceiver() + "-" + chat.getSender());
                        Log.d("ggggggggggggg", "chat.getType() + message: i " + i + " " + chat.getType() + ":" + chat.getMessage());

//                    chat.getReceiver()=dzAZNw7EBJchnky8eJnrFepjBU73+++++  firebaseUser.getUid() =CVNR755JIpRLnM94zE6EUm0ykf12
                        // sau khi chạy hết vòng lặp bảng chat đến phần tử cuối cung
                        if (chat.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        }
                        if (chat.getType().equals("text")) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getAllUserHolizontal() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userListAvatar.clear();
                    hisUid = "" + ds.getRef().getKey();
                    getListUser(hisUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getListUser(String hisUid) {

        refUser.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser user = ds.getValue(ModelUser.class);
                            if (user.getOnlineStatus().equals("online")) {
                                userListAvatar.add(user);
                                if(userListAvatar.size()==0){
                                    listAvatarUser.setVisibility(View.GONE);
                                }else {
                                    onlineLayout.setVisibility(View.VISIBLE);
                                    onlineNumberTv.setText(" ("+userListAvatar.size()+ ")");
                                }
                            }
                        }
                        adapterUser = new AdapterUser(getContext(), R.layout.row_avatar_user, userListAvatar);
                        recyclerView_User.setAdapter(adapterUser);
                        adapterUser.notifyDataSetChanged();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void searchChat(final String s) {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatlist chatlist : chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            if (user.getName().toLowerCase().contains(s.toLowerCase())) {
                                userList.add(user);
                            }
                            break;
                        }
                    }
                    adapterChatlist = new AdapterOneChatlist(getContext(), userList);
                    recyclerView_ChatList.setAdapter(adapterChatlist);

                    for (int i = 0; i < userList.size(); i++) {
                        lastMessage(userList.get(i).getUid());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void customSeachView() {
        searchView = view.findViewById(R.id.searchView);
        noMessageLayout = view.findViewById(R.id.noMessageLayout);
        searchView.setActivated(true);
        searchView.setQueryHint(Html.fromHtml("<font color = #ACACAC>" + getString(R.string.Search) + "</font>"));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setFocusable(false);
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
    }

}