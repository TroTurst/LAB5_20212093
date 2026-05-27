package com.example.lab4_20212093;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab4_20212093.database.EventDAO;
import com.example.lab4_20212093.model.Event;
import com.example.lab4_20212093.notification.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etName;
    private Button btnPickDate;
    private CheckBox cbIncludeTime;
    private TimePicker tpTime;
    private RadioGroup rgPeriodicity;
    private RadioButton rbOnce;
    private RadioButton rbAnnual;
    private Spinner spNotifyDays;
    private Button btnSave;

    private EventDAO eventDAO;
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private Integer editingEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        btnPickDate = findViewById(R.id.btnPickDate);
        cbIncludeTime = findViewById(R.id.cbIncludeTime);
        tpTime = findViewById(R.id.tpTime);
        rgPeriodicity = findViewById(R.id.rgPeriodicity);
        rbOnce = findViewById(R.id.rbOnce);
        rbAnnual = findViewById(R.id.rbAnnual);
        spNotifyDays = findViewById(R.id.spNotifyDays);
        btnSave = findViewById(R.id.btnSave);

        eventDAO = new EventDAO(this);

        setupSpinner();
        initDefaultDate();

        btnPickDate.setOnClickListener(v -> openDatePicker());
        cbIncludeTime.setOnCheckedChangeListener((buttonView, isChecked) ->
            tpTime.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        editingEventId = getIntent().hasExtra("eventId")
            ? getIntent().getIntExtra("eventId", -1)
            : null;
        if (editingEventId != null && editingEventId >= 0) {
            loadEvent(editingEventId);
        } else {
            rbOnce.setChecked(true);
        }

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.notify_options,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNotifyDays.setAdapter(adapter);
    }

    private void initDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedYear = calendar.get(Calendar.YEAR);
        updateDateButton();
    }

    private void openDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month + 1;
                selectedDay = dayOfMonth;
                updateDateButton();
            },
            selectedYear,
            selectedMonth - 1,
            selectedDay
        );
        dialog.show();
    }

    private void updateDateButton() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnPickDate.setText(formatter.format(calendar.getTime()));
    }

    private void loadEvent(int eventId) {
        Event event = eventDAO.getEventById(eventId);
        if (event == null) {
            finish();
            return;
        }

        etName.setText(event.getName());
        selectedDay = event.getDay();
        selectedMonth = event.getMonth();
        selectedYear = event.getYear();
        updateDateButton();

        if (event.getHour() >= 0 && event.getMinute() >= 0) {
            cbIncludeTime.setChecked(true);
            tpTime.setVisibility(View.VISIBLE);
            tpTime.setHour(event.getHour());
            tpTime.setMinute(event.getMinute());
        } else {
            cbIncludeTime.setChecked(false);
            tpTime.setVisibility(View.GONE);
        }

        if ("ANNUAL".equals(event.getPeriodicity())) {
            rbAnnual.setChecked(true);
        } else {
            rbOnce.setChecked(true);
        }

        spNotifyDays.setSelection(indexFromNotifyDays(event.getNotifyDaysBefore()));
    }

    private boolean ensureExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                );
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

    private void saveEvent() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.label_name));
            return;
        }

        Event event = new Event();
        if (editingEventId != null && editingEventId >= 0) {
            event.setId(editingEventId);
        }
        event.setName(name);
        event.setDay(selectedDay);
        event.setMonth(selectedMonth);
        event.setYear(selectedYear);

        if (cbIncludeTime.isChecked()) {
            event.setHour(tpTime.getHour());
            event.setMinute(tpTime.getMinute());
        } else {
            event.setHour(-1);
            event.setMinute(-1);
        }

        String periodicity = rbAnnual.isChecked() ? "ANNUAL" : "ONCE";
        event.setPeriodicity(periodicity);
        event.setNotifyDaysBefore(notifyDaysFromIndex(spNotifyDays.getSelectedItemPosition()));

        if (editingEventId != null && editingEventId >= 0) {
            NotificationHelper.cancelNotification(this, event.getId());
            eventDAO.updateEvent(event);
            if (ensureExactAlarmPermission()) {
                NotificationHelper.scheduleNotification(this, event);
            }
        } else {
            int id = eventDAO.insertEvent(event);
            event.setId(id);
            if (ensureExactAlarmPermission()) {
                NotificationHelper.scheduleNotification(this, event);
            }
        }

        finish();
    }

    private int notifyDaysFromIndex(int index) {
        switch (index) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 7;
            default:
                return 0;
        }
    }

    private int indexFromNotifyDays(int days) {
        switch (days) {
            case 1:
                return 1;
            case 3:
                return 2;
            case 7:
                return 3;
            default:
                return 0;
        }
    }
}
