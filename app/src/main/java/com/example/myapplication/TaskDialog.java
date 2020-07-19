package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;

public class TaskDialog {
    public static Dialog getCustomDialog(Context context, int layout) {
        Dialog dialog;
        dialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
        return dialog;
    }
}
