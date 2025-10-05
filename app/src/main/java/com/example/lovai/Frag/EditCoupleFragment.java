package com.example.lovai.Frag;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lovai.API.CoupleApi;
import com.example.lovai.API.RetrofitClient;
import com.example.lovai.DTO.CoupleResponse;
import com.example.lovai.DTO.CreateCoupleWithPartnerRequest;
import com.example.lovai.DTO.PartnerRequest;
import com.example.lovai.DTO.UpdateCoupleRequest;
import com.example.lovai.R;
import com.example.lovai.SignUp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCoupleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCoupleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditCoupleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditCoupleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCoupleFragment newInstance(String param1, String param2) {
        EditCoupleFragment fragment = new EditCoupleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextInputEditText edtCoupleName, edtAnniversaryDate, edtUserId,
            edtPartnerName, edtPartnerDob;
    private MaterialButton btnSaveCouple;
    private Spinner genderSpinner;
    private CoupleApi coupleApi;
    private CoupleResponse currentCouple;
    private String userId;



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
        View view = inflater.inflate(R.layout.fragment_edit_couple, container, false);
        edtCoupleName = view.findViewById(R.id.edtCoupleName);
        edtAnniversaryDate = view.findViewById(R.id.edtAnniversaryDate);
        AnniversaryDate();
        edtUserId = view.findViewById(R.id.edtUserId);
        edtPartnerName = view.findViewById(R.id.edtPartnerName);
        genderSpinner = view.findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(requireContext(),R.array.gender_options,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        edtPartnerDob = view.findViewById(R.id.edtPartnerDob);
        PartnerDob();
        btnSaveCouple = view.findViewById(R.id.btnSaveCouple);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs",MODE_PRIVATE);
        userId = prefs.getString("userId", null);


        if (userId != null) {
            Toast.makeText(requireContext(), "Hello " + userId, Toast.LENGTH_SHORT).show();
        }

        coupleApi = RetrofitClient.getCoupleApi(requireContext());
        edtUserId.setText(userId);



        loadCouple();

        btnSaveCouple.setOnClickListener(v->saveCouple());

        return view;
    }

    private void loadCouple(){
        coupleApi.getMyCouple(userId).enqueue(new Callback<CoupleResponse>() {
            @Override
            public void onResponse(Call<CoupleResponse> call, Response<CoupleResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                    currentCouple = response.body();
                    fillForm(currentCouple);
                }else{
//                    try {
//                        Log.e("LOAD_COUPLE", "Code: " + response.code());
//                        Log.e("LOAD_COUPLE", "Message: " + response.message());
//                        if (response.errorBody() != null) {
//                            Log.e("LOAD_COUPLE", "ErrorBody: " + response.errorBody().string());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    Toast.makeText(requireContext(), "No couple yet, create a couple: " + response.message(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(requireContext(), "No couple yet, create a couple: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CoupleResponse> call, Throwable t) {
                Toast.makeText(requireContext(),"Error loading couple"+ t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillForm(CoupleResponse coupleResponse){
        edtCoupleName.setText(coupleResponse.getTitle());
        edtAnniversaryDate.setText(coupleResponse.getAnniversaryDate());
        if(coupleResponse.getPartner()!=null){
            edtPartnerName.setText(coupleResponse.getPartner().getName());
            edtPartnerDob.setText(coupleResponse.getPartner().getDateOfBirth());
            String gender = coupleResponse.getPartner().getGender();
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) genderSpinner.getAdapter();
            int position = adapter.getPosition(gender);
            if(position>=0){
                genderSpinner.setSelection(position);
            }
        }
    }

    private void saveCouple(){
        String title = edtCoupleName.getText().toString().trim();
        String anniversaryDate = edtAnniversaryDate.getText().toString().trim();
        String partnerName = edtPartnerName.getText().toString().trim();
        String partnerGender = genderSpinner.getSelectedItem().toString().trim();
        String partnerDob = edtPartnerDob.getText().toString().trim();

        PartnerRequest partnerRequest = new PartnerRequest(partnerName, partnerGender, partnerDob);

        if(currentCouple!=null && currentCouple.getId()!=null) {
            //update
            UpdateCoupleRequest req = new UpdateCoupleRequest(title, anniversaryDate, partnerRequest);
            coupleApi.updateCouple(currentCouple.getId(), req).enqueue(new Callback<CoupleResponse>() {
                @Override
                public void onResponse(Call<CoupleResponse> call, Response<CoupleResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Update successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Update failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CoupleResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            //create
            CreateCoupleWithPartnerRequest req = new CreateCoupleWithPartnerRequest(title, anniversaryDate, userId, partnerRequest);
            coupleApi.createCoupleWithPartner(req).enqueue(new Callback<CoupleResponse>() {
                @Override
                public void onResponse(Call<CoupleResponse> call, Response<CoupleResponse> response) {
                    if(response.isSuccessful()){
                        Toast.makeText(requireContext(), "Create success", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }else{
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR", "Create failure: " + errorBody);
                            Log.e("API_ERROR", "Code: " + response.code() + ", Message: " + response.message());
                            Toast.makeText(requireContext(), "Create failure, check log for details", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Error reading errorBody", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<CoupleResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void AnniversaryDate(){
        edtAnniversaryDate.setOnClickListener(v->{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        edtAnniversaryDate.setText(dob);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    public void PartnerDob(){
        edtPartnerDob.setOnClickListener(v->{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        edtPartnerDob.setText(dob);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }


}