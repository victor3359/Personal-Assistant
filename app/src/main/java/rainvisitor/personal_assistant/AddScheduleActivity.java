package rainvisitor.personal_assistant;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class AddScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private long count = 0;
    private android.support.v7.widget.Toolbar toolbar;
    private EditText editText_title, editText_content;
    private TextView textView_location, textView_dateStart, textView_timeStart, textView_dateEnd, textView_timeEnd;
    private LinearLayout linearLayout;
    private Calendar now;
    private int current_date = 0, current_time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);
        initToolbar();
        initView();
        now = Calendar.getInstance();
        String date = now.get(Calendar.YEAR) + "年" + now.get(Calendar.MONTH) + "月" + now.get(Calendar.DAY_OF_MONTH) + "日";
        String timeS = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
        String timeE = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
        textView_dateStart.setText(date);
        textView_dateEnd.setText(date);
        textView_timeStart.setText(timeS);
        textView_timeEnd.setText(timeE);
        textView_dateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddScheduleActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                current_date = 1;
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        textView_dateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddScheduleActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                current_date = 2;
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        textView_timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        AddScheduleActivity.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false);
                current_time = 1;
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
        textView_timeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        AddScheduleActivity.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false);
                current_time = 1;
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
    }

    private void initView() {
        editText_title = (EditText) findViewById(R.id.edittext_title);
        editText_content = (EditText) findViewById(R.id.edittext_content);
        textView_location = (TextView) findViewById(R.id.textview_location);
        textView_dateStart = (TextView) findViewById(R.id.textview_startdate);
        textView_timeStart = (TextView) findViewById(R.id.textview_starttime);
        textView_dateEnd = (TextView) findViewById(R.id.textview_enddate);
        textView_timeEnd = (TextView) findViewById(R.id.textview_endtime);
        linearLayout = (LinearLayout) findViewById(R.id.linelayout_addschedule);
    }

    private void initToolbar() {
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setTitle("新增行程");
        toolbar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_cancel:
                        finish();
                        break;
                    case R.id.action_save:
                        saveSchedule();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.toolber_add);
        //setSupportActionBar(toolbar);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = +dayOfMonth + "/" + (++monthOfYear) + "/" + year;
        Log.e("onDateSet", view.toString());
        switch (current_date) {
            case 1:
                textView_dateStart.setText(date);
                break;
            case 2:
                textView_dateEnd.setText(date);
                break;
            default:
                break;
        }
    }

    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        String time = hourString + ":" + minuteString;
        switch (current_time) {
            case 1:
                textView_timeStart.setText(time);
                break;
            case 2:
                textView_timeEnd.setText(time);
                break;
            default:
                break;
        }
    }

    private void saveSchedule() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (textView_location.getText().toString().equals("") || editText_content.getText().toString().equals("") ||
                        editText_title.getText().toString().equals("") || textView_dateStart.getText().toString().equals("") ||
                        textView_timeStart.getText().toString().equals("") || textView_dateEnd.getText().toString().equals("") ||
                        textView_timeEnd.getText().toString().equals("")) {
                    final Snackbar snackbar = Snackbar
                            .make(linearLayout, "請勿留空", Snackbar.LENGTH_INDEFINITE)
                            .setAction("確定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                    snackbar.show();
                } else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid;
                    if (user != null) {
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        Uri photoUrl = user.getPhotoUrl();

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getToken() instead.
                        uid = user.getUid();
                        Log.e("getCurrentUser","uid = "  + uid +"  name = "  + name +"  email = "  + email +"  photoUrl = "  + photoUrl );
                    }
                    else uid = "0";
                    DatabaseReference mDatabase = dataSnapshot.child("activity").getRef();
                    DatabaseReference userDatabase = dataSnapshot.child("users").child(uid).getRef();
                    count = dataSnapshot.child("activity").getChildrenCount();
                    Log.e("count", count + "");
                    DatabaseReference dr = mDatabase.child((count + 1) + "").getRef();
                    dr.child("location").setValue(textView_location.getText() + "");
                    dr.child("content").setValue(editText_content.getText() + "");
                    dr.child("title").setValue(editText_title.getText() + "");
                    dr.child("time").child("begin").setValue(textView_dateStart.getText() + " " + textView_timeStart.getText() + "");
                    dr.child("time").child("end").setValue(textView_dateEnd.getText() + " " + textView_timeEnd.getText() + "");
                    dr.child("members").child("0").child("authority").setValue("creator");
                    dr.child("members").child("0").child("uid").setValue(uid);
                    finish();
                    long count_activity = dataSnapshot.child("users").child(uid).child("activtys").getChildrenCount();
                    Log.e("count_activity","count_activity" + count_activity);
                    userDatabase.child("activtys").child((count_activity+1)+"").child("uid").setValue((count + 1) + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
        if (dpd != null) dpd.setOnDateSetListener(this);
    }
}
