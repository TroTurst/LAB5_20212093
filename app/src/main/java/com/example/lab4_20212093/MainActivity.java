package com.example.lab4_20212093;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab4_20212093.adapter.EventAdapter;
import com.example.lab4_20212093.database.EventDAO;
import com.example.lab4_20212093.model.Event;
import com.example.lab4_20212093.notification.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EventDAO eventDAO;
    private EventAdapter adapter;
    private View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventDAO = new EventDAO(this);
        NotificationHelper.createChannels(this);

        RecyclerView rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(new ArrayList<>(), this::onDeleteEvent, this::onEditEvent);
        rvEvents.setAdapter(adapter);

        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> openAddEdit(null));

        findViewById(R.id.btnOpenCalendar)
            .setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        adapter.updateList(eventDAO.getAllEvents());
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void onDeleteEvent(int id) {
        eventDAO.deleteEvent(id);
        NotificationHelper.cancelNotification(this, id);
        loadEvents();
    }

    private void onEditEvent(Event event) {
        openAddEdit(event.getId());
    }

    private void openAddEdit(Integer eventId) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        if (eventId != null) {
            intent.putExtra("eventId", eventId);
        }
        startActivity(intent);
    }
}