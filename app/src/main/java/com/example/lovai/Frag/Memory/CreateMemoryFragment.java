package com.example.lovai.Frag.Memory;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lovai.API.MemoryApi;
import com.example.lovai.API.RetrofitClient;
import com.example.lovai.DTO.Memory.MemoryCreateRequest;
import com.example.lovai.DTO.Memory.MemoryResponse;
import com.example.lovai.R;
import com.example.lovai.SignUp;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateMemoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateMemoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateMemoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateMemoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateMemoryFragment newInstance(String param1, String param2) {
        CreateMemoryFragment fragment = new CreateMemoryFragment();
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

    private EditText edtTitle, edtDescription, edtHappenedAt;
    private AppCompatButton btnCreateMemory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_memory, container, false);
        edtTitle = view.findViewById(R.id.etMemoryTitle);
        edtDescription = view.findViewById(R.id.etMemoryDescription);
        edtHappenedAt = view.findViewById(R.id.etHappenedAt);
        DateHappendAt();
        btnCreateMemory = view.findViewById(R.id.btnCreateMemory);

        btnCreateMemory.setOnClickListener(v->createMemory());



        return view;

    }

    private void createMemory(){
        String title = edtTitle.getText().toString();
        String description = edtDescription.getText().toString();
        String happendedAt = edtHappenedAt.getText().toString();
        if(title.isEmpty()){
            edtTitle.setError("Title is required");
            return;
        }
        String coupleId = getCurrentCoupleId();
        if (coupleId == null) {
            Toast.makeText(requireContext(), "Missing couple, please create couple first!", Toast.LENGTH_SHORT).show();
            return;
        }

        MemoryCreateRequest request = new MemoryCreateRequest( coupleId, title, description, happendedAt);
        MemoryApi memoryApi = RetrofitClient.getMemoryApi(requireContext());
        memoryApi.createMemory(request).enqueue(new Callback<MemoryResponse>() {
            @Override
                public void onResponse(Call<MemoryResponse> call, Response<MemoryResponse> response) {
                    if(response.isSuccessful() && response.body()!=null){
                        Toast.makeText(getContext(), "Memory created!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    else{
                        Toast.makeText(getContext(), "Memory creation failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MemoryResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Create memory failed!", Toast.LENGTH_SHORT).show();
                }
        });

    }
    private String getCurrentCoupleId(){
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", requireContext().MODE_PRIVATE);
        return prefs.getString("coupleId", null);
    }

    public void DateHappendAt(){
        edtHappenedAt.setOnClickListener(v->{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        edtHappenedAt.setText(dob);
                    },
                    year, month, day // ngày hiện tại
            );
            datePickerDialog.show();
        });
    }
}