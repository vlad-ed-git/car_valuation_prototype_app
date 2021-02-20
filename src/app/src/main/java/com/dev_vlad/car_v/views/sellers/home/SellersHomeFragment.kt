package com.dev_vlad.car_v.views.sellers.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentSellersHomeBinding
import com.dev_vlad.car_v.util.InternetChecker
import com.dev_vlad.car_v.view_models.sellers.home.SellersHomeViewModel
import com.dev_vlad.car_v.view_models.sellers.home.SellersHomeViewModelFactory

class SellersHomeFragment : Fragment() {

    companion object {
        private val TAG = SellersHomeFragment::class.java.simpleName
    }

    private var _binding: FragmentSellersHomeBinding? = null
    private val binding get() = _binding!!
    private val sellersHomeViewModel : SellersHomeViewModel by viewModels {
        SellersHomeViewModelFactory((activity?.application as CarVApp).userRepo)
    }
    private lateinit var internetChecker : InternetChecker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSellersHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun initViews(){
        binding.apply {

            addCarFab.setOnClickListener {
                    val action = SellersHomeFragmentDirections.actionSellersHomeFragmentToAddCarFragment()
                   findNavController().navigate(action)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sellers_home_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //todo item.itemId
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }


}