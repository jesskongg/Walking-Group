package ca.cmpt276.walkinggroup.app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.dataobjects.Group;

public class GroupAdapter extends ArrayAdapter<Group> {

    private int layoutId;

    static class ViewHolder {
        TextView Group;

        ViewHolder(View v) {
            Group = v.findViewById(R.id.listgroup);
        }
    }

    public GroupAdapter(Context context, int layoutId, List<Group> groupList) {
        super(context, layoutId, groupList);
        this.layoutId = layoutId;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        // Using View Holder pattern.
        // Inspired by: https://developer.android.com/training/improving-layouts/smooth-scrolling#ViewHolder
        // Adapted from : https://stackoverflow.com/questions/41080437/how-to-make-custom-adapter-with-view-holder-pattern
        ca.cmpt276.walkinggroup.app.model.GroupAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new ca.cmpt276.walkinggroup.app.model.GroupAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ca.cmpt276.walkinggroup.app.model.GroupAdapter.ViewHolder) convertView.getTag();
        }

        Group group = getItem(i);
        viewHolder.Group.setText(group.getGroupDescription());

        return convertView;
    }
}
