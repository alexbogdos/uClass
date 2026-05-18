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
    private LocalDateTime current;
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

        // Populate the calendar with the current week's days
        now = LocalDateTime.now();
        week = now;
        current = now;
        populateCalendar(week);

        // Update current date text
        binding.dateText.setText(String.format("%s, %s %s", days[now.getDayOfWeek().getValue() - 1], now.getDayOfMonth(), months[now.getMonth().getValue() - 1]));

        assignments = new ArrayList<>();
        events = new ArrayList<>();
        adapter = new EventsAdapter(events);
        binding.recycler.setAdapter(adapter);

        UClass.eclass.getAssignments().observe(getViewLifecycleOwner(), this::populateAssignments);

        // Previous & Next week
        binding.buttonPrev.setOnClickListener(v -> {
            week = week.minusWeeks(1);
            populateCalendar(week);
            changeToDate(v, current.getDayOfWeek().getValue() - 1);
        });

        binding.buttonNext.setOnClickListener(v -> {
            week = week.plusWeeks(1);
            populateCalendar(week);
            changeToDate(v, current.getDayOfWeek().getValue() - 1);
        });

        // Select date
        buttons[now.getDayOfWeek().getValue() - 1].filled.setOnClickListener(v -> changeToDate(v, now.getDayOfWeek().getValue() - 1));
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
            int offset = current.getDayOfWeek().getValue() - 1;

            if (buttons[offset].filled.getVisibility() == View.GONE) {
                buttons[offset].plain.setVisibility(View.VISIBLE);
                buttons[offset].outlined.setVisibility(View.GONE);
            }

            current = week.plusDays(index + 1 - week.getDayOfWeek().getValue());

            offset = current.getDayOfWeek().getValue() - 1;
            binding.dateText.setText(String.format("%s, %s %s", days[offset], current.getDayOfMonth(), months[offset]));

            if (buttons[offset].filled.getVisibility() == View.GONE) {
                buttons[offset].plain.setVisibility(View.GONE);
                buttons[offset].outlined.setVisibility(View.VISIBLE);
                buttons[offset].outlined.setText(buttons[offset].plain.getText());
            }

            populateEvents();
        });
    }

    private void populateCalendar(LocalDateTime date) {
        if (getActivity() == null) {
            Log.e("Calendar", "Cannot use UI thread");
        }

        final int offset = date.getDayOfWeek().getValue() - 1;
        getActivity().runOnUiThread(() -> {
            binding.button1.plain.setText(String.valueOf(date.plusDays(0 - offset).getDayOfMonth()));
            binding.button2.plain.setText(String.valueOf(date.plusDays(1 - offset).getDayOfMonth()));
            binding.button3.plain.setText(String.valueOf(date.plusDays(2 - offset).getDayOfMonth()));
            binding.button4.plain.setText(String.valueOf(date.plusDays(3 - offset).getDayOfMonth()));
            binding.button5.plain.setText(String.valueOf(date.plusDays(4 - offset).getDayOfMonth()));
            binding.button6.plain.setText(String.valueOf(date.plusDays(5 - offset).getDayOfMonth()));
            binding.button7.plain.setText(String.valueOf(date.plusDays(6 - offset).getDayOfMonth()));

            // If week shown is week.now(), show date.now() as filled.
            // Else, show that button as outlined.
            final ButtonCalendarBinding current = buttons[offset];
            if (now.toString().equals(date.toString())) {
                current.plain.setVisibility(View.GONE);
                current.filled.setVisibility(View.VISIBLE);
                current.filled.setText(current.plain.getText());
            } else {
                current.plain.setVisibility(View.VISIBLE);
                current.filled.setVisibility(View.GONE);
            }
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
        });
    }

    private synchronized void populateEvents() {
        events.clear();
        events.addAll(assignments.stream().filter(assignment -> {
            final LocalDateTime date = assignment.getEnd();
            return current.getYear() == date.getYear() && current.getMonth() == date.getMonth() && current.getDayOfMonth() == date.getDayOfMonth();
        }).map(Event::new).collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
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