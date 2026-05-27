package com.example.lab4_20212093.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab4_20212093.R;
import com.example.lab4_20212093.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnDeleteListener {
        void onDelete(int id);
    }

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    private List<Event> events;
    private final OnDeleteListener deleteListener;
    private final OnItemClickListener clickListener;

    public EventAdapter(List<Event> events, OnDeleteListener deleteListener,
                        OnItemClickListener clickListener) {
        this.events = events;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvName.setText(event.getName());
        holder.tvDate.setText(formatDate(event));
        holder.tvTimeLeft.setText(formatTimeLeft(event, holder.itemView));

        int colorRes = "ANNUAL".equals(event.getPeriodicity())
            ? R.color.colorEventAnnual
            : R.color.colorEventOnce;
        holder.tvName.setTextColor(
            androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(event.getId()));
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(event));
    }

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    public void updateList(List<Event> newList) {
        this.events = newList;
        notifyDataSetChanged();
    }

    private String formatDate(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, event.getYear());
        calendar.set(Calendar.MONTH, event.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, event.getDay());
        if (event.getHour() >= 0 && event.getMinute() >= 0) {
            calendar.set(Calendar.HOUR_OF_DAY, event.getHour());
            calendar.set(Calendar.MINUTE, event.getMinute());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return formatter.format(calendar.getTime());
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatter.format(calendar.getTime());
    }

    private String formatTimeLeft(Event event, View view) {
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.set(Calendar.YEAR, event.getYear());
        eventCalendar.set(Calendar.MONTH, event.getMonth() - 1);
        eventCalendar.set(Calendar.DAY_OF_MONTH, event.getDay());
        int hour = event.getHour() >= 0 ? event.getHour() : 9;
        int minute = event.getMinute() >= 0 ? event.getMinute() : 0;
        eventCalendar.set(Calendar.HOUR_OF_DAY, hour);
        eventCalendar.set(Calendar.MINUTE, minute);
        eventCalendar.set(Calendar.SECOND, 0);
        eventCalendar.set(Calendar.MILLISECOND, 0);

        if ("ANNUAL".equals(event.getPeriodicity())) {
            Calendar now = Calendar.getInstance();
            if (eventCalendar.before(now)) {
                eventCalendar.add(Calendar.YEAR, 1);
            }
        }

        long diffMillis = eventCalendar.getTimeInMillis() - System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
        if (days < 0) {
            return view.getContext().getString(R.string.time_left_past);
        }
        if (days == 0) {
            return view.getContext().getString(R.string.time_left_today);
        }
        return "Faltan " + days + " dias";
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDate;
        final TextView tvTimeLeft;
        final ImageButton btnDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
