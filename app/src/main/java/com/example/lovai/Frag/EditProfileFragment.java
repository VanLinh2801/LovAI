package com.example.lovai.Frag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.lovai.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtBirthday;
    private Button btnSave;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        edtName = view.findViewById(R.id.edtName);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtBirthday = view.findViewById(R.id.edtBirthday);
        btnSave = view.findViewById(R.id.btnSave);

        if (getArguments() != null) {
            edtName.setText(getArguments().getString("name", ""));
            edtEmail.setText(getArguments().getString("email", ""));
            edtPhone.setText(getArguments().getString("phone", ""));
            edtBirthday.setText(getArguments().getString("birthday", ""));
        }

        btnSave.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("name", edtName.getText().toString());
            result.putString("email", edtEmail.getText().toString());
            result.putString("phone", edtPhone.getText().toString());
            result.putString("birthday", edtBirthday.getText().toString());
            getParentFragmentManager().popBackStack();
        });


        return view;
    }
}