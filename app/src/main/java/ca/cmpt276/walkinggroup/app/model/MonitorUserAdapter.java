package ca.cmpt276.walkinggroup.app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.dataobjects.User;

/*
 * MonitorUserAdapter class holds
 */

public class MonitorUserAdapter extends ArrayAdapter<User> {

    private int layoutId;

    static class ViewHolder {
        TextView user_name;
        TextView user_email;

        ViewHolder(View v) {
            user_name = v.findViewById(R.id.user_name);
            user_email = v.findViewById(R.id.user_email);
        }
    }

    public MonitorUserAdapter(Context context, int layoutId, List<User> userList) {
        super(context, layoutId, userList);
        this.layoutId = layoutId;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        // Using View Holder pattern.
        // Inspired by: https://developer.android.com/training/improving-layouts/smooth-scrolling#ViewHolder
        // Adapted from : https://stackoverflow.com/questions/41080437/how-to-make-custom-adapter-with-view-holder-pattern
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        User cache = getItem(i);
        viewHolder.user_name.setText(cache.getName());
        viewHolder.user_email.setText(cache.getEmail());

        return convertView;
    }
}
