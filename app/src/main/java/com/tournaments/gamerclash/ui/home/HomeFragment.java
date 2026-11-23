package com.tournaments.gamerclash.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tournaments.gamerclash.R;
import com.tournaments.gamerclash.databinding.FragmentHomeBinding;
import com.tournaments.gamerclash.refer_earn;

public class HomeFragment extends Fragment {


        private FragmentHomeBinding binding;
        LinearLayout refer_earn,lucky_draw,buyProduct;

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            HomeViewModel homeViewModel =
                    new ViewModelProvider(this).get(HomeViewModel.class);

            binding = FragmentHomeBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            // Initialize refer_earn from layout (agar aapka layout me id 'refer_earn' hai)
            refer_earn = root.findViewById(R.id.refer_earn);

            refer_earn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), refer_earn.class);
                    startActivity(intent);
                }
            });
            lucky_draw = root.findViewById(R.id.lucky_draw);
            lucky_draw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
                }
            });
            buyProduct = root.findViewById(R.id.buyProducts);
            buyProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
                }
            });



            return root;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }
    }
