package com.matrix_maeny.myworks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;

import com.matrix_maeny.myworks.databases.WorkDataBaseHelper;

import java.util.Objects;

public class SettingsDialog extends AppCompatDialogFragment {

    private AppCompatButton okBtn;
    private EditText enteredTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.settings_layout, null);
        root.setBackground(new ColorDrawable(Color.TRANSPARENT));
        builder.setView(root);

        enteredTime = root.findViewById(R.id.enteredTime);

        okBtn = root.findViewById(R.id.okBtn);

        okBtn.setOnClickListener(v -> {

            int time = getTime();
            if (time != -1) {
                saveTimeToDataBase(time);
            }

            dismiss();

        });

        return builder.create();

    }

    private void saveTimeToDataBase(int time) {

        WorkDataBaseHelper dataBaseHelper = new WorkDataBaseHelper(requireContext().getApplicationContext());

        if (!dataBaseHelper.updateNotificationTime(time)) {
            Toast.makeText(getContext(), "some error occurred: settings dialog: 62", Toast.LENGTH_SHORT).show();
        }

        dataBaseHelper.close();

    }

    private int getTime() {

        String time = "null";

        try {
            time = enteredTime.getText().toString().trim();
        } catch (Exception e) {
            return -1;
        }

        if (time.equals("")) {
            return -1;
        }

        int timeInt = Integer.parseInt(time);

        if (timeInt == 0) {
            timeInt = 10;
        }
        Toast.makeText(getContext(), "set to " + timeInt + " minutes", Toast.LENGTH_SHORT).show();
        return timeInt;

    }
}
