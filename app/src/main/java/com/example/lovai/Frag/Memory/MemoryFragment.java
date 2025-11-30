package com.example.lovai.Frag.Memory;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lovai.API.MemoryAdapter;
import com.example.lovai.API.MemoryApi;
import com.example.lovai.API.RetrofitClient;
import com.example.lovai.DTO.Memory.MemoryResponse;
import com.example.lovai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MemoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemoryFragment newInstance(String param1, String param2) {
        MemoryFragment fragment = new MemoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private MemoryAdapter memoryAdapter;
    private FloatingActionButton fabAddMemory;
    private List<MemoryResponse> memoryList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_memory, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewMemories);
        fabAddMemory = view.findViewById(R.id.fabAddMemory);

        memoryAdapter = new MemoryAdapter(memoryList, new MemoryAdapter.OnMemoryClickListener() {
            @Override
            public void onMemoryClick(MemoryResponse memory) {
                openMemoryDetail(memory.getId());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(memoryAdapter);

        fabAddMemory.setOnClickListener(v -> openCreateMemoryFragment());

        loadMemories();

        return view;
    }

    private void loadMemories(){
        String coupleId = getCurrentCoupleId();
        if (coupleId == null) {
            Toast.makeText(getContext(), "Couple ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        MemoryApi memoryApi = RetrofitClient.getMemoryApi(requireContext());
        memoryApi.getMemoriesByCoupleId(coupleId).enqueue(new Callback<List<MemoryResponse>>() {
            @Override
            public void onResponse(Call<List<MemoryResponse>> call, Response<List<MemoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    memoryList = response.body();
                    memoryAdapter.setMemoryList(memoryList);
                } else {
                    Toast.makeText(getContext(), "Failed to load memories!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MemoryResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void openCreateMemoryFragment() {
        CreateMemoryFragment createMemoryFragment = new CreateMemoryFragment();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
        navController.navigate(R.id.createMemoryFragment);
    }

    private String getCurrentCoupleId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", requireContext().MODE_PRIVATE);
        return prefs.getString("coupleId", null);
    }

    private void openMemoryDetail(String memoryId) {
        Bundle args = new Bundle();
        args.putString("memoryId", memoryId);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
        navController.navigate(R.id.memoryDetailFragment, args);
    }
}