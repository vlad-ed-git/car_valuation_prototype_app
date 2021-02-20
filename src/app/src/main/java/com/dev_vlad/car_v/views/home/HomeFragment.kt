package com.dev_vlad.car_v.views.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.databinding.FragmentHomeBinding
import com.dev_vlad.car_v.util.InternetChecker
import com.dev_vlad.car_v.view_models.home.HomeViewModel
import com.dev_vlad.car_v.view_models.home.HomeViewModelFactory

class HomeFragment : Fragment() {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel : HomeViewModel by viewModels {
        HomeViewModelFactory((activity?.application as CarVApp).repository)
    }
    private lateinit var internetChecker : InternetChecker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun initViews(){

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}