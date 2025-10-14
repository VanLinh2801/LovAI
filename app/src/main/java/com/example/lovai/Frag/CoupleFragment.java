package com.example.lovai.Frag;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lovai.API.CoupleApi;
import com.example.lovai.API.RetrofitClient;
import com.example.lovai.DTO.CoupleResponse;
import com.example.lovai.Frag.Memory.MemoryFragment;
import com.example.lovai.R;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoupleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoupleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CoupleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CoupleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoupleFragment newInstance(String param1, String param2) {
        CoupleFragment fragment = new CoupleFragment();
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

    private CardView cardcouple;
    private TextView tvCoupleName, tvAnniversaryDate;
    private ImageView imgCover;
    private MaterialButton btnViewMemory;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_couple,container,false);
        tvCoupleName = view.findViewById(R.id.tvCoupleName);
        tvAnniversaryDate = view.findViewById(R.id.tvAnniversaryDate);
        imgCover = view.findViewById(R.id.imgCover);
        btnViewMemory = view.findViewById(R.id.button2);
        btnViewMemory.setOnClickListener(v->{
            MemoryFragment viewMemoryFragment = new MemoryFragment();
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
            navController.navigate(R.id.memoryFragment);
        });

        loadMyCouple();


        cardcouple = view.findViewById(R.id.cardCouple);
        cardcouple.setOnClickListener(v->{
            EditCoupleFragment editCoupleFragment = new EditCoupleFragment();
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
            navController.navigate(R.id.editCoupleFragment);
        });

        return view;

    }

    private void loadMyCouple(){
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        Toast.makeText(requireContext(), "Hello " + userId, Toast.LENGTH_SHORT).show();
        CoupleApi coupleApi = RetrofitClient.getCoupleApi(requireContext());
        coupleApi.getMyCouple(userId).enqueue(new Callback<CoupleResponse>() {
            @Override
            public void onResponse(Call<CoupleResponse> call, Response<CoupleResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                    CoupleResponse coupleResponse = response.body();
                    displayCoupleInfo(coupleResponse);
                    SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("coupleId", coupleResponse.getId());
                    editor.apply();
                }
                else{
                    Toast.makeText(requireContext(), "Couple not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CoupleResponse> call, Throwable t) {

            }
        });

    }

    private void displayCoupleInfo(CoupleResponse coupleResponse){
        if(coupleResponse!=null){
            tvCoupleName.setText(coupleResponse.getTitle());
            tvAnniversaryDate.setText("Anniversary: " + coupleResponse.getAnniversaryDate());
        }
    }

}