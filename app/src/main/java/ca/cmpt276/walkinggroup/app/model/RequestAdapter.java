package ca.cmpt276.walkinggroup.app.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class RequestAdapter extends ArrayAdapter<PermissionRequest> {
    private static final String TAG = "RequestAdapter";

    private int layoutId;

    static class ViewHolder {
        TextView pointsView;

        ViewHolder(View v) {
            pointsView = v.findViewById(R.id.list_request);
        }
    }

    public RequestAdapter(Context context, int layoutId, List<PermissionRequest> requestList) {
        super(context, layoutId, requestList);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        // Using View Holder pattern.
        // Inspired by: https://developer.android.com/training/improving-layouts/smooth-scrolling#ViewHolder
        // Adapted from : https://stackoverflow.com/questions/41080437/how-to-make-custom-adapter-with-view-holder-pattern
        RequestAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new RequestAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RequestAdapter.ViewHolder) convertView.getTag();
        }

        PermissionRequest request = getItem(i);
        Log.d(TAG, "Displaying permission request: " + request.toString());

        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();
        List<String> responses = new ArrayList<>();
        for (PermissionRequest.Authorizor auth : authorizors) {
            User responder = auth.getWhoApprovedOrDenied();
            if (responder != null) {
                responses.add(responder.getName() + " (" + auth.getStatus() + ")");
            }
        }

        String content = request.getStatus().toString()
                + "\n " + request.getMessage()
                + "\n Responded to by: " + responses.toString();

        viewHolder.pointsView.setText(content);

        return convertView;
    }
}
