package com.example.lovai.Frag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lovai.Model.Place;
import com.example.lovai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DiscoverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscoverFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DiscoverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DiscoverFragment newInstance(String param1, String param2) {
        DiscoverFragment fragment = new DiscoverFragment();
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
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        LinearLayout discoverContainer = view.findViewById(R.id.discoverContainer);
        EditText etSearch = view.findViewById(R.id.etSearch);



        List<Place> places = new ArrayList<>();
        places.add(new Place("Quán Cafe Lãng Mạn", "Không gian ấm cúng, nhạc nhẹ nhàng. Phù hợp hẹn hò.", R.drawable.cafe2));
        places.add(new Place("Nhà hàng Ý", "Pizza ngon, rượu vang chất lượng. Rất hợp cho dinner date.", R.drawable.cafe1));
        places.add(new Place("Quán Cafe Lãng Mạn", "Không gian ấm cúng, nhạc nhẹ nhàng. Phù hợp hẹn hò.", R.drawable.cafe3));

        for(Place p: places){
            View card = inflater.inflate(R.layout.discover_card_item, discoverContainer, false);

            ImageView img = card.findViewById(R.id.imgPlace);
            TextView title = card.findViewById(R.id.tvTitle);
            TextView description = card.findViewById(R.id.tvDescription);

            img.setImageResource(p.getImageResId());
            title.setText(p.getName());
            description.setText(p.getDescription());
            discoverContainer.addView(card);
        }


//        for (int i = 0; i < 10; i++) {
//            View card = inflater.inflate(R.layout.discover_card_item, discoverContainer, false);
//            discoverContainer.addView(card);
//        }


        return view;


    }
}