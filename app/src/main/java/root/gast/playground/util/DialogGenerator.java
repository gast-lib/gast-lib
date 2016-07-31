package root.gast.playground.util;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

/**
 * makes various kinds of dialogs
 * @author milette
 */
public class DialogGenerator
{
    public static final DialogInterface.OnClickListener DO_NOTHING = 
        new DialogInterface.OnClickListener(){ public void onClick(DialogInterface arg0, int arg1) {}};
    
    public static AlertDialog createYesNoInfoDialog(Context finalContext, String message, 
        DialogInterface.OnClickListener onNoListener, DialogInterface.OnClickListener onYesListener)
    {
        AlertDialog a = new AlertDialog.Builder(finalContext).setTitle(
            message)
            .setNegativeButton("No", onNoListener)
            .setPositiveButton("Yes", onYesListener).create();   

        return a;
    }

    public static AlertDialog createConfirmDialog(Context finalContext, 
            String title,
            String confirmWhat, 
            DialogInterface.OnClickListener onNoListener, String noButtonText, 
            DialogInterface.OnClickListener onYesListener, String yesButtonText)
        {
            AlertDialog a = new AlertDialog.Builder(finalContext).setTitle(
                    title)
                .setMessage(confirmWhat)
                .setNegativeButton(noButtonText, onNoListener)
                .setPositiveButton(yesButtonText, onYesListener).create();   

            return a;
        }

    public static AlertDialog createConfirmDialog(Context finalContext, String confirmWhat, 
        DialogInterface.OnClickListener onNoListener, DialogInterface.OnClickListener onYesListener)
    {
        return createConfirmDialog(finalContext, "Confirm", confirmWhat, onNoListener, "No", onYesListener, "Yes");
    }

    public static AlertDialog createErrorDialog(Context finalContext, String title, String message)
    {
        return DialogGenerator.createErrorDialog(finalContext, title, message, DO_NOTHING);
    }

    public static AlertDialog createErrorDialog(Context finalContext, String title, String message, 
        DialogInterface.OnClickListener onOkListener)
    {
        AlertDialog a = new AlertDialog.Builder(finalContext)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton("OK", onOkListener).create();   

        return a;
    }

    public static AlertDialog createInfoDialog(Context finalContext, String title, String message)
    {
        return createInfoDialog(finalContext, title, message, DO_NOTHING);
    }

    public static AlertDialog createInfoDialog(Context finalContext, String title, String message,
        DialogInterface.OnClickListener onOkListener)
    {
        AlertDialog a = new AlertDialog.Builder(finalContext).setTitle(
            title)
            .setMessage(message)
            .setNeutralButton("OK", onOkListener).create();   

        return a;
    }

    public static AlertDialog createInfoDialogWithIcon(Context finalContext, String title, String message,
        String okButtonText, int iconRes,
        DialogInterface.OnClickListener onOkListener)
    {
        AlertDialog a = new AlertDialog.Builder(finalContext).setTitle(
            title)
            .setMessage(message)
            .setNeutralButton(okButtonText, onOkListener)
            .setIcon(iconRes)
            .create();   

        return a;
    }

    public static AlertDialog createConfirmDialog(Context finalContext, String confirmWhat, 
        DialogInterface.OnClickListener onYesListener)
    {
        return createConfirmDialog(finalContext, confirmWhat, DO_NOTHING, onYesListener);
    }

    public static <T> AlertDialog makeSelectListDialog(String prompt,
        Context finalContext, List<T> listItems, final DialogInterface.OnClickListener onYesListener)
    {
        return makeSelectListDialog(prompt, finalContext, listItems, onYesListener, DO_NOTHING);
    }
    /**
     * select an item from the list
     * @param <T>
     */
    public static <T> AlertDialog makeSelectListDialog(String prompt,
        Context finalContext, List<T> listItems, final DialogInterface.OnClickListener onYesListener,
        final DialogInterface.OnClickListener onNoListener)
    {
        final ArrayAdapter<T> adapt = 
            new ArrayAdapter<T>(finalContext, android.R.layout.select_dialog_item, listItems);
        AlertDialog a = new AlertDialog.Builder(finalContext)
            .setTitle(prompt)
            .setAdapter(adapt, onYesListener)
            .setNegativeButton("Cancel", onNoListener)
                .create();
        
        return a;
    }
    
    public static AlertDialog createFrameDialog(Activity finalContext, 
            String title, 
            View layout,
            DialogInterface.OnClickListener onYesListener)
        {
            final FrameLayout frame = new FrameLayout(finalContext);
            
            frame.addView(layout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
            
            AlertDialog.Builder ab = new AlertDialog.Builder(finalContext);
            
            AlertDialog a = ab.setTitle(title).setView(frame).setCancelable(true)
                            .setNegativeButton("Cancel",
                                DO_NOTHING)
                                .setPositiveButton("OK", onYesListener).create();
            return a;
        }
}
