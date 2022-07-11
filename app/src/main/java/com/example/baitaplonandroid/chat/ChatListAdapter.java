package com.example.baitaplonandroid.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>{

    private Context context;
    private List<ChatModel> chatModelList;


    public ChatListAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
    }

    @NonNull
    @Override
    public ChatListAdapter.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout,parent,false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ChatListViewHolder holder, int position) {
        ChatModel  chatModel = chatModelList.get(position);

        holder.txtFullname.setText(chatModel.getName());

        StorageReference fileRef = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference().child("images/"+chatModel.getPhoto());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri)
                        .placeholder(R.drawable.defaultpropic)
                        .error(R.drawable.defaultpropic)
                        .into(holder.ivProfile);
            }
        });

        if(!chatModel.getUnreadCount().equals("0"))
        {
            holder.txtUnreadCount.setVisibility(View.VISIBLE);
            holder.txtUnreadCount.setText(chatModel.getUnreadCount());

        }else{
            holder.txtUnreadCount.setVisibility(View.GONE);
        }

        String lastMessage = chatModel.getLastMessage();

        lastMessage = lastMessage.length()>30?lastMessage.substring(0,30):lastMessage;

        holder.txtLastMessage.setText(lastMessage);
        String lastMessageTime = chatModel.getLastMessageTime();
        if(lastMessageTime==null)  lastMessageTime = "";

        if(!TextUtils.isEmpty(lastMessageTime))
            holder.txtLastMessageTime.setText(Util.getTimeAgo(Long.parseLong(lastMessageTime)));


        holder.llChatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("user_key",chatModel.getUserId());
                intent.putExtra("user_name",chatModel.getName());
                intent.putExtra("user_photo",chatModel.getPhoto());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout llChatList;
        private TextView txtFullname,txtLastMessage,txtLastMessageTime,txtUnreadCount;
        private ImageView ivProfile;


        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            llChatList = itemView.findViewById(R.id.llChatList);
            txtFullname = itemView.findViewById(R.id.txtFullName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
            txtLastMessageTime = itemView.findViewById(R.id.txtLastMessageTime);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }
}
