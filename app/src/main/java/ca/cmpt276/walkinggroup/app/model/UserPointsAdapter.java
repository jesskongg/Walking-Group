package ca.cmpt276.walkinggroup.app.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class UserPointsAdapter extends ArrayAdapter<User> {
    private static final String TAG = "UserPointsAdapter";

    private int layoutId;

    static class ViewHolder {
        TextView pointsView;

        ViewHolder(View v) {
            pointsView = v.findViewById(R.id.list_points);
        }
    }

    public UserPointsAdapter(Context context, int layoutId, List<User> userList) {
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
            viewHolder = new UserPointsAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (UserPointsAdapter.ViewHolder) convertView.getTag();
        }

        User user = getItem(i);
        Log.d(TAG, "Displaying points for user: " + user.toString());

        String formattedName;
        String[] nameSegments = user.getName().split(" ");
        if (nameSegments.length == 2) {
            formattedName = nameSegments[0] + " " + nameSegments[1].substring(0, 1);
        } else {
            formattedName = nameSegments[0];
        }
        Log.d(TAG, "Formatted user name as: " + formattedName);

        String content = Integer.toString(i+1)
                + ": " + formattedName
                + " (" + user.getTotalPointsEarned() + ")";
        viewHolder.pointsView.setText(content);

        return convertView;
    }
}
