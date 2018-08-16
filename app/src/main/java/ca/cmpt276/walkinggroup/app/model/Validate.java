package ca.cmpt276.walkinggroup.app.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.regex.Pattern;

// Validate class handles validation of objects.

public class Validate {
    public static boolean isValidEmailAddress(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidFirstName(String firstName) {
        return Pattern.matches("[a-zA-Z]+", firstName);
    }

    public static boolean isValidLastName(String lastName) {
        return Pattern.matches("[a-zA-Z]+", lastName);
    }

    public static boolean isCorrectGooglePlayVersion(Context context) {
        final String TAG = context.toString();
        final int ERROR_DIALOG_REQUEST = 9001;
        Log.d(TAG ,"checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "an error occurred but it is fixable");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(context, "Map request cannot be made", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
