package com.dev_vlad.car_v.views.sellers.add

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
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
import com.dev_vlad.car_v.util.hideKeyBoard
import com.dev_vlad.car_v.util.myTxt
import com.dev_vlad.car_v.util.setTxt
import com.dev_vlad.car_v.util.showSnackBarToUser
import com.dev_vlad.car_v.view_models.sellers.add.AddOrEditCarViewModel
import com.dev_vlad.car_v.view_models.sellers.add.AddOrEditCarViewModelFactory
import com.dev_vlad.car_v.view_models.sellers.add.DATA_STATE


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
        addCarVm.getCarDataState().observe(viewLifecycleOwner,
            Observer {
                when (it) {
                    DATA_STATE.DELETED -> {
                        binding.apply {
                            loadingBar.isVisible = true
                            subtitle.text = getString(R.string.car_deleted_redirecting)
                        }
                        val action =
                            AddOrEditCarFragmentDirections.actionAddOrEditCarFragmentToSellersHomeFragment()
                        findNavController().navigate(action)

                    }
                    DATA_STATE.SAVED -> {
                        addCarVm.getCarId()?.let { carId ->
                            binding.apply {
                                loadingBar.isVisible = true
                                subtitle.text = getString(R.string.car_saved_redirecting)
                            }
                            val action =
                                AddOrEditCarFragmentDirections.actionAddOrEditCarFragmentToAddCarImagesFragment(
                                    carId
                                )
                            findNavController().navigate(action)
                            addCarVm.resetCarDataState()
                        }
                    }
                    DATA_STATE.ERROR -> {
                        binding.apply {
                            loadingBar.isVisible = false
                            subtitle.text = getString(R.string.saving_unknown_err)
                        }
                    }

                    else -> {
                    }
                }
            }
        )
        addCarVm.observeCarData().observe(
            viewLifecycleOwner,
            Observer { car ->
                car?.let {
                    //car data has been saved
                    displayRestoredData(it)
                }
            }
        )
    }


    private fun saveCar() {
        hideKeyBoard(requireContext(), binding.container)
        if (addCarVm.isOperationOnGoing())
            return
        binding.apply {
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
                || mileage.isNullOrBlank()
            ) {

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
            bodyStyle.setTxt(bodyStyle, savedCar.bodyStyle)
            make.setTxt(make, savedCar.make)
            model.setTxt(model, savedCar.model)
            year.setTxt(year, savedCar.year)
            color.setTxt(color, savedCar.color)
            condition.setTxt(condition, savedCar.condition)
            mileage.setTxt(mileage, savedCar.mileage)
            extraDetails.setTxt(extraDetails, savedCar.extraDetails)
            hasBeenInAccident.isChecked = savedCar.hasBeenInAccident
            hasFloodDamage.isChecked = savedCar.hasFloodDamage
            hasFlameDamage.isChecked = savedCar.hasFlameDamage
            hasIssuesOnDashboard.isChecked = savedCar.hasIssuesOnDashboard
            hasBrokenOrReplacedOdometer.isChecked = savedCar.hasBrokenOrReplacedOdometer
            noOfTiresToReplace.setTxt(
                noOfTiresToReplace,
                savedCar.noOfTiresToReplace.toString()
            )
            hasCustomizations.isChecked = savedCar.hasCustomizations

        }
    }


    private var alertDialog: AlertDialog? = null
    private fun deleteCar() {
        if (addCarVm.isOperationOnGoing())
            return

        if (alertDialog != null) {
            if (alertDialog!!.isShowing)
                return
            else
                alertDialog = null
        }

        alertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.delete_car_confirm)
                setPositiveButton(R.string.yes_txt,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        addCarVm.deleteCar()
                    })
                setNegativeButton(R.string.cancel_action_txt,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                    })
            }
            builder.create()
        }
        alertDialog?.show()
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sellers_add_car, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveCar()
                true
            }
            R.id.action_delete -> {
                deleteCar()
                true
            }
            else -> item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (alertDialog?.isShowing == true)
            alertDialog?.dismiss()
        alertDialog = null
        _binding = null
    }


}