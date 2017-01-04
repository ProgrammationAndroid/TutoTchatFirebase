package com.tutocode.tutotchat.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.tutocode.tutotchat.Entities.Message;
import com.tutocode.tutotchat.R;

import java.util.ArrayList;

public class TchatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<Message> messages;
    private static final int SELF_MESSAGE = 0;
    private static final int OTHER_MESSAGE = 1;
    private static final int IMAGE_MESSAGE = 2;
    private FirebaseUser user;


    public TchatAdapter() {
        this.messages = new ArrayList<>();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getUid().hashCode();
    }

    public void setUser(FirebaseUser user){
        this.user = user;
    }

    public void addMessage(Message message){
        messages.add(message);
        notifyDataSetChanged();
    }
    public void deleteMessage(Message message){
        int index = messages.indexOf(message);
        messages.remove(index);
        notifyItemRemoved(index);
    }

    public void clearMessage(){
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if(messages.size() > 0){

            if(messages.get(position).getContent() == null && messages.get(position).getImageUrl() != null){
                return IMAGE_MESSAGE;
            }else {
                if (messages.get(position).getUserId().equals(user.getUid())) {
                    return SELF_MESSAGE;
                } else {
                    return OTHER_MESSAGE;
                }
            }
        }

        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case SELF_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_self_message, parent, false);
                return new SelfMessageViewHolder(view);
            case IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_image, parent, false);
                return new ImageViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);
                return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if(holder.getItemViewType() == OTHER_MESSAGE){
            ((MessageViewHolder) holder).bind(message);
        }else if(holder.getItemViewType() == SELF_MESSAGE){
            ((SelfMessageViewHolder) holder).bind(message);
        }else{
            ((ImageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class SelfMessageViewHolder extends RecyclerView.ViewHolder{

        private TextView selfMessage;

        public SelfMessageViewHolder(View itemView) {
            super(itemView);
            selfMessage = (TextView) itemView.findViewById(R.id.selfMessage);
        }

        void bind(Message message){
            selfMessage.setText(message.getContent());
        }
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView username, content;

        public MessageViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            content = (TextView) itemView.findViewById(R.id.message);
        }
        void bind(Message message){
            username.setText(message.getUsername());
            content.setText(message.getContent());
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder{

        private ImageView image;
        private TextView imageUsername;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            imageUsername = (TextView) itemView.findViewById(R.id.imageUsername);
        }

        void bind(Message message){
            imageUsername.setText(message.getUsername());
            Glide.with(image.getContext()).load(message.getImageUrl()).override(500,500).fitCenter().placeholder(R.drawable.placeholder).into(image);
        }
    }

}
