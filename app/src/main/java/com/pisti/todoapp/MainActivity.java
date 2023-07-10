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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        setNumberPicker(0, 60, R.id.numberPicker);

        //DAtePicker
        setDatePicker();

        //Hour, and minute picker
        setNumberPicker(0, 24, R.id.HourPicker);
        setNumberPicker(0, 60, R.id.MinutePicker);

        final Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the values and send to the db
                final EditText todoName = findViewById(R.id.megnevezesText);
                final NumberPicker prevAlert = findViewById(R.id.numberPicker);

                String name = todoName.getText().toString();
                int mins = prevAlert.getValue();
                if (mCalendar != null) {
                    notifyUser();
                }
                PostTODO(name, mins);
            }
        });
    }

    private void setNumberPicker(int minValue, int maxValue, int ID) {
        String [] values = new String [maxValue];
        for(int i= minValue;i<maxValue;i++){
            values[i] = String.valueOf(i);
        }

        NumberPicker picker = (NumberPicker) findViewById(ID);
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue-1);
        picker.setDisplayedValues(values);
    }

    private void setDatePicker() {
        //https://www.geeksforgeeks.org/datepickerdialog-in-android/
        tvDate = findViewById(R.id.tvDate);
        btPickDate = findViewById(R.id.btnPickDate);
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

    private void notifyUser() {
        Intent intent = new Intent(this, NotificationPublisher.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE ) ;
        assert alarmManager != null;

        final NumberPicker prevAlert = findViewById(R.id.numberPicker);
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = sdf.parse(getDate());
            NumberPicker hour = findViewById(R.id.HourPicker);
            long h = hour.getValue() * 60 * 60 * 1000;
            NumberPicker mins = findViewById(R.id.MinutePicker);
            long m = mins.getValue() * 60 * 1000;
            long alertTime = currentTime + m + h;

            String text = "Alert set on\n" + getDate() + "\n" + hour.getValue() + ":" + mins.getValue();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            alarmManager.set(AlarmManager.RTC_WAKEUP , alertTime , pendingIntent) ;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        //This can be called from an admin-like page
        //Here notifies the user with the unique ID
        //The ID should be stored if update or notification remove required.
        //NotificationManagerCompat.from(this)
        //        .notify(CHANNEL_ID.lastIndexOf(-1), builder.build());

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