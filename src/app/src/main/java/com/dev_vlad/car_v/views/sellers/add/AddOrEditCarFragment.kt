package com.dev_vlad.car_v.views.sellers.add

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
import com.dev_vlad.car_v.databinding.FragmentAddCarBinding
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.util.myTxt
import com.dev_vlad.car_v.util.setTxt
import com.dev_vlad.car_v.util.showSnackBarToUser
import com.dev_vlad.car_v.view_models.sellers.add.AddOrEditCarViewModel
import com.dev_vlad.car_v.view_models.sellers.add.AddOrEditCarViewModelFactory

class AddOrEditCarFragment : Fragment() {

    companion object {
        private val TAG = AddOrEditCarFragment::class.java.simpleName
    }

    private var _binding: FragmentAddCarBinding? = null
    private val binding get() = _binding!!
    private val addCarVm: AddOrEditCarViewModel by viewModels {
        val carApp = (activity?.application as CarVApp)
        AddOrEditCarViewModelFactory(carApp.userRepo, carApp.carRepo)
    }

    private val args: AddOrEditCarFragmentArgs by navArgs()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddCarBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (args.CarId != null) {
            //we are editing a car
            addCarVm.initCarForEditing(args.CarId!!)
        }
        addCarVm.getCarBeingEdited().observe(
                viewLifecycleOwner,
                Observer { savedCar ->
                    if (savedCar != null) {
                        //car data has been saved
                        displayRestoredData(savedCar)
                        if (addCarVm.savingInProgress) {
                            //we just finished saving
                            addCarVm.savingInProgress = false
                            binding.apply {
                                loadingBar.isVisible = true
                                subtitle.text = getString(R.string.car_saved_redirecting)
                            }
                            val action = AddOrEditCarFragmentDirections.actionAddOrEditCarFragmentToAddCarImagesFragment(savedCar.carId)
                            findNavController().navigate(action)
                        }
                    }


                }
        )
    }


    private fun saveCar() {
        if (addCarVm.savingInProgress)
            return
        binding.apply {
            addCarVm.savingInProgress = true
            loadingBar.isVisible = true
            subtitle.text = getString(R.string.saving_please_wait)
            val bodyStyle = bodyStyle.myTxt(bodyStyle)
            val make = make.myTxt(make)
            val model = model.myTxt(model)
            val year = year.myTxt(year)
            val color = color.myTxt(color)
            val condition = condition.myTxt(condition)
            val mileage = mileage.myTxt(mileage)

            if (bodyStyle.isNullOrBlank()
                    || make.isNullOrBlank()
                    || model.isNullOrBlank()
                    || year.isNullOrBlank()
                    || color.isNullOrBlank()
                    || condition.isNullOrBlank()
                    || mileage.isNullOrBlank()) {

                addCarVm.savingInProgress = false
                loadingBar.isVisible = false
                subtitle.text = getString(R.string.add_car_subtitle)
                container.showSnackBarToUser(
                        msgResId = R.string.missing_required_car_info,
                        isErrorMsg = true,
                        actionMessage = R.string.got_it_txt
                )
                return
            }

            val extraDetailsTxt = extraDetails.myTxt(extraDetails) ?: ""
            val hasBeenInAccidentTxt = hasBeenInAccident.isChecked
            val hasFloodDamageTxt = hasFloodDamage.isChecked
            val hasFlameDamageTxt = hasFlameDamage.isChecked
            val hasIssuesOnDashboardTxt = hasIssuesOnDashboard.isChecked
            val hasBrokenOrReplacedOdometerTxt = hasBrokenOrReplacedOdometer.isChecked
            val noOfTiresToReplaceTxt = noOfTiresToReplace.myTxt(noOfTiresToReplace) ?: "0"
            val hasCustomizationsTxt = hasCustomizations.isChecked

            addCarVm.saveCarInfo(
                    bodyStyle,
                    make,
                    model,
                    year,
                    color,
                    condition,
                    mileage,
                    extraDetailsTxt,
                    hasBeenInAccidentTxt,
                    hasFloodDamageTxt,
                    hasFlameDamageTxt,
                    hasIssuesOnDashboardTxt,
                    hasBrokenOrReplacedOdometerTxt,
                    noOfTiresToReplaceTxt,
                    hasCustomizationsTxt,
            )

        }
    }

    private fun displayRestoredData(savedCar: CarEntity) {
        binding.apply {
            if (make.myTxt(make) == savedCar.make)
                return //no need to reset data
            bodyStyle.setTxt(bodyStyle, savedCar.body_style)
            make.setTxt(make, savedCar.make)
            model.setTxt(model, savedCar.model)
            year.setTxt(year, savedCar.year)
            color.setTxt(color, savedCar.color)
            condition.setTxt(condition, savedCar.condition)
            mileage.setTxt(mileage, savedCar.mileage)
            extraDetails.setTxt(extraDetails, savedCar.extra_details)
            hasBeenInAccident.isChecked = savedCar.has_been_in_accident
            hasFloodDamage.isChecked = savedCar.has_flood_damage
            hasFlameDamage.isChecked = savedCar.has_flame_damage
            hasIssuesOnDashboard.isChecked = savedCar.has_issues_on_dashboard
            hasBrokenOrReplacedOdometer.isChecked = savedCar.has_broken_or_replaced_odometer
            noOfTiresToReplace.setTxt(noOfTiresToReplace, savedCar.no_of_tires_to_replace.toString())
            hasCustomizations.isChecked = savedCar.has_customizations

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sellers_add_car, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_save) {
            saveCar()
            true
        } else item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }


}