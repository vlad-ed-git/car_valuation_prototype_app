package com.dev_vlad.car_v.views.sellers.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentSellersHomeBinding
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.VerticalSpacingItemDecorator
import com.dev_vlad.car_v.view_models.sellers.home.SellersHomeViewModel
import com.dev_vlad.car_v.view_models.sellers.home.SellersHomeViewModelFactory
import com.dev_vlad.car_v.views.adapters.sellers.MyCarsAdapter

class SellersHomeFragment : Fragment(), MyCarsAdapter.MyCarsActionsListener {

    companion object {
        private val TAG = SellersHomeFragment::class.java.simpleName
    }

    private var _binding: FragmentSellersHomeBinding? = null
    private val binding get() = _binding!!
    private val sellersHomeViewModel: SellersHomeViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        SellersHomeViewModelFactory(carVApp.userRepo, carVApp.carRepo)
    }
    private val myCarsAdapter = MyCarsAdapter(this)
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
        sellersHomeViewModel.getCurrentUser().observe(
                viewLifecycleOwner, Observer {
            if (it != null) {
                observeMyCars()
            }

        }
        )
    }

    private fun observeMyCars() {
        sellersHomeViewModel.observeMyCarsState().observe(
                viewLifecycleOwner, Observer {
            if (it == null) {
                MyLogger.logThis(
                        TAG,
                        "observeMyCars()",
                        "My Cars List Is Null"
                )
            } else {
                MyLogger.logThis(
                        TAG,
                        "observeMyCars()",
                        "Found ${it.size} cars"
                )
                myCarsAdapter.submitList(it)
            }
        }
        )
    }

    private fun initViews() {
        binding.apply {

            addCarFab.setOnClickListener {
                val action = SellersHomeFragmentDirections.actionSellersHomeFragmentToAddOrEditCarFragment(null)
                findNavController().navigate(action)
            }
            myCars.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            myCars.adapter = myCarsAdapter
            myCars.addItemDecoration(VerticalSpacingItemDecorator(30))
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
        //todo menu? -- about, profile, etc
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onCarClicked(clickedCar: CarEntity) {
        MyLogger.logThis(TAG, "onCarClicked()", "car ${clickedCar.carId}")
        val action = SellersHomeFragmentDirections.actionSellersHomeFragmentToMyCarDetailsFragment(clickedCar.carId)
        findNavController().navigate(action)
    }


}