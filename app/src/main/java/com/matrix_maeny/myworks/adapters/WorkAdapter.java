package com.matrix_maeny.myworks.adapters;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.matrix_maeny.myworks.R;
import com.matrix_maeny.myworks.databases.WorkDataBaseHelper;
import com.matrix_maeny.myworks.models.WorkModel;
import com.matrix_maeny.myworks.receivers.WorkReceiver;

import java.util.ArrayList;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.viewHolder> {

    ArrayList<WorkModel> list;
    Context context;
    int state;
    NotificationManagerCompat notificationCompat;

    boolean isNotificationBooked = false;
    int notificationPosition = -1;
    WorkModel tempModel = null;
    Handler handler = null;


    private final RefreshTheLayout refresh;

    public WorkAdapter(Context context, ArrayList<WorkModel> list) {
        this.list = list;
        this.context = context;
        refresh = (RefreshTheLayout) context;

        notificationCompat = NotificationManagerCompat.from(context.getApplicationContext());


    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.work_vie_model, parent, false);
        return new viewHolder(view);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        WorkModel model = list.get(position);
        holder.workName.setText(model.getWorkName());


        state = model.getState();
        if (state == 1) {
            holder.workName.setPaintFlags(holder.workName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.workName.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.checkBox.setBackground(new ColorDrawable(Color.TRANSPARENT));
            // holder.viewLayout.setBackgroundResource(R.drawable.notification_spotter);

            holder.checkBox.setChecked(true);
        } else {
            holder.workName.setPaintFlags(holder.workName.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
            holder.workName.setTypeface(null, Typeface.BOLD);
            holder.checkBox.setBackgroundResource(R.drawable.task_completed);
            holder.checkBox.setChecked(false);

        }

        if (isNotifiedWork(model.getWorkName())) {
            holder.viewLayout.setBackgroundResource(R.drawable.notification_spotter);
        }


        holder.cardView.setOnLongClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context.getApplicationContext(), holder.cardView);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.en_notifications:

                        if (!isNotificationBooked && model.getState() == 0) {
                            notificationPosition = holder.getAdapterPosition();
                            state = model.getState();
                            tempModel = model;
                            isNotificationBooked = true;
                            refresh.setRepeatingNotification(model.getWorkName());
                            Toast.makeText(context.getApplicationContext(), "Notification enable for " + model.getWorkName(), Toast.LENGTH_LONG).show();
                            holder.viewLayout.setBackgroundResource(R.drawable.notification_spotter);

                            updateNotification(model.getWorkName());

                        } else {

                            if (notificationPosition != -1) {
                                String tempName = list.get(notificationPosition).getWorkName();
                                tempName = "Already enabled for the work: " + tempName;
                                Toast.makeText(context.getApplicationContext(), tempName, Toast.LENGTH_LONG).show();
                            }
                            if (model.getState() == 1) {
                                Toast.makeText(context.getApplicationContext(), "Work is already completed", Toast.LENGTH_SHORT).show();

                            }

                        }

                        break;
                    case R.id.delete:
                        if (isNotificationBooked && notificationPosition == holder.getAdapterPosition()) {
                            isNotificationBooked = false;
                            if (handler != null) {
                                handler.removeCallbacksAndMessages(handler);
                            }
                        }
                        deleteData(holder.workName.getText().toString());
                        refresh.refreshTheLayout();
                        break;

                }
                return true;
            });
            popupMenu.show();

            return false;
        });
        holder.cardView.setOnClickListener(v -> {
            WorkModel model1 = list.get(holder.getAdapterPosition());
            int s = model1.getState();
            if (s == 1) {
                Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Task incomplete", Toast.LENGTH_SHORT).show();

            }
        });


        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.workName.setPaintFlags(holder.workName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.workName.setTypeface(null, Typeface.BOLD_ITALIC);
                updateState(holder.workName.getText().toString(), 1);
                model.setState(1);
                if (state == 0) {
                    Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();
                }
                holder.checkBox.setBackground(new ColorDrawable(Color.TRANSPARENT));
                state = 1;
                cancelIntent(model.getWorkName());
                holder.viewLayout.setBackgroundResource(R.drawable.view_model_backgroud);

                if (notificationPosition == holder.getAdapterPosition()) {
                    isNotificationBooked = false;
                    tempModel = model;
                    holder.viewLayout.setBackgroundResource(R.drawable.view_model_backgroud);
                }
            } else {
                holder.workName.setPaintFlags(holder.workName.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
                holder.workName.setTypeface(null, Typeface.BOLD);
                holder.checkBox.setBackgroundResource(R.drawable.task_completed);

                state = 0;
                model.setState(0);

                updateState(holder.workName.getText().toString(), 0);
                //model.setState(0);

            }
        });


    }


    final void deleteData(String name) {
        WorkDataBaseHelper helper = new WorkDataBaseHelper(context.getApplicationContext());


        cancelIntent(name);

        if (helper.deleteData(name)) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
        helper.close();
    }

    private void cancelIntent(String name) {
        WorkDataBaseHelper helper = new WorkDataBaseHelper(context.getApplicationContext());

        Cursor cursor = helper.getNotificationData();
        Cursor cursor2 = helper.getData();

        cursor.moveToNext();

        while (cursor2.moveToNext()) {
            if (cursor2.getString(0).trim().equals(name.trim())) {
                break;
            }
        }

        if (cursor.getString(1).trim().equals(name.trim())) {
            // cancel notification
            int requestCode = cursor2.getInt(2);
            Intent intent = new Intent(context.getApplicationContext(), WorkReceiver.class);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
        }

        helper.updateNotification("");

        helper.close();

    }

    private void updateNotification(String name) {

        WorkDataBaseHelper dataBaseHelper = new WorkDataBaseHelper(context.getApplicationContext());

        if (!dataBaseHelper.updateNotification(name)) {
            Toast.makeText(context, "Some error occurred: WorkAdapter: 207", Toast.LENGTH_SHORT).show();
        }
        dataBaseHelper.close();
    }

    private boolean isNotifiedWork(String name) {

        WorkDataBaseHelper dataBaseHelper = new WorkDataBaseHelper(context.getApplicationContext());

        Cursor cursor = dataBaseHelper.getNotificationData();
        String temp = "";
        try {
            cursor.moveToNext();
            temp = cursor.getString(1);
            return temp.trim().equals(name.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    final void deleteAll() {
//        WorkDataBaseHelper helper = new WorkDataBaseHelper(context.getApplicationContext());
//
//        helper.deleteAll();
//        helper.close();
//    }

    final void updateState(String name, int state) {
        WorkDataBaseHelper helper = new WorkDataBaseHelper(context);
        if (!helper.updateData(name, state)) {
            Toast.makeText(context, "Error updating Data", Toast.LENGTH_SHORT).show();
        }
        helper.close();
    }


    public interface RefreshTheLayout {
        void refreshTheLayout();

        void setRepeatingNotification(String name);
    }


//    private void sendNotification(String name) {
//
//        refresh.setRepeatingNotification(name);
//
////        Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), NotificationSender.CHANNEL_ID)
////                .setSmallIcon(R.drawable.test)
////                .setContentTitle(name)
////                .setContentText("You have a pending work: " + name)
////                .setPriority(Notification.PRIORITY_HIGH)
////                .setCategory(Notification.CATEGORY_MESSAGE)
////                .build();
////
////
////        handler = new Handler();
////        handler.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                if (isNotificationBooked && tempModel.getState() == 0) {
////                    notificationCompat.notify(0, notification);
////                    handler.postDelayed(this, 300000);
////                } else {
////                    isNotificationBooked = false;
////                    notificationCompat.cancel(0);
////                }
////            }
////        }, 300000);
//
//
//    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView workName;
        CheckBox checkBox;
        CardView cardView;
        LinearLayout viewLayout;


        public viewHolder(@NonNull View itemView) {
            super(itemView);

            workName = itemView.findViewById(R.id.workName);
            checkBox = itemView.findViewById(R.id.checkBox);
            cardView = itemView.findViewById(R.id.cardView);
            viewLayout = itemView.findViewById(R.id.viewLayout);

        }
    }
}
