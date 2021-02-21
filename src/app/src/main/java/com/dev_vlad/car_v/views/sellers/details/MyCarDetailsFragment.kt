package com.dev_vlad.car_v.views.sellers.details

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentMyCarDetailsBinding
import com.dev_vlad.car_v.models.persistence.cars.CarEntity

import com.dev_vlad.car_v.view_models.sellers.details.MyCarDetailsViewModel
import com.dev_vlad.car_v.view_models.sellers.details.MyCarDetailsViewModelFactory
import java.util.*

class MyCarDetailsFragment : Fragment() {

    companion object {
        private val TAG = MyCarDetailsFragment::class.java.simpleName
    }

    private var _binding: FragmentMyCarDetailsBinding? = null
    private val binding get() = _binding!!
    private val myCarDetailsImgsVm: MyCarDetailsViewModel by viewModels {
        val carApp = (activity?.application as CarVApp)
        MyCarDetailsViewModelFactory(carApp.carRepo)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyCarDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    private val args: MyCarDetailsFragmentArgs by navArgs()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myCarDetailsImgsVm.fetchCarDetails(args.CarId)
        observeCarData()
    }

    private fun observeCarData() {
        myCarDetailsImgsVm.observeCar().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                displayDetails(it)
            }
        })
    }

    private fun displayDetails(car: CarEntity) {
        binding.apply {

            /* the details */
            val bodyStyleTxt = getString(R.string.car_body_style_hint) + "\n" + car.body_style.capitalize(Locale.getDefault())
            val makeTxt = getString(R.string.car_make_hint) + "\n" + car.make.capitalize(Locale.getDefault())
            val modelTxt = getString(R.string.car_model_hint) + "\n" + car.model.capitalize(Locale.getDefault())
            val yearTxt = getString(R.string.car_year_hint) + "\n" + car.year.capitalize(Locale.getDefault())
            val colorTxt = getString(R.string.car_color_hint) + "\n" + car.color.capitalize(Locale.getDefault())
            val conditionTxt = getString(R.string.add_car_condition_hint) + "\n" + car.condition.capitalize(Locale.getDefault())
            val mileageTxt = getString(R.string.car_mileage_hint) + "\n" + car.mileage.capitalize(Locale.getDefault())
            val extraDetailsTxt = getString(R.string.add_car_extra_details) + "\n" + car.extra_details.capitalize(Locale.getDefault())
            val replaceTiresTxt = getString(R.string.car_tyres_to_replace_num_hint) + " " + car.no_of_tires_to_replace.toString()
            bodyStyle.text = bodyStyleTxt
            make.text = makeTxt
            model.text = modelTxt
            year.text = yearTxt
            color.text = colorTxt
            condition.text = conditionTxt
            mileage.text = mileageTxt
            extraDetails.text = extraDetailsTxt
            hasBeenInAccident.isChecked = car.has_been_in_accident
            hasFloodDamage.isChecked = car.has_flood_damage
            hasFlameDamage.isChecked = car.has_flame_damage
            hasIssuesOnDashboard.isChecked = car.has_issues_on_dashboard
            hasBrokenOrReplacedOdometer.isChecked = car.has_broken_or_replaced_odometer
            noOfTiresToReplace.text = replaceTiresTxt
            hasCustomizations.isChecked = car.has_customizations

            /* the visibility */
            noOfTiresToReplace.isVisible = (car.no_of_tires_to_replace > 0)
            hasBeenInAccident.isVisible = car.has_been_in_accident
            hasFloodDamage.isVisible = car.has_flood_damage
            hasFlameDamage.isVisible = car.has_flame_damage
            hasIssuesOnDashboard.isVisible = car.has_issues_on_dashboard
            hasBrokenOrReplacedOdometer.isVisible = car.has_broken_or_replaced_odometer
            hasCustomizations.isVisible = car.has_customizations
            loadingBar.isVisible = false
        }
    }


    private fun editCar() {
        val carId = myCarDetailsImgsVm.getCarId()
        if (carId != null) {
            val action = MyCarDetailsFragmentDirections.actionMyCarDetailsFragmentToAddOrEditCarFragment(carId)
            findNavController().navigate(action)
        }
    }

    private fun deleteCar() {
        //TODO alert to confirm
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_car_details_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                editCar()
                true
            }
            R.id.action_delete -> {
                deleteCar()
                true
            }
            else -> item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
        }
    }

}