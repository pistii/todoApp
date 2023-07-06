package com.pisti.todoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private android.view.MotionEvent MotionEvent;
    TextView tvDate;
    Button btPickDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //NumberPicker
        String [] minutes = new String [60];
        for(int i= 0;i<60;i++){
            minutes[i] = String.valueOf(i);
        }

        NumberPicker picker = (NumberPicker) findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(59);
        picker.setDisplayedValues(minutes);

        //DAtePicker
        tvDate = findViewById(R.id.tvDate);
        btPickDate = findViewById(R.id.btPickDate);
        btPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Please note that use your package name here
                com.pisti.todoapp.DatePicker mDatePickerDialogFragment;
                mDatePickerDialogFragment = new com.pisti.todoapp.DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "DATE PICK");
            }
        });

        final Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the values and send to the firebase
                final EditText todoName = findViewById(R.id.megnevezesText);
                final NumberPicker prevAlert = findViewById(R.id.numberPicker);
                //final DatePicker alertDate = findViewById(R.id.datePicker);

                String name = todoName.getText().toString();
                int mins = prevAlert.getValue();
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                //int year = tvDate
                //int month = tv.getMonth();
                //int day = alertDate.getDayOfMonth();

                addToDB(name, tvDate.getText().toString(), mins);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        // Create a Calendar instance
        Calendar mCalendar = Calendar.getInstance();
        // Set static variables of Calendar instance
        mCalendar.set(Calendar.YEAR,year);
        mCalendar.set(Calendar.MONTH,month);
        mCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        // Get the date in form of string
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());
        // Set the textview to the selectedDate String
        tvDate.setText(selectedDate);
    }

    
    public void addToDB(String name, String date, int prevAlert) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> todos = new HashMap<>();
        todos.put("name", name);
        todos.put("time", date);
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