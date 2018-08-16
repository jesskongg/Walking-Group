package ca.cmpt276.walkinggroup.app.user_interface;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.PermissionDialogFragment;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroup.proxy.WGServerProxy.PermissionStatus.PENDING;

/**
 * Activity that allows the current user to approve or deny their pending permission requests.
 */
public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";

    private ArrayList<String> listPending = new ArrayList<>();
    private TextView noRequestsTextView;
    private int listPosition;
    private String message;
    private User user;
    private WGServerProxy proxy;
    private Session session;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        session = Session.getInstance();
        proxy = session.getSessionProxy();
        user = session.getSessionUser();

        progressBar = findViewById(R.id.permission_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        noRequestsTextView = findViewById(R.id.no_requests_label);
        noRequestsTextView.setVisibility(View.INVISIBLE);

        getPendingPermission();
        setupDialog();
    }

    public static Intent makeIntent(Context context) {
        Log.d(TAG, "Creating intent from context: " + context.toString());
        return new Intent(context, PermissionActivity.class);
    }

    private void getPendingPermission() {
        Call<List<PermissionRequest>> caller = proxy.getPermissionFoUserPending(user.getId(), PENDING);
        ProxyBuilder.callProxy(PermissionActivity.this, caller,
                returnedPermissions -> handleResponse(returnedPermissions));
    }

    public void handleResponse(List<PermissionRequest> returnedPermissions) {
        listPending = new ArrayList<>();
        for (PermissionRequest request : returnedPermissions){
            listPending.add(request.getMessage());
        }
        session.setRequests(returnedPermissions);
        populateList(listPending);
    }

    private void setupDialog() {
        ListView list = (ListView) findViewById(R.id.pending_permission_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                listPosition = position;
                message = listPending.get(position);
                FragmentManager fragmentManager = getFragmentManager();
                PermissionDialogFragment Dialog = new PermissionDialogFragment();
                Dialog.show(fragmentManager, TAG);
            }
        });
    }

    public void populateList(ArrayList<String> permissionList){
        ListView list = (ListView) findViewById(R.id.pending_permission_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.permissions, permissionList);
        list.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);

        if (permissionList.size() == 0) {
            noRequestsTextView.setVisibility(View.VISIBLE);
        }
    }

    public int getPosition() {
        return listPosition;
    }

    public String getMessage() {
        return message;
    }
}
