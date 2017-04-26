package com.indianic.qbchat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.utils.TimeUtils;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by PG on 19/05/16.
 * Purpose: This classs used for set ui for chat message
 */
public class QbChatMessageAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private ArrayList<QBChatMessage> qbChatMessagesList;
    private Context mcontext;
    private LayoutInflater inflater;

    public QbChatMessageAdapter(Context mcontext, ArrayList<QBChatMessage> qbChatMessagesList) {
        this.qbChatMessagesList = qbChatMessagesList;
        this.mcontext = mcontext;
        this.inflater = LayoutInflater.from(mcontext);
    }

    @Override
    public View getHeaderView(int position, View ViewHeaderconvertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        if (ViewHeaderconvertView == null) {
            headerViewHolder = new HeaderViewHolder();
            ViewHeaderconvertView = inflater.inflate(R.layout.qbitem_chat_message_header, parent, false);
            headerViewHolder.dateTextView = (TextView) ViewHeaderconvertView.findViewById(R.id.header_date_textview);
            ViewHeaderconvertView.setTag(headerViewHolder);
        } else

        {
            headerViewHolder = (HeaderViewHolder) ViewHeaderconvertView.getTag();
        }
        QBChatMessage chatMessage = getItem(position);
        long messageTime = 0;
        if (chatMessage.getProperty("time") != null) {
            messageTime = Long.parseLong(chatMessage.getProperty("time") + "") - QbApp.getInstance().getTimeDifference();
        } else {
            messageTime = (chatMessage.getDateSent() * 1000) - QbApp.getInstance().getTimeDifference();
        }
        String headerDate = TimeUtils.getDate(messageTime);
        Log.e("QbChatMessageAdapter", "HeaderDate======>" + headerDate);
        headerViewHolder.dateTextView.setText(headerDate);
        return ViewHeaderconvertView;
    }

    @Override
    public long getHeaderId(final int position) {
        QBChatMessage chatMessage = getItem(position);
        long messageTime = 0;
        if (chatMessage.getProperty("time") != null) {
            messageTime = Long.parseLong(chatMessage.getProperty("time") + "") - QbApp.getInstance().getTimeDifference();
        } else {
            messageTime = (chatMessage.getDateSent() * 1000) - QbApp.getInstance().getTimeDifference();
        }
        return TimeUtils.getDateAsHeaderId(messageTime);
    }

    @Override
    public int getCount() {
        return qbChatMessagesList.size();
    }

    @Override
    public QBChatMessage getItem(final int position) {
        //  Log.e("Position", position + "");
        return qbChatMessagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.qblist_item_chat_message, parent, false);

            holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
            holder.messageContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat_message_container);
            holder.messageBodyContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_message_content_container);
            holder.messageTime = (TextView) convertView.findViewById(R.id.text_message_time);
            holder.messageUsername = (TextView) convertView.findViewById(R.id.text_username);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBChatMessage chatMessage = getItem(position);
        setIncomingOrOutgoingMessageAttributes(holder, chatMessage);
        setMessageTime(holder, chatMessage);
        setMessageBody(holder, chatMessage);
        return convertView;

    }

    /**
     * Mehod use for set message sent or received time
     * @param holder
     * @param chatMessage
     */
    private void setMessageTime(ViewHolder holder, QBChatMessage chatMessage) {
        long messageTime = 0;
        if (chatMessage.getProperty("time") != null) {
            messageTime = Long.parseLong(chatMessage.getProperty("time") + "") - QbApp.getInstance().getTimeDifference();
        } else {
            messageTime = (chatMessage.getDateSent() * 1000) - QbApp.getInstance().getTimeDifference();
        }
        String times = TimeUtils.getTime(messageTime);
        Log.e("Time", times);
        holder.messageTime.setText(times);
    }


    /**
     * Method used for set message layout view
     * @param holder
     * @param chatMessage
     */
    private void setIncomingOrOutgoingMessageAttributes(final ViewHolder holder, final QBChatMessage chatMessage) {
        boolean isIncoming = isIncoming(chatMessage);
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = gravity;
        holder.messageTime.setLayoutParams(params);
    }

    /**
     * Method use for check message is incoming or outgoing
     * @param chatMessage
     * @return
     */
    private boolean isIncoming(final QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    /**
     * Method use for set messege for view
     * @param holder
     * @param chatMessage
     */
    private void setMessageBody(final ViewHolder holder, final QBChatMessage chatMessage) {
        // Log.e("ChatAdapter", chatMessage.getBody() + "");
        holder.messageBodyTextView.setText(chatMessage.getBody());
        if (chatMessage.getProperty("sendername") != null) {
            holder.messageUsername.setText(chatMessage.getProperty("sendername") + "");
            Log.e("SenderName",chatMessage.getProperty("sendername") + "");
        } else {
            holder.messageUsername.setVisibility(View.GONE);
            Log.e("SenderName", "No Name");
        }

    }

    private static class HeaderViewHolder {
        public TextView dateTextView;
    }

    private static class ViewHolder {
        public TextView messageUsername;
        public TextView messageBodyTextView;
        public TextView messageTime;
        public LinearLayout messageContainerLayout;
        public LinearLayout messageBodyContainerLayout;

    }

    /**
     * Mehod use for add single new incoming or outgoing message
     * @param item
     */
    public void addMessage(QBChatMessage item) {
        qbChatMessagesList.add(item);
        this.notifyDataSetChanged();
    }
    /**
     * Mehod use for add single new incoming or outgoing message
     * @param items
     */
    public void addMessageList(ArrayList<QBChatMessage> items) {
        qbChatMessagesList.addAll(0, items);
        this.notifyDataSetChanged();
    }

    /**
     * Method used for geting all message list
     * @return
     */
    public ArrayList<QBChatMessage> getList() {
        return qbChatMessagesList;
    }


}
