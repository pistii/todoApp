package com.pisti.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextView tvDate;
    Button btPickDate;
    Calendar mCalendar;
    public static final String CHANNEL_ID = "CHANNEL_ID#1";

    private String date;
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

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
        btnOk.setOnClickListener(view -> {
            //Get the values and send to the db
            final EditText todoName = findViewById(R.id.megnevezesText);
            final NumberPicker prevAlert = findViewById(R.id.numberPicker);

            int mins = prevAlert.getValue();
            if (mCalendar != null && todoName != null) {
                notifyUser();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (todoName.getText().toString().isEmpty()) {
                    builder.setMessage("Név megadása kötelező");
                } else {
                    //TODO dátum validálás
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            if (new SimpleDateFormat("yyyy/MM/dd").parse(getDate()).compareTo(LocalDate.now())) {

                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    } */
                    builder.setMessage("Nem megfelelő dátum");

                }
                builder.setTitle("Error");
                builder.setPositiveButton("Ok", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            PostTODO(todoName.getText().toString(), mins);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.Todos) {
            startActivity(new Intent(this, Todo.class));
            return true;
        } else {
            return false;
        }
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
        btPickDate.setOnClickListener(v -> {
            com.pisti.todoapp.DatePicker mDatePickerDialogFragment;
            mDatePickerDialogFragment = new com.pisti.todoapp.DatePicker();
            mDatePickerDialogFragment.show(getSupportFragmentManager(), "DATE PICK");
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
        long beforeAlert = prevAlert.getValue();
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = sdf.parse(getDate());
            NumberPicker hour = findViewById(R.id.HourPicker);
            long h = hour.getValue() * 60 * 60 * 1000;
            NumberPicker mins = findViewById(R.id.MinutePicker);
            long m = mins.getValue() * 60 * 1000;
            //When the user needed to be alert
            long alertTime = currentTime + m + h - beforeAlert;

            String text = "Alert set on\n" + getDate() + "\n" + hour.getValue() + ":" + mins.getValue();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            alarmManager.set(AlarmManager.RTC_WAKEUP , alertTime , pendingIntent) ;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
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
                .addOnSuccessListener(aVoid -> Log.d("Message: ", "success!"))
                .addOnFailureListener(e -> Log.w("Message: ", "Error", e));
    }
}