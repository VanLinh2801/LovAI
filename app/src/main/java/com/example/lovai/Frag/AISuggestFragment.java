package com.example.lovai.Frag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lovai.Model.Forecast;
import com.example.lovai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AISuggestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AISuggestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AISuggestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AISuggestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AISuggestFragment newInstance(String param1, String param2) {
        AISuggestFragment fragment = new AISuggestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_i_suggest, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });



        LinearLayout weatherContainer = view.findViewById(R.id.weatherContainer);
        List<Forecast> forecastList = new ArrayList<>();
        forecastList.add(new Forecast("Monday", "23/09/2025", "Cloudy, 28°C", "Perfect day for a coffee date"));
        forecastList.add(new Forecast("Tuesday", "24/09/2025", "Sunny, 30°C", "Great for a picnic"));

        for (Forecast f : forecastList) {
            View card = inflater.inflate(R.layout.weather_card_item, weatherContainer, false);

            TextView dayText = card.findViewById(R.id.dayText);
            TextView dateText = card.findViewById(R.id.dateText);
            TextView weatherText = card.findViewById(R.id.weatherText);
            TextView suggestionText = card.findViewById(R.id.suggestionText);

            dayText.setText(f.getDay());
            dateText.setText(f.getDate());
            weatherText.setText(f.getWeather());
            suggestionText.setText(f.getSuggestion());

            weatherContainer.addView(card);
        }

        return view;
    }
}