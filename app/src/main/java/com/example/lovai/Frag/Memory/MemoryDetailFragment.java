package com.example.lovai.Frag.Memory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lovai.API.MemoryApi;
import com.example.lovai.API.MemoryMediaAdapter;
import com.example.lovai.API.RetrofitClient;
import com.example.lovai.DTO.Memory.MemoryMediaCreateRequest;
import com.example.lovai.DTO.Memory.MemoryMediaResponse;
import com.example.lovai.R;
import com.example.lovai.Ultis.FileUltis;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemoryDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemoryDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MemoryDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemoryDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemoryDetailFragment newInstance(String param1, String param2) {
        MemoryDetailFragment fragment = new MemoryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private MemoryMediaAdapter adapter;
    private List<MemoryMediaResponse> mediaList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMedia);
        adapter = new MemoryMediaAdapter(mediaList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),3));
        recyclerView.setAdapter(adapter);

        String memoryId=getArguments().getString("memoryId");
        String coupleId = getCurrentCoupleId();

        if (memoryId == null || coupleId == null) {
            Toast.makeText(getContext(), "Missing memoryId or coupleId", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadMedia(memoryId, coupleId);

        FloatingActionButton btnUpload = view.findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> openGallery());

        return view;
    }

    public void loadMedia(String memoryId, String coupleId){
        MemoryApi memoryApi = RetrofitClient.getMemoryApi(requireContext());
        memoryApi.getMemoryMedia(memoryId).enqueue(new Callback<List<MemoryMediaResponse>>() {
            @Override
            public void onResponse(Call<List<MemoryMediaResponse>> call, Response<List<MemoryMediaResponse>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    mediaList = response.body();
                    Toast.makeText(
                            getContext(),
                            "Loaded " + mediaList.size() + " media",
                            Toast.LENGTH_SHORT
                    ).show();
                    adapter.setMediaList(mediaList);
                }
            }

            @Override
            public void onFailure(Call<List<MemoryMediaResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load media", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentCoupleId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", requireContext().MODE_PRIVATE);
        return prefs.getString("coupleId", null);
    }

    private static final int PICK_IMAGE_REQUEST = 1001;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK); //chọn dữ liệu
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // goi callback cua he thong
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null){
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadMedia(imageUri);
            }
        }
    }

    private void uploadMedia(Uri imageUri) {
        try {
            String memoryId = getArguments().getString("memoryId");
            if (memoryId == null) {
                Toast.makeText(getContext(), "Thiếu memoryId", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert URI -> File
            String filePath = FileUltis.getPath(requireContext(), imageUri);
            if (filePath == null) {
                Toast.makeText(getContext(), "Không thể lấy đường dẫn file", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(filePath);

            // Tạo body parts
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            RequestBody memoryIdPart = RequestBody.create(MediaType.parse("text/plain"), memoryId);
            RequestBody folderPart = RequestBody.create(MediaType.parse("text/plain"), "memories");

            // Gọi API
            MemoryApi memoryApi = RetrofitClient.getMemoryApi(requireContext());
            memoryApi.uploadMedia(filePart, memoryIdPart, folderPart).enqueue(new Callback<MemoryMediaResponse>() {
                @Override
                public void onResponse(Call<MemoryMediaResponse> call, Response<MemoryMediaResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "Upload thành công!", Toast.LENGTH_SHORT).show();
                        loadMedia(memoryId, getCurrentCoupleId()); // reload list
                    } else {
                        Toast.makeText(getContext(), "Upload thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MemoryMediaResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Upload lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}


