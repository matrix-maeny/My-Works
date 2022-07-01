package com.matrix_maeny.myworks;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.matrix_maeny.myworks.adapters.WorkAdapter;
import com.matrix_maeny.myworks.databases.WorkDataBaseHelper;
import com.matrix_maeny.myworks.models.WorkModel;
import com.matrix_maeny.myworks.receivers.WorkReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MyWorkAlertDialogBox.WorkListener, WorkAdapter.RefreshTheLayout {


    private final static int NOTIFICATION_PERMISSION_CODE = 1;
    private final static int STORAGE_PERMISSION_CODE = 2;

    RecyclerView recyclerView;  // recycler view id

    ArrayList<WorkModel> list; // to store list of works
    TextView emptyView;
    WorkAdapter adapter;


    MyWorkAlertDialogBox dialogBox;

    WorkDataBaseHelper helper;

    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }


        insertNotifiedData();

        recyclerView = findViewById(R.id.recyclerView);

        emptyView = findViewById(R.id.emptyView);


        list = new ArrayList<>();

        adapter = new WorkAdapter(MainActivity.this, list);
        recyclerView.setAdapter(adapter);
        setTheChanges();
        setTheLayout();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(MainActivity.this, WorkReceiver.class);


//        addBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MyWorkAlertDialogBox dialogBox = new MyWorkAlertDialogBox();
//                dialogBox.show(getSupportFragmentManager(), "work dialog");
//
//            }
//        });
//        addBtn.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
//
//
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN:
//                        dX = v.getX() - event.getRawX();
//                        dY = v.getY() - event.getRawY();
//                        lastAction = MotionEvent.ACTION_DOWN;
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
////                        v.setY(event.getRawY() + dY);
////                        v.setX(event.getRawX() + dX);
//
//                        int viewWidth = v.getWidth();
//                        int viewHeight = v.getHeight();
//
//                        View viewParent = (View) v.getParent();
//                        int parentWidth = viewParent.getWidth();
//                        int parentHeight = viewParent.getHeight();
//
//                        float newX = event.getRawX() + dX;
//                        newX = Math.max(layoutParams.leftMargin, newX); // Don't allow the FAB past the left hand side of the parent
//                        newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX); // Don't allow the FAB past the right hand side of the parent
//
//                        float newY = event.getRawY() + dY;
//                        newY = Math.max(layoutParams.topMargin, newY); // Don't allow the FAB past the top of the parent
//                        newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY); // Don't allow the FAB past the bottom of the parent
//
//                        v.animate()
//                                .x(newX)
//                                .y(newY)
//                                .setDuration(0)
//                                .start();
//
//                        lastAction = MotionEvent.ACTION_MOVE;
//
//
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        if (lastAction == MotionEvent.ACTION_DOWN) {
//                            dialogBox = new MyWorkAlertDialogBox();
//                            dialogBox.show(getSupportFragmentManager(), "work dialog");
//                        }
//                        break;
//
//                    default:
//                        return false;
//                }
//                return true;
//
//            }
//        });


    }

    private void insertNotifiedData() {
        helper = new WorkDataBaseHelper(MainActivity.this);

        if (helper.insertNotification("work", "main work",10)) {
            tempToast("Data successfully installed", 1);
        }

        helper.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addWork() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            dialogBox = new MyWorkAlertDialogBox();
            dialogBox.show(getSupportFragmentManager(), "work dialog");
        } else {
            requestStoragePermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_works_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.delete_all:
                deleteAll();
                break;
            case R.id.add_work:
                addWork();
                break;
            case R.id.settings_:
                openSettings();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog();
        dialog.show(getSupportFragmentManager(),"settings dialog");
    }

    private void deleteAll() {
        WorkDataBaseHelper helper = new WorkDataBaseHelper(MainActivity.this);

        Cursor cursor = helper.getNotificationData();
        Cursor cursor2 = helper.getData();

        cursor.moveToNext();

        while (cursor2.moveToNext()) {
            if (cursor2.getString(0).trim().equals(cursor.getString(1).trim())) {
                int requestCode = cursor2.getInt(2);
                Intent intent = new Intent(MainActivity.this, WorkReceiver.class);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(pendingIntent);
                break;
            }
        }

        if (helper.deleteAll()) {
            tempToast("All deleted..", 1);
        }
        setTheChanges();
        helper.close();
    }

    @Override
    public void saveWork(String name) {
        helper = new WorkDataBaseHelper(MainActivity.this);
        Random random = new Random();
        int requestCode = random.nextInt(2000);

        if (!helper.insertData(name, 0, requestCode)) {
            tempToast("work is already exists", 1);

        }
        helper.close();
        setTheChanges();
    }


    @SuppressLint("NotifyDataSetChanged")
    final void setTheChanges() {
        helper = new WorkDataBaseHelper(MainActivity.this);
        Cursor cursor = helper.getData();

        if (cursor.getCount() > 0) {
            list.clear();

            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                int state = cursor.getInt(1);
                list.add(new WorkModel(name, state));
            }
        } else {
            list.clear();
        }

        if (list.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        helper.close();


    }

    //
    final void setTheLayout() {
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    final void tempToast(String m, int n) {
        if (n == 0) {
            Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, m, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onDestroy() {
        helper.close();
        super.onDestroy();
    }

    @Override
    public void refreshTheLayout() {
        setTheChanges();
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void setRepeatingNotification(String name) {

        WorkDataBaseHelper db = new WorkDataBaseHelper(MainActivity.this);
        Cursor cursor = db.getData();

        while (cursor.moveToNext()) {
            if (cursor.getString(0).equals(name)) {
                break;
            }
        }
        int requestCode = cursor.getInt(2);

        alarmIntent.putExtra("name", name);
        alarmIntent.putExtra("id", requestCode);
        alarmPendingIntent = PendingIntent.getBroadcast(MainActivity.this, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int minute = calendar.get(Calendar.MINUTE) + 2;
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        long time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

        if (System.currentTimeMillis() > time) {

            if (Calendar.AM_PM == 0) {
                time = time + (1000 * 60 * 60 * 12);
            } else {
                time = time + (1000 * 60 * 60 * 24);

            }

        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, alarmPendingIntent);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission needed")
                    .setMessage("Storage permission is needed to save WORKS")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NOTIFICATION_POLICY}, NOTIFICATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NOTIFICATION_POLICY}, STORAGE_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE || requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission DENIED... please ENABLE manually", Toast.LENGTH_SHORT).show();

            }
        }

    }
}