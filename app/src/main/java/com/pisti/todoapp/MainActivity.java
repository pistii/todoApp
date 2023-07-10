package com.pisti.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private android.view.MotionEvent MotionEvent;
    TextView tvDate;
    Button btPickDate;
    Calendar mCalendar;
    public static final String CHANNEL_ID = "CHANNEL_ID#1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Required for notify the user
        createNotificationChannel();

        //NumberPicker
        setNumberPicker();

        //DAtePicker
        setDatePicker();


        final Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the values and send to the db
                final EditText todoName = findViewById(R.id.megnevezesText);
                final NumberPicker prevAlert = findViewById(R.id.numberPicker);

                String name = todoName.getText().toString();
                int mins = prevAlert.getValue();

                notifyUser("Hello wörld", "Hello everibodi", mCalendar.getTime());
                PostTODO(name, mins);
            }
        });
    }


    private void setNumberPicker() {
        String [] minutes = new String [60];
        for(int i= 0;i<60;i++){
            minutes[i] = String.valueOf(i);
        }

        NumberPicker picker = (NumberPicker) findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(59);
        picker.setDisplayedValues(minutes);
    }

    private void setDatePicker() {
        //https://www.geeksforgeeks.org/datepickerdialog-in-android/
        tvDate = findViewById(R.id.tvDate);
        btPickDate = findViewById(R.id.btPickDate);
        btPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.pisti.todoapp.DatePicker mDatePickerDialogFragment;
                mDatePickerDialogFragment = new com.pisti.todoapp.DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "DATE PICK");
            }
        });
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        // Create a Calendar instance
        mCalendar = Calendar.getInstance();
        // Set static variables of Calendar instance
        mCalendar.set(Calendar.YEAR,year);
        mCalendar.set(Calendar.MONTH,month);
        mCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        // Get the date in form of string
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());
        setDate(year + "-" + month + "-" + dayOfMonth);
        // Set the textview to the selectedDate String
        tvDate.setText(selectedDate);


    }

    //Notification
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //Register the channel
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void notifyUser(String title, String description, Date delay) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP , delay.getTime() , pendingIntent) ;
        //This can be called from an admin-like page
        //Here notifies the user with the unique ID
        //The ID should be stored if update or notification remove required.
        NotificationManagerCompat.from(this)
                .notify(CHANNEL_ID.lastIndexOf(-1), builder.build());
    }

    private String date;
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void PostTODO(String name, int prevAlert) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> todos = new HashMap<>();
        todos.put("name", name);
        todos.put("time", getDate());
        todos.put("prevAlert", prevAlert);

        db.collection("todos")
                .document("todayTodos")
                .set(todos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Message: ", "success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Message: ", "Error", e);
                    }
                });
    }
}