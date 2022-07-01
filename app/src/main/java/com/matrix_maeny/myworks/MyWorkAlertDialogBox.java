package com.matrix_maeny.myworks;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;

public class MyWorkAlertDialogBox extends AppCompatDialogFragment {

    private EditText enterWork;
    private AppCompatButton addBtn;
    private int position;


    private WorkListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(),R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.activity_work,null);
        root.setBackground(new ColorDrawable(Color.TRANSPARENT));
        builder.setView(root);

        enterWork = root.findViewById(R.id.enterWork);

        addBtn = root.findViewById(R.id.addWork);


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enterWork.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Please enter work", Toast.LENGTH_SHORT).show();
                }else {
                    String workText = enterWork.getText().toString();

                    if (!workText.trim().equals("")) {
                         workText = enterWork.getText().toString();
                        workText = workText.trim();
                        listener.saveWork(workText);
                        dismiss();
                    }else {
                        Toast.makeText(getContext(), "Enter the work", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (WorkListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Must implement WorkListener");
        }
    }

    public interface WorkListener{
        void saveWork(String name);
    }
}
