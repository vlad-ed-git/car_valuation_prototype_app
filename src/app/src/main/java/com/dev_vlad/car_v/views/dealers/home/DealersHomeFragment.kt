package com.dev_vlad.car_v.views.dealers.home

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentDealersHomeBinding
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.VerticalSpacingItemDecorator
import com.dev_vlad.car_v.view_models.dealers.home.CarsWrapperForDealers
import com.dev_vlad.car_v.view_models.dealers.home.DealersHomeViewModel
import com.dev_vlad.car_v.view_models.dealers.home.DealersHomeViewModelFactory
import com.dev_vlad.car_v.views.adapters.dealers.CarsAdapter

class DealersHomeFragment : Fragment(), CarsAdapter.CarsActionsListener {

    companion object {
        private val TAG = DealersHomeFragment::class.java.simpleName
    }

    private var _binding: FragmentDealersHomeBinding? = null
    private val binding get() = _binding!!
    private val dealersHomeViewModel: DealersHomeViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        DealersHomeViewModelFactory(carVApp.userRepo, carVApp.carRepo, carVApp.offerRepo)
    }
    private val carsAdapter = CarsAdapter(this)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDealersHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dealersHomeViewModel.getCurrentUser().observe(
                viewLifecycleOwner, Observer {
            if (it != null) {
                observeCars()
            }

        }
        )
    }

    private fun observeCars() {
        dealersHomeViewModel.observeCarsState().observe(
                viewLifecycleOwner, Observer {
            binding.loadingBar.isVisible = false
            dealersHomeViewModel.isLoading = false
            if (it == null) {
                MyLogger.logThis(
                        TAG,
                        "observeCars()",
                        "Cars List Is Null"
                )
            } else {
                MyLogger.logThis(
                        TAG,
                        "observeCars()",
                        "Found ${it.size} cars"
                )
                carsAdapter.submitList(it)
            }
        }
        )
    }

    private fun initViews() {
        binding.apply {
            carsRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            carsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!carsRv.canScrollVertically(1)) {
                        //we have reached the bottom of the list
                        dealersHomeViewModel.fetchMoreCars(totalItemsInListNow = carsAdapter.itemCount)
                    }
                }
            })
            carsRv.adapter = carsAdapter
            carsRv.addItemDecoration(VerticalSpacingItemDecorator(30))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dealers_home_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //todo menu? -- about, profile, etc
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
        )
    }

    override fun onCarClicked(clickedCar: CarsWrapperForDealers) {
        val action =
                DealersHomeFragmentDirections.actionDealersHomeFragmentToCarDetailsFragment(clickedCar.car.carId)
        findNavController().navigate(action)
    }


}