package com.example.lovai.Frag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.lovai.Model.User;
import com.example.lovai.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private TextView txtName, txtEmail, txtPhone, txtBirthday;
    private Button btnEditProfile;
    private User currentUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtBirthday = view.findViewById(R.id.txtBirthday);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        currentUser = new User("Nguyễn Văn A", "example@gmail.com", "0123456789", "01/01/2000");
        loadUserData();

        btnEditProfile.setOnClickListener(v->{
            EditProfileFragment editFragment = new EditProfileFragment();

            Bundle args = new Bundle();
            args.putString("name", currentUser.getName());
            args.putString("email", currentUser.getEmail());
            args.putString("phone", currentUser.getPhone());
            args.putString("birthday", currentUser.getBirthday());

            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
            navController.navigate(R.id.editProfileFragment, args);
        });

        return view;
    }

    private void loadUserData() {
        txtName.setText(currentUser.getName());
        txtEmail.setText(currentUser.getEmail());
        txtPhone.setText(currentUser.getPhone());
        txtBirthday.setText(currentUser.getBirthday());
    }
}