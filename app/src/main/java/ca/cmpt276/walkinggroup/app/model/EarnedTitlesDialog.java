package ca.cmpt276.walkinggroup.app.model;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.List;

import ca.cmpt276.walkinggroup.app.R;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class EarnedTitlesDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_layout, null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        assert (false);
                }
            }
        };

        Session session = Session.getInstance();
        User currentUser = session.getSessionUser();
        List<String> earnedTitles = currentUser.getRewards().getEarnedTitlesList();

        String titles = "";

        for(int i = 0; i < earnedTitles.size(); i++){
            titles = titles + earnedTitles.get(i);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("My Titles")
                .setMessage(titles)
                .setView(dialogView)
                .setNegativeButton("Close", listener)
                .create();
    }
}
