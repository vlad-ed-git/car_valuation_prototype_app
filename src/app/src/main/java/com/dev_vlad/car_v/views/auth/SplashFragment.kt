package com.dev_vlad.car_v.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentSplashBinding
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import com.dev_vlad.car_v.view_models.auth.AuthViewModelFactory


class SplashFragment : Fragment() {
    companion object {
        private val TAG =  SplashFragment::class.java.simpleName
    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((activity?.application as CarVApp).repository)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        resetViews()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        authViewModel.userState.observe(
            viewLifecycleOwner,
            Observer {
                if (it.isNullOrEmpty() || it.size != 1){
                    val action = SplashFragmentDirections.actionSplashFragmentToLoginFragment()
                    findNavController().navigate(action)
                }else{
                    val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
            }
        )
    }

    private fun resetViews(){
        binding.apply {
            retryBtn.isVisible = false
            Glide.with(requireContext())
                .load(R.drawable.yellow_vintage_car)
                .into(bgImg)
            loadingTxt.text = getString(R.string.splash_loading_txt)
            loadingBar.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}