package com.example.baitaplonandroid.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.model.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {
    private Context context;
    private List<Message> messageList;
    private FirebaseAuth firebaseAuth;

    public MessagesAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessagesAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout,parent,false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MessagesViewHolder holder, int position) {
        Message message = messageList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String fromuserId = message.getMessageFrom();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String dateTime = sdf.format(new Date(message.getMessageTime()));

        String [] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        if(fromuserId.equals(currentUserId)){
            holder.llSent.setVisibility(View.VISIBLE);
            holder.llReceived.setVisibility(View.GONE);
            holder.txtSentMessage.setText(message.getMessage());
            holder.txtSentMessageTime.setText(messageTime);
        }else{
            holder.llReceived.setVisibility(View.VISIBLE);
            holder.llSent.setVisibility(View.GONE);
            holder.txtReceivedMessage.setText(message.getMessage());
            holder.txtReceivedMessageTime.setText(messageTime);
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class MessagesViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout llSent,llReceived;
        private TextView txtSentMessage,txtSentMessageTime,txtReceivedMessage,txtReceivedMessageTime;
        private ConstraintLayout clMessage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            llSent = itemView.findViewById(R.id.llSent);
            llReceived = itemView.findViewById(R.id.llReceived);
            txtSentMessage = itemView.findViewById(R.id.txtSentMessage);
            txtSentMessageTime = itemView.findViewById(R.id.txtSentMessageTime);

            txtReceivedMessage = itemView.findViewById(R.id.txtReceivedMessage);
            txtReceivedMessageTime = itemView.findViewById(R.id.txtReceivedMessageTime);

            clMessage = itemView.findViewById(R.id.clMessages);
        }
    }
}
