package the.fellowship.uclass.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;

import the.fellowship.uclass.R;
import the.fellowship.uclass.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private LocalDateTime now;
    private LocalDateTime week;
    private FragmentCalendarBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        now = LocalDateTime.now();
        week = now;
        populateCalendar(week);


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
            binding.button1.setText(String.valueOf(date.plusDays(0 - offset).getDayOfMonth()));
            binding.button2.setText(String.valueOf(date.plusDays(1 - offset).getDayOfMonth()));
            binding.button3.setText(String.valueOf(date.plusDays(2 - offset).getDayOfMonth()));
            binding.button4.setText(String.valueOf(date.plusDays(3 - offset).getDayOfMonth()));
            binding.button5.setText(String.valueOf(date.plusDays(4 - offset).getDayOfMonth()));
            binding.button6.setText(String.valueOf(date.plusDays(5 - offset).getDayOfMonth()));
            binding.button7.setText(String.valueOf(date.plusDays(6 - offset).getDayOfMonth()));

            if (now.toString().equals(date.toString())) {
                MaterialButton button = null;
                switch (date.getDayOfWeek().getValue()) {
                    case 1:
                        button = binding.button1;
                        break;
                    case 2:
                        button = binding.button2;
                        break;
                    case 3:
                        button = binding.button3;
                        break;
                    case 4:
                        button = binding.button4;
                        break;
                    case 5:
                        button = binding.button5;
                        break;
                    case 6:
                        button = binding.button6;
                        break;
                    case 7:
                        button = binding.button7;
                        break;
                }

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