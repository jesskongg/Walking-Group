package ca.cmpt276.walkinggroup.app.model;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.user_interface.PermissionActivity;
import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroup.proxy.WGServerProxy.PermissionStatus.APPROVED;
import static ca.cmpt276.walkinggroup.proxy.WGServerProxy.PermissionStatus.DENIED;

public class PermissionDialogFragment extends DialogFragment {
    private User user;
    private String message;
    private WGServerProxy proxy;
    private Session session;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        session = Session.getInstance();
        proxy = session.getSessionProxy();
        user = session.getSessionUser();

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.permissionfragment, null);
        String message = ((PermissionActivity) getActivity()).getMessage();

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int perm) {
                int position = ((PermissionActivity) getActivity()).getPosition();
                List<PermissionRequest> requests = session.getRequests();
                switch (perm) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(requests.get(position).getId(), APPROVED);
                        session.setRequests(requests);

                        removeRequest(position, requests);

                        ((PermissionActivity) getActivity()).handleResponse(requests);
                        Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
                        ProxyBuilder.callProxy(getActivity(), caller, returnedPermission -> approveResponse(returnedPermission));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Call<PermissionRequest> denyCaller = proxy.approveOrDenyPermissionRequest(requests.get(position).getId(), DENIED);

                        session.setRequests(requests);
                        removeRequest(position,requests);

                        ((PermissionActivity) getActivity()).handleResponse(requests);
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                        ProxyBuilder.callProxy(getActivity(), denyCaller, returnedPermission -> denyResponse(returnedPermission));
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        break;

                    default:
                        assert(false);
                }

            }

            private void removeRequest(int pos, List<PermissionRequest> request) {
                request.remove(pos);
            }
        };

        return new AlertDialog.Builder(getContext()).setTitle("permission").setView(v)
                .setMessage(message)
                .setPositiveButton("Approve", listener)
                .setNegativeButton("Deny", listener)
                .setNeutralButton("Cancel", listener)
                .create();
    }

    private void denyResponse(PermissionRequest returnedPermission) {
        dismiss();
    }

    private void approveResponse(PermissionRequest returnedPermission) {
        dismiss();
    }
}
