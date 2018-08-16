package ca.cmpt276.walkinggroup.app.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.app.user_interface.sign_up_in_activities.SignUpActivity;

/*
 * Preferences class saves data in the application context
 * It saves the token and user email.
 */

public class Preferences {

    private static final String GLOBAL_PREFERENCE_NAME = "globalPreferences";
    private static final String TOKEN_NAME = "token";
    private static final String EMAIL_NAME = "email";
    private static final String MESSAGE_IDS_NAME = "message ids";

    @SuppressLint("StaticFieldLeak")
    private static Context context = SignUpActivity.contextOfApplication;

    public static void saveToken(String token) {
        saveString(TOKEN_NAME, token);
    }

    public static String getToken() {
        return getString(TOKEN_NAME, "Token not found");
    }

    public static boolean isTokenSaved() { return isStringSaved(TOKEN_NAME); }

    public static void saveEmail(String email) {
        saveString(EMAIL_NAME, email);
    }

    public static String getEmail() {
        return getString(EMAIL_NAME, "Email not found");
    }

    public static boolean isEmailSaved() {
        return isStringSaved(EMAIL_NAME);
    }

    public static void saveMessageIdBuild(String messageIdBuild) { saveString(MESSAGE_IDS_NAME, messageIdBuild); }

    public static String getMessageIds() { return getString(MESSAGE_IDS_NAME, "Message ids not found"); }

    public static boolean isMessageIdsSaved() {
        return isStringSaved(MESSAGE_IDS_NAME);
    }

    public static void clear() {
        SharedPreferences preference = context.getSharedPreferences(GLOBAL_PREFERENCE_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.clear();
        editor.apply();
    }

    private static void saveString(String name, String value) {
        SharedPreferences preference = context.getSharedPreferences(GLOBAL_PREFERENCE_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(name, value);
        editor.apply();
    }

    private static String getString(String name, String failMessage) {
        SharedPreferences preference = context.getSharedPreferences(GLOBAL_PREFERENCE_NAME, context.MODE_PRIVATE);
        if (isStringSaved(name)) {
            return preference.getString(name, null);
        } else {
            Log.w("Preferences", "Failed to get string: " + failMessage);
            return null;
        }
    }

    private static boolean isStringSaved(String name) {
        SharedPreferences preference = context.getSharedPreferences(GLOBAL_PREFERENCE_NAME, context.MODE_PRIVATE);
        return preference.contains(name);
    }
}
