package com.dev_vlad.car_v.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentLoginBinding
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import com.dev_vlad.car_v.view_models.auth.AuthViewModelFactory


class LoginFragment : Fragment() {
    companion object {
        private val TAG =  LoginFragment::class.java.simpleName
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((activity?.application as CarVApp).repository)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews(){
        binding.apply {
            val items = listOf("Material", "Design", "Components", "Android")
            val adapter = ArrayAdapter(requireContext(), R.layout.country_item, items)
            (countryPicker.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        authViewModel.userState.observe(
            viewLifecycleOwner,
            Observer {
                if (it.isNullOrEmpty() || it.size != 1){
                    //TODO user has signed out
                }else{
                    //TODO user is signed in
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}