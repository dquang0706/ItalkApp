package com.example.italkapp.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.R;
import com.example.italkapp.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterMemberAdd extends RecyclerView.Adapter<AdapterMemberAdd.ViewHolder> {
    @NonNull
    private Context context;
    private List<ModelUser> userList;
    private String groupId, myGroupRole;

    public AdapterMemberAdd(@NonNull Context context, List<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    public AdapterMemberAdd(@NonNull Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data
        final ModelUser model = userList.get(position);
        String name = model.getName();
        String image = model.getImage();
        final String uid = model.getUid();

        // set data
        holder.nameTv.setText(name);
        try {
            Picasso.with(context).load(image).placeholder(R.drawable.avatar_default).into(holder.avatarIv);
        } catch (Exception ex) {
            holder.avatarIv.setImageResource(R.drawable.avatar_default);
        }

        checkIfAlreadyExists(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra user đã được add vào group hay chưa
                // Nếu chưa thì hiện tuỳ chọn thêm
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // user exists
                                    String hisPreviousRole = "" + dataSnapshot.child("role").getValue();

                                    // option to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(context.getString(R.string.choose));
                                    if (myGroupRole.equals("creator")) {
                                        if (hisPreviousRole.equals("admin")) {
                                            options = new String[]{context.getString(R.string.Remove_Admin), context.getString(R.string.Remove_User)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        removeAdmin(model);
                                                    } else {
                                                        removeMember(model);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("member")) {
                                            options = new String[]{context.getString(R.string.Make_Admin), context.getString(R.string.Remove_User)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        makeAdmin(model);
                                                    } else {
                                                        removeMember(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (myGroupRole.equals("admin")) {
                                        if (hisPreviousRole.equals("creator")) {
                                            Toast.makeText(context, R.string.Creator_of_Groug, Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("admin")) {
                                            options = new String[]{context.getString(R.string.Remove_Admin),context.getString(R.string.Remove_User)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        removeAdmin(model);
                                                    } else {
                                                        removeMember(model);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("member")) {
                                            options = new String[]{context.getString(R.string.Remove_Admin),context.getString(R.string.Remove_User)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        makeAdmin(model);
                                                    } else {
                                                        removeMember(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                } else {

                                    final Dialog dialog=new Dialog(context);
                                    dialog.setContentView(R.layout.dialog_confirm);
                                    Window window = dialog.getWindow();
                                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    if (dialog != null && dialog.getWindow() != null) {
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    }
                                    TextView cancelTv=dialog.findViewById(R.id.cancelTv);
                                    TextView addTv=dialog.findViewById(R.id.updatetv);
                                    TextView confirmMessageTv=dialog.findViewById(R.id.messagCfTv);
                                    cancelTv.setText(context.getString(R.string.cancel));
                                    addTv.setText(context.getString(R.string.Add));
                                    confirmMessageTv.setText(R.string.Add_this_user_into_this_group);
                                    addTv.setTextColor(context.getResources().getColor(R.color.blackNhat2));

                                    LinearLayout cancelBtn=dialog.findViewById(R.id.cancelBtn);
                                    LinearLayout addBtn=dialog.findViewById(R.id.deleteBtn);
                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });
                                    addBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            addMember(model);
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

        });
    }

    private void removeAdmin(ModelUser model) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "member");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(model.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, R.string.The_user_is_no_longer_admin, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeMember(ModelUser model) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(model.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void makeAdmin(ModelUser model) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(model.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, R.string.The_user_is_now_admin, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMember(ModelUser model) {
        // setup user data
        String timeStamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", model.getUid());
        hashMap.put("role", "member");
        hashMap.put("timeStamp", "" + timeStamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(model.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, R.string.Added_successfully, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(ModelUser model, final ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(model.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // already exists
                            String hisRole = "" + dataSnapshot.child("role").getValue();
                            if(hisRole.equals("admin")){
                                holder.statusTv.setText(context.getString(R.string.Admin));
                            }else if(hisRole.equals("creator")){
                                holder.statusTv.setText(context.getString(R.string.Creator));
                            }else if(hisRole.equals("member")){
                                holder.statusTv.setText(context.getString(R.string.Member));
                            }

                        } else {
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarIv;
        TextView nameTv, statusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            statusTv = itemView.findViewById(R.id.statusTv);
            nameTv = itemView.findViewById(R.id.nameTv);
        }
    }
}
