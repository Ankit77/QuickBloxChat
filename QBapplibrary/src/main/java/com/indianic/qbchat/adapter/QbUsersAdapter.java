package com.indianic.qbchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.Toaster;
import com.indianic.qbchat.utils.qb.ResourceUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose: This class set listof registed user ui in list
 */
public class QbUsersAdapter extends BaseAdapter {

    private Context context;
    private List<QBUser> initiallyAllUsers;
    private List<QBUser> selectedUsers;
    protected LayoutInflater inflater;
    private int isAction;

    public QbUsersAdapter(Context context, List<QBUser> users, int isAction) {
        try {
            this.selectedUsers = new ArrayList<>();
            this.context = context;
            this.initiallyAllUsers = users;
            this.inflater = LayoutInflater.from(context);
            this.isAction = isAction;
        } catch (Exception e) {
        }
    }


    @Override
    public int getCount() {
        return initiallyAllUsers.size();
    }

    @Override
    public QBUser getItem(int position) {
        return initiallyAllUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final QBUser user = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.image_user);
            holder.loginTextView = (TextView) convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_user);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.colorPrimary));
        }

        //holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
        holder.userCheckBox.setVisibility(View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAction == QbConstant.requestSinglechat) {
                    if (!selectedUsers.contains(user)) {
                        if (selectedUsers.size() == 0) {
                            selectedUsers.add(user);
                            holder.userCheckBox.setChecked(selectedUsers.contains(user));

                        } else {
                            Toaster.shortToast("Maximum one participant allow to select for chat.");
                        }
                    } else {
                        selectedUsers.remove(user);
                        holder.userCheckBox.setChecked(selectedUsers.contains(user));

                    }


                } else {
                    if (!selectedUsers.contains(user)) {
                        selectedUsers.add(user);
                        holder.userCheckBox.setChecked(selectedUsers.contains(user));
                    } else {
                        selectedUsers.remove(user);
                        holder.userCheckBox.setChecked(selectedUsers.contains(user));

                    }
                }

            }
        });

        holder.userCheckBox.setVisibility(View.VISIBLE);
        holder.userCheckBox.setChecked(selectedUsers.contains(user));

        return convertView;
    }

    /**
     * Method used for geting selected user for chat
     * @return
     */
    public List<QBUser> getSelectedUsers() {
        return selectedUsers;
    }

    /**
     * Method  used for clear selected user for chat
     */
    public void clearSelected() {
        selectedUsers.clear();
        notifyDataSetChanged();

    }

    /**
     * Method used for cheked selected user is me or not
     * @param user
     * @return
     */
    protected boolean isUserMe(QBUser user) {
        QBUser currentUser = QbApp.getInstance().getSharedPrefsHelper().getQbUser();
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected boolean isAvailableForSelection(QBUser user) {
        QBUser currentUser = QbApp.getInstance().getSharedPrefsHelper().getQbUser();
        return currentUser == null || !currentUser.getId().equals(user.getId());
    }

    protected static class ViewHolder {
        ImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
    }


}
