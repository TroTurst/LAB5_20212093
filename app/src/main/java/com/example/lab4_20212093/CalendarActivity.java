package com.example.lab4_20212093;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab4_20212093.adapter.EventAdapter;
import com.example.lab4_20212093.database.EventDAO;
import com.example.lab4_20212093.model.Event;
import com.example.lab4_20212093.notification.NotificationHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    private EventDAO eventDAO;
    private EventAdapter adapter;
    private MaterialCalendarView calendarView;
    private int lastSelectedDay;
    private int lastSelectedMonth;
    private int lastSelectedYear;
    private boolean hasSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        eventDAO = new EventDAO(this);
        calendarView = findViewById(R.id.calendarView);

        RecyclerView rvCalendarEvents = findViewById(R.id.rvCalendarEvents);
        rvCalendarEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(new ArrayList<>(), this::onDeleteEvent, this::onEditEvent);
        rvCalendarEvents.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        lastSelectedDay = today.get(Calendar.DAY_OF_MONTH);
        lastSelectedMonth = today.get(Calendar.MONTH) + 1;
        lastSelectedYear = today.get(Calendar.YEAR);
        hasSelectedDate = false;

        loadDecorators();
        adapter.updateList(new ArrayList<>());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            lastSelectedDay = date.getDay();
            lastSelectedMonth = date.getMonth();
            lastSelectedYear = date.getYear();
            hasSelectedDate = true;
            updateEventsForDate(lastSelectedDay, lastSelectedMonth, lastSelectedYear);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDecorators();
        if (hasSelectedDate) {
            updateEventsForDate(lastSelectedDay, lastSelectedMonth, lastSelectedYear);
        } else {
            adapter.updateList(new ArrayList<>());
        }
    }

    private void loadDecorators() {
        List<Event> events = eventDAO.getAllEvents();
        Set<CalendarDay> annualDays = new HashSet<>();
        Set<CalendarDay> onceDays = new HashSet<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (Event event : events) {
            int year = "ANNUAL".equals(event.getPeriodicity()) ? currentYear : event.getYear();
            int resolvedMonth = resolveEventMonth(year, event.getMonth(), event.getDay());
            if (!isValidDate(year, resolvedMonth, event.getDay())) {
                continue;
            }
            try {
                CalendarDay day = CalendarDay.from(year, resolvedMonth, event.getDay());
                if ("ANNUAL".equals(event.getPeriodicity())) {
                    annualDays.add(day);
                } else {
                    onceDays.add(day);
                }
            } catch (RuntimeException ignored) {
                // Fecha inválida en BD; se omite para no crashear el calendario.
            }
        }

        calendarView.removeDecorators();
        int annualColor = ContextCompat.getColor(this, R.color.colorEventAnnual);
        int onceColor = ContextCompat.getColor(this, R.color.colorEventOnce);
        calendarView.addDecorator(new EventDecorator(annualDays, annualColor));
        calendarView.addDecorator(new EventDecorator(onceDays, onceColor));
    }

    private int resolveEventMonth(int year, int month, int day) {
        if (!isValidDate(year, month, day) && isValidDate(year, month + 1, day)) {
            return month + 1;
        }
        return month;
    }

    private boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12 || day < 1) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        try {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.getTime();
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void updateEventsForDate(int day, int month, int year) {
        adapter.updateList(eventDAO.getEventsFromDate(day, month, year));
    }

    private void onDeleteEvent(int id) {
        eventDAO.deleteEvent(id);
        NotificationHelper.cancelNotification(this, id);
        loadDecorators();
        updateEventsForDate(lastSelectedDay, lastSelectedMonth, lastSelectedYear);
    }

    private void onEditEvent(Event event) {
        android.content.Intent intent = new android.content.Intent(this, AddEditEventActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }

    private static class EventDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;
        private final int color;

        EventDecorator(Set<CalendarDay> dates, int color) {
            this.dates = dates;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new DotSpan(8, color));
        }
    }

}
