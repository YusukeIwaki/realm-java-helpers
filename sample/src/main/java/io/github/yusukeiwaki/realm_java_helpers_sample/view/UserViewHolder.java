package io.github.yusukeiwaki.realm_java_helpers_sample.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;

/**
 * use android.R.layout.simple_list_item_1 for layout.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    private final TextView username;

    public UserViewHolder(View itemView) {
        super(itemView);
        username = (TextView) itemView.findViewById(android.R.id.text1);
    }

    public final void bind(User user) {
        if (user == null) {
            username.setText("-");
        } else {
            username.setText(user.toString());
        }
    }
}
