package ca.cmpt276.walkinggroup.app.model;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import ca.cmpt276.walkinggroup.app.R;

/* MapDialogFragment class implements the dialog UT to confirm user wants
 * to join a selected walking group on the map
 */

public class MapDialogFragment extends AppCompatDialogFragment {

    private String groupDescription;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View confirmDialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_layout, null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        setConfirmation(true);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        setConfirmation(false);
                        break;
                    default:
                        assert(false);
                }
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle("Join Group Confirmation")
                .setMessage("Do you want to join group \'" + groupDescription + "\'?")
                .setView(confirmDialogView)
                .setPositiveButton("Confirm", listener)
                .setNegativeButton("Cancel", listener)
                .create();
    }

    public void setGroupDescription(String description){
        groupDescription = description;
    }

    public void setConfirmation(boolean confirmation){
        this.mapDialogListener.onComplete(confirmation);
    }

    public static interface OnCompleteListener{
        public abstract void onComplete(boolean confirm);
    }

    private OnCompleteListener mapDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mapDialogListener = (OnCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + ": must implement OnCompleteListener");
        }
    }
}
