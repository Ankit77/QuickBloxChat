package com.indianic.qbchat.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.fragment.QbChatFragment;
import com.indianic.qbchat.fragment.QbMessagesFragment;
import com.indianic.qbchat.utils.qb.QbDialogUtils;
import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;

/**
 * Purpose: This class used for viewing the old message
 */
public class QbOldMessagesAdapter extends BaseAdapter {

    QbMessagesFragment fragment;
    ArrayList<QBDialog> qbDialogArrayList;
    private LayoutInflater inflater;


    public QbOldMessagesAdapter(QbMessagesFragment fragment, ArrayList<QBDialog> qbDialogArrayList) {
        this.fragment = fragment;
        this.qbDialogArrayList = qbDialogArrayList;
        this.inflater = LayoutInflater.from(fragment.getActivity());
    }

    @Override
    public int getCount() {
        return qbDialogArrayList.size();
    }

    @Override
    public QBDialog getItem(int position) {
        return qbDialogArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(QBDialog item) {
        qbDialogArrayList.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.qblist_item_old_chat, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.tv_nameFirstLatter = (TextView) convertView.findViewById(R.id.tv_dialog_firs_latter);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBDialog dialog = getItem(position);
        String nameFirstLatter = QbDialogUtils.getDialogName(dialog);
        nameFirstLatter = nameFirstLatter.substring(0, 1).toUpperCase() + nameFirstLatter.substring(1);
        holder.nameTextView.setText(nameFirstLatter);
        holder.lastMessageTextView.setText(dialog.getLastMessage());

        if (nameFirstLatter != null && !nameFirstLatter.isEmpty() && nameFirstLatter.length() > 0) {
            holder.tv_nameFirstLatter.setText(String.valueOf(nameFirstLatter.charAt(0)).toUpperCase());
        }

        int unreadMessagesCount = dialog.getUnreadMessageCount();
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
        }
        holder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fragment.deleteAlert(dialog);
                return false;
            }
        });
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(QbChatFragment.EXTRA_DIALOG, dialog);
                QbChatFragment chatFragment = new QbChatFragment();
                chatFragment.setArguments(bundle);
                ((QbActivity) fragment.getActivity()).addFragment(chatFragment, fragment);

            }
        });


        return convertView;
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        TextView tv_nameFirstLatter;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
    }
}
