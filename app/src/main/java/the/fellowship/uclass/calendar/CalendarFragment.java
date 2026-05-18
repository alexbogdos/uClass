package the.fellowship.uclass.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.uclass.UClass;
import the.fellowship.uclass.databinding.ButtonCalendarBinding;
import the.fellowship.uclass.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {
    private static final String[] days = {"Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο", "Κυριακή"};
    private static final String[] months = {"Ιανουαρίου", "Φεβρουαρίου", "Μαρτίου", "Απριλίου", "Μαΐου", "Ιουνίου", "Ιουλίου", "Αυγούστου", "Σεπτεμβρίου", "Οκτωβρίου", "Νοεμβρίου", "Δεκεμβρίου"};

    private LocalDateTime now;
    private LocalDateTime week;
    private LocalDateTime selected;
    private List<Assignment> assignments;
    private List<Event> events;
    private EventsAdapter adapter;
    private ButtonCalendarBinding[] buttons;
    private FragmentCalendarBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        buttons = new ButtonCalendarBinding[]{binding.button1, binding.button2, binding.button3, binding.button4, binding.button5, binding.button6, binding.button7};

        assignments = new ArrayList<>();
        events = new ArrayList<>();
        adapter = new EventsAdapter(events);
        binding.recycler.setAdapter(adapter);

        // Populate the calendar with the current week's days
        now = LocalDateTime.now();
        week = now;
        selected = now;
        populateCalendar();

        // Update current date text
        binding.dateText.setText(String.format("%s, %s %s", days[now.getDayOfWeek().getValue() - 1], now.getDayOfMonth(), months[now.getMonthValue() - 1]));

        UClass.eclass.getAssignments().observe(getViewLifecycleOwner(), this::populateAssignments);

        // Previous & Next week
        binding.buttonPrev.setOnClickListener(v -> {
            week = week.minusWeeks(1);
            populateCalendar();
            changeToDate(v, selected.getDayOfWeek().getValue() - 1);
        });

        binding.buttonNext.setOnClickListener(v -> {
            week = week.plusWeeks(1);
            populateCalendar();
            changeToDate(v, selected.getDayOfWeek().getValue() - 1);
        });

        // Select date
        buttons[now.getDayOfWeek().getValue() - 1].outlined.setOnClickListener(v -> changeToDate(v, now.getDayOfWeek().getValue() - 1));
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].plain.setOnClickListener(v -> changeToDate(v, index));
        }

        return binding.getRoot();
    }

    private void changeToDate(View view, int index) {
        if (getActivity() == null) {
            Log.e("Calendar", "Cannot use UI thread");
        }

        getActivity().runOnUiThread(() -> {
            int offset = selected.getDayOfWeek().getValue() - 1;

            // Unselect previous button and restore outline if it was day.now()
            buttons[offset].filled.setVisibility(View.GONE);
            buttons[offset].plain.setVisibility(selected.equals(now) ? View.GONE : View.VISIBLE);
            buttons[offset].outlined.setVisibility(selected.equals(now) ? View.VISIBLE : View.GONE);
            if (selected.equals(now)) buttons[offset].outlined.setText(buttons[offset].plain.getText());

            selected = week.plusDays(index + 1 - week.getDayOfWeek().getValue());

            offset = selected.getDayOfWeek().getValue() - 1;
            binding.dateText.setText(String.format("%s, %s %s", days[offset], selected.getDayOfMonth(), months[selected.getMonthValue() - 1]));

            // Select current button
            buttons[offset].plain.setVisibility(View.GONE);
            buttons[offset].outlined.setVisibility(View.GONE);
            buttons[offset].filled.setVisibility(View.VISIBLE);
            buttons[offset].filled.setText(buttons[offset].plain.getText());

            populateEvents();
        });
    }

    private void populateCalendar() {
        if (getActivity() == null) {
            Log.e("Calendar", "Cannot use UI thread");
        }

        final int offset = week.getDayOfWeek().getValue() - 1;
        getActivity().runOnUiThread(() -> {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].plain.setText(String.valueOf(week.plusDays(i - offset).getDayOfMonth()));
            }

            // If week shown is week.now(), show date.now() as outlined.
            // Else, show that button as filled.
            final ButtonCalendarBinding current = buttons[offset];
            if (now.equals(week)) {
                current.plain.setVisibility(View.GONE);
                if (!now.equals(selected)) {
                    current.filled.setVisibility(View.GONE);
                    current.outlined.setVisibility(View.VISIBLE);
                    current.outlined.setText(current.plain.getText());
                } else {
                    current.outlined.setVisibility(View.GONE);
                    current.filled.setVisibility(View.VISIBLE);
                    current.filled.setText(current.plain.getText());
                }
            } else if (current.filled.getVisibility() == View.GONE) {
                current.plain.setVisibility(View.VISIBLE);
                current.outlined.setVisibility(View.GONE);
            }

            populateBadged();
        });
    }

    private void populateAssignments(List<Assignment> list) {
        if (getActivity() == null) {
            Log.e("Calendar", "Cannot use UI Thread");
            return;
        }

        getActivity().runOnUiThread(() -> {
            assignments.clear();
            assignments.addAll(list);
            populateEvents();
            populateBadged();
        });
    }

    private synchronized void populateBadged() {
        final int offset = week.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].icon.setVisibility(assignments.stream().anyMatch(assignment -> equals(week.plusDays(index - offset), assignment.getEnd())) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private synchronized void populateEvents() {
        events.clear();
        events.addAll(assignments.stream().filter(assignment -> equals(selected, assignment.getEnd())).map(Event::new).collect(Collectors.toList()));
        adapter.notifyItemRangeChanged(0, this.events.size());
    }

    static private boolean equals(LocalDateTime dateA, LocalDateTime dateB) {
        return dateA.getYear() == dateB.getYear() && dateA.getMonth() == dateB.getMonth() && dateA.getDayOfMonth() == dateB.getDayOfMonth();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}