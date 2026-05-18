package the.fellowship.uclass.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;

import the.fellowship.uclass.databinding.ButtonCalendarBinding;
import the.fellowship.uclass.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {
    private static final String[] days = {"Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο", "Κυριακή"};
    private static final String[] months = {"Ιανουαρίου", "Φεβρουαρίου", "Μαρτίου", "Απριλίου", "Μαΐου", "Ιουνίου", "Ιουλίου", "Αυγούστου", "Σεπτεμβρίου", "Οκτωβρίου", "Νοεμβρίου", "Δεκεμβρίου"};

    private LocalDateTime now;
    private LocalDateTime week;
    private ButtonCalendarBinding[] buttons;
    private FragmentCalendarBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        buttons = new ButtonCalendarBinding[]{binding.button1, binding.button2, binding.button3, binding.button4, binding.button5, binding.button6, binding.button7};

        now = LocalDateTime.now();
        week = now;
        populateCalendar(week);

        binding.dateText.setText(String.format("%s, %s %s", days[now.getDayOfWeek().getValue() - 1], now.getDayOfMonth(), months[now.getMonth().getValue() - 1]));

        binding.buttonPrev.setOnClickListener(v -> {
            week = week.minusWeeks(1);
            populateCalendar(week);
        });

        binding.buttonNext.setOnClickListener(v -> {
            week = week.plusWeeks(1);
            populateCalendar(week);
        });

        return binding.getRoot();
    }

    private void populateCalendar(LocalDateTime date) {
        if (getActivity() == null) {
            Log.e("Calendar", "Cannot use UI thread");
        }

        final int offset = date.getDayOfWeek().getValue() - 1;
        getActivity().runOnUiThread(() -> {
            binding.button1.outlined.setText(String.valueOf(date.plusDays(0 - offset).getDayOfMonth()));
            binding.button2.outlined.setText(String.valueOf(date.plusDays(1 - offset).getDayOfMonth()));
            binding.button3.outlined.setText(String.valueOf(date.plusDays(2 - offset).getDayOfMonth()));
            binding.button4.outlined.setText(String.valueOf(date.plusDays(3 - offset).getDayOfMonth()));
            binding.button5.outlined.setText(String.valueOf(date.plusDays(4 - offset).getDayOfMonth()));
            binding.button6.outlined.setText(String.valueOf(date.plusDays(5 - offset).getDayOfMonth()));
            binding.button7.outlined.setText(String.valueOf(date.plusDays(6 - offset).getDayOfMonth()));

            final ButtonCalendarBinding current = buttons[offset];
            if (now.toString().equals(date.toString())) {
                current.outlined.setVisibility(View.GONE);
                current.filled.setVisibility(View.VISIBLE);
                current.filled.setText(String.valueOf(date.plusDays(offset).getDayOfMonth()));
            } else {
                current.outlined.setVisibility(View.VISIBLE);
                current.filled.setVisibility(View.GONE);
            }
        });
    }

    private void populateEvents() {
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