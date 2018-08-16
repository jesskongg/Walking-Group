package ca.cmpt276.walkinggroup.app.model;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;


import ca.cmpt276.walkinggroup.app.R;


public class ViewMessageDialogFragment extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View messageDialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_layout, null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                    default:
                        assert (false);
                }
            }
        };

        int messageBorder = SelectedMessage.getMessage().indexOf("\n");
        String messageSubject;
        String messageBody;

        if(messageBorder > 0) {
            messageSubject = SelectedMessage.getMessage().substring(0, messageBorder);
            messageBody = SelectedMessage
                    .getMessage()
                    .substring(messageBorder + 1, SelectedMessage.getMessage().length());
        } else if(messageBorder ==  0) {
            messageSubject = "No Subject";
            messageBody = SelectedMessage.getMessage().substring(1);
        } else {
            messageSubject = SelectedMessage.getMessage();
            messageBody = "";
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("Subject: " + messageSubject)
                .setMessage("From: " + SelectedMessage.getFromName()
                        + "\n\n" + "Message: " + messageBody)
                .setView(messageDialogView)
                .setPositiveButton("Close", listener)
                .create();
    }
}
