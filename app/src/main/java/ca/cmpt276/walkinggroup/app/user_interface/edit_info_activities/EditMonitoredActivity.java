package ca.cmpt276.walkinggroup.app.user_interface.edit_info_activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.app.model.Preferences;
import ca.cmpt276.walkinggroup.app.model.Session;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static com.google.android.gms.common.util.NumberUtils.isNumeric;

// Class EditMonitoredActivity allows users to edit the information of their monitored users.

public class EditMonitoredActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private Session session;
    private static final String TAG = "EditUserActivity";
    private User user;
    private Long UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_monitored);

        session = Session.getInstance();
        proxy = session.getSessionProxy();
        setupGetMonitoredUser();
    }

    private void setupGetMonitoredUser() {
        Intent intent = getIntent();
        UID = intent.getLongExtra("userID",0);
        Call<User> caller = proxy.getUserById(UID);
        ProxyBuilder.callProxy(EditMonitoredActivity.this, caller,
                returnedUser->getMonitoredUser(returnedUser));
    }

    private void getMonitoredUser(User returnedUser) {

        EditText editNameView = findViewById(R.id.edit_monitored_name);
        EditText editEmailView = findViewById(R.id.edit_monitored_email);
        EditText editBirthView = findViewById(R.id.edit_monitored_birth_yr);
        EditText editBirthMonthView = findViewById(R.id.edit_monitored_birth_month);
        EditText editAddressView = findViewById(R.id.edit_monitored_address);
        EditText editCellPhoneView = findViewById(R.id.edit_monitored_cell_phone);
        EditText editHomePhoneView = findViewById(R.id.edit_monitored_home_phn);
        EditText editGradeView = findViewById(R.id.edit_monitored_grade);
        EditText editTeacherView = findViewById(R.id.edit_monitored_teacher);
        EditText editEmergencyContactView = findViewById(R.id.edit_monitored_emergency_contact);

        if(returnedUser.getName() != null){
            editNameView.setText(returnedUser.getName());
        }

        if(returnedUser.getEmail() != null){
            editEmailView.setText(returnedUser.getEmail());
        }

        if(returnedUser.getBirthYear() != null){
            editBirthView.setText(returnedUser.getBirthYear().toString());
        }

        if(returnedUser.getBirthMonth() != null){
            editBirthMonthView.setText(returnedUser.getBirthMonth().toString());
        }

        if(returnedUser.getAddress() != null){
            editAddressView.setText(returnedUser.getAddress());
        }

        if(returnedUser.getCellPhone() != null){
            editCellPhoneView.setText(returnedUser.getCellPhone());
        }

        if(returnedUser.getHomePhone() != null){
            editHomePhoneView.setText(returnedUser.getHomePhone());
        }

        if(returnedUser.getGrade() != null){
            editGradeView.setText(returnedUser.getGrade());
        }

        if(returnedUser.getTeacherName() != null){
            editTeacherView.setText(returnedUser.getTeacherName());
        }

        if(returnedUser.getEmergencyContactInfo() != null){
            editEmergencyContactView.setText(returnedUser.getEmergencyContactInfo());
        }

        user = returnedUser;

        setupConfirmButton();

    }

    private void setupConfirmButton() {

        EditText editNameView = findViewById(R.id.edit_monitored_name);
        EditText editEmailView = findViewById(R.id.edit_monitored_email);
        EditText editBirthView = findViewById(R.id.edit_monitored_birth_yr);
        EditText editBirthMonthView = findViewById(R.id.edit_monitored_birth_month);
        EditText editAddressView = findViewById(R.id.edit_monitored_address);
        EditText editCellPhoneView = findViewById(R.id.edit_monitored_cell_phone);
        EditText editHomePhoneView = findViewById(R.id.edit_monitored_home_phn);
        EditText editGradeView = findViewById(R.id.edit_monitored_grade);
        EditText editTeacherView = findViewById(R.id.edit_monitored_teacher);
        EditText editEmergencyContactView = findViewById(R.id.edit_monitored_emergency_contact);

        Button btn = findViewById(R.id.ebtn_confirm);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Confirm Button is working");
                String name = editNameView.getText().toString();
                user.setName(name);

                String email = editEmailView.getText().toString();
                user.setEmail(email);

                String birthYear = editBirthView.getText().toString();

                if(isNumeric(birthYear)){
                    user.setBirthYear(Integer.parseInt(birthYear));
                }

                String birthmonth = editBirthMonthView.getText().toString();

                if(isNumeric(birthmonth)){
                    user.setBirthMonth(Integer.parseInt(birthmonth));
                }


                String address = editAddressView.getText().toString();
                user.setAddress(address);

                String cellPhone = editCellPhoneView.getText().toString();
                user.setCellPhone(cellPhone);

                String homePhone = editHomePhoneView.getText().toString();
                user.setHomePhone(homePhone);

                String grade = editGradeView.getText().toString();
                user.setGrade(grade);

                String teacherName = editTeacherView.getText().toString();
                user.setTeacherName(teacherName);

                String emergencyContactInfo = editEmergencyContactView.getText().toString();
                user.setEmergencyContactInfo(emergencyContactInfo);

                Call<User> caller2 = proxy.editUser(user.getId(),user);
                ProxyBuilder.callProxy(EditMonitoredActivity.this,caller2,returnedUser->doNothing(returnedUser));

            }

            private void doNothing(User returnedUser) {
                Log.d(TAG, "doNothing: Successful edit!");
                Toast.makeText(EditMonitoredActivity.this,"Successfully edited monitored user!",Toast.LENGTH_SHORT);
                finish();
            }
        });
    }
}
