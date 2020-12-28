package com.example.italkapp.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.PostDetailActivity;
import com.example.italkapp.R;
import com.example.italkapp.model.ModelComment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{
    Context context;
    ArrayList<ModelComment> commentList;
    String myUid, postId;
    public AdapterComments(Context context, ArrayList<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // get data
        ModelComment modelComment=commentList.get(position);
        final String uid=modelComment.getUid();
        String name=modelComment.getuName();
        String image=modelComment.getuAvatar();
        final String cId=modelComment.getcId();
        String comment=modelComment.getComment();
        String timestamp=modelComment.getTimestamp();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();

        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        try {
            Picasso.with(context).load(image).placeholder(R.drawable.avatar_default).into(holder.avatarIv);
        }catch (Exception e){

        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(myUid.equals(uid)){
                    //my comment
                    final Dialog dialog=new Dialog(context);
                    dialog.setContentView(R.layout.dialog_confirm);
                    Window window = dialog.getWindow();
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (dialog != null && dialog.getWindow() != null) {
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                    LinearLayout cancelBtn=dialog.findViewById(R.id.cancelBtn);
                    LinearLayout deletelBtn=dialog.findViewById(R.id.deleteBtn);
                    TextView confirmTv=dialog.findViewById(R.id.messagCfTv);
                    confirmTv.setText(R.string.Do_you_want_delete_this_comment);
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    deletelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteComment(cId);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                }else {
                    //no my comment
                    Toast.makeText(context, R.string.You_not_delete_this_commment, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    private void deleteComment(String cid) {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                PostDetailActivity.adapterComments.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView avatarIv;
        TextView nameTv,commentTv,timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
