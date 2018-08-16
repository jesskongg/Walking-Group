package ca.cmpt276.walkinggroup.app.user_interface.edit_info_activities;

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

// Class EditUserActivity allows editing of user information.

public class EditUserActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private Session session;
    private static final String TAG = "EditUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        session = Session.getInstance();
        proxy = session.getSessionProxy();
        displayUserDetails(session.getSessionUser());
    }
    

    private void displayUserDetails(User sessionUser) {
         EditText editNameView = findViewById(R.id.edit_name);
         EditText editEmailView = findViewById(R.id.edit_email);
         EditText editBirthView = findViewById(R.id.edit_birth_yr);
         EditText editBirthMonthView = findViewById(R.id.edit_birth_month);
         EditText editAddressView = findViewById(R.id.edit_address);
         EditText editCellPhoneView = findViewById(R.id.edit_cell_phone);
         EditText editHomePhoneView = findViewById(R.id.edit_home_phn);
         EditText editGradeView = findViewById(R.id.edit_grade);
         EditText editTeacherView = findViewById(R.id.edit_teacher);
         EditText editEmergencyContactView = findViewById(R.id.edit_emergency_contact);

        if(sessionUser.getName()!=null){
            editNameView.setText(sessionUser.getName());
        }

        if(sessionUser.getEmail()!=null){
            editEmailView.setText(sessionUser.getEmail());
        }

        if(sessionUser.getBirthYear()!=null){
            editBirthView.setText(sessionUser.getBirthYear().toString());
        }

        if(sessionUser.getBirthMonth()!=null){
            editBirthMonthView.setText(sessionUser.getBirthMonth().toString());
        }

        if(sessionUser.getAddress()!=null){
            editAddressView.setText(sessionUser.getAddress());
        }

        if(sessionUser.getCellPhone()!=null){
            editCellPhoneView.setText(sessionUser.getCellPhone());
        }

        if(sessionUser.getHomePhone()!=null){
            editHomePhoneView.setText(sessionUser.getHomePhone());
        }

        if(sessionUser.getGrade()!=null){
            editGradeView.setText(sessionUser.getGrade());
        }

        if(sessionUser.getTeacherName()!=null){
            editTeacherView.setText(sessionUser.getTeacherName());
        }

        if(sessionUser.getEmergencyContactInfo()!=null){
            editEmergencyContactView.setText(sessionUser.getEmergencyContactInfo());
        }

        setupConfirmButton(session.getSessionUser());


    }
    private void setupConfirmButton(User sessionUser) {
         EditText editNameView = findViewById(R.id.edit_name);
         EditText editEmailView = findViewById(R.id.edit_email);
         EditText editBirthView = findViewById(R.id.edit_birth_yr);
         EditText editBirthMonthView = findViewById(R.id.edit_birth_month);
         EditText editAddressView = findViewById(R.id.edit_address);
         EditText editCellPhoneView = findViewById(R.id.edit_cell_phone);
         EditText editHomePhoneView = findViewById(R.id.edit_home_phn);
         EditText editGradeView = findViewById(R.id.edit_grade);
         EditText editTeacherView = findViewById(R.id.edit_teacher);
         EditText editEmergencyContactView = findViewById(R.id.edit_emergency_contact);

        Button btn = findViewById(R.id.btn_confirm);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Confirm Button is working");
                String name = editNameView.getText().toString();
                sessionUser.setName(name);

                String email = editEmailView.getText().toString();
                sessionUser.setEmail(email);
                Preferences.saveEmail(email);

                String birthYear = editBirthView.getText().toString();
                if(isNumeric(birthYear)) {
                    sessionUser.setBirthYear(Integer.parseInt(birthYear));
                }

                String birthMonth = editBirthMonthView.getText().toString();
                if(isNumeric(birthMonth)) {
                    sessionUser.setBirthMonth(Integer.parseInt(birthMonth));
                }

                String address = editAddressView.getText().toString();
                sessionUser.setAddress(address);

                String cellPhone = editCellPhoneView.getText().toString();
                sessionUser.setCellPhone(cellPhone);

                String homePhone = editHomePhoneView.getText().toString();
                sessionUser.setHomePhone(homePhone);

                String grade = editGradeView.getText().toString();
                sessionUser.setGrade(grade);

                String teacherName = editTeacherView.getText().toString();
                sessionUser.setTeacherName(teacherName);

                String emergencyContactInfo = editEmergencyContactView.getText().toString();
                sessionUser.setEmergencyContactInfo(emergencyContactInfo);

                Call<User> caller = proxy.editUser(sessionUser.getId(), sessionUser);
                ProxyBuilder.callProxy(EditUserActivity.this, caller, returnedUser->doNothing(returnedUser));
            }

            private void doNothing(User returnedUser) {
                Log.d(TAG, "doNothing: Successful edit!!!");
                Toast.makeText(EditUserActivity.this,"Successfully edited user!",Toast.LENGTH_SHORT);
                finish();
            }
        });
    }


}
