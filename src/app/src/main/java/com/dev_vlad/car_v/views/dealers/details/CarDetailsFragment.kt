package com.dev_vlad.car_v.views.dealers.details

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentCarDetailsBinding
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.util.VerticalSpacingItemDecorator
import com.dev_vlad.car_v.view_models.dealers.details.CarDetailsViewModel
import com.dev_vlad.car_v.view_models.dealers.details.CarDetailsViewModelFactory
import com.dev_vlad.car_v.views.adapters.sellers.MyCarImagesAdapter
import java.util.*


class CarDetailsFragment : Fragment() {

    companion object {
        private val TAG = CarDetailsFragment::class.java.simpleName
    }

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!
    private val carDetailsImagesVM: CarDetailsViewModel by viewModels {
        val carApp = (activity?.application as CarVApp)
        CarDetailsViewModelFactory(carApp.carRepo)
    }
    private val carImagesAdapter = MyCarImagesAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCarDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding.imagesRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.imagesRv.adapter = carImagesAdapter
        binding.imagesRv.addItemDecoration(VerticalSpacingItemDecorator(30))
        return binding.root
    }

    private val args: CarDetailsFragmentArgs by navArgs()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        carDetailsImagesVM.fetchCarDetails(args.CarId)
        observeCarData()
    }

    private fun observeCarData() {
        carDetailsImagesVM.observeCar().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                displayDetails(it)
            }
        })
    }

    private fun displayDetails(car: CarEntity) {
        binding.apply {
            carImagesAdapter.setNewImages(car.imageUrls)
            swipeIndicator.isVisible = car.imageUrls.size > 1
            /* the details */
            val bodyStyleTxt =
                getString(R.string.car_body_style_hint) + "\n" + car.bodyStyle.capitalize(Locale.getDefault())
            val makeTxt =
                getString(R.string.car_make_hint) + "\n" + car.make.capitalize(Locale.getDefault())
            val modelTxt =
                getString(R.string.car_model_hint) + "\n" + car.model.capitalize(Locale.getDefault())
            val yearTxt =
                getString(R.string.car_year_hint) + "\n" + car.year.capitalize(Locale.getDefault())
            val colorTxt =
                getString(R.string.car_color_hint) + "\n" + car.color.capitalize(Locale.getDefault())
            val conditionTxt =
                getString(R.string.add_car_condition_hint) + "\n" + car.condition.capitalize(Locale.getDefault())
            val mileageTxt =
                getString(R.string.car_mileage_hint) + "\n" + car.mileage.capitalize(Locale.getDefault())
            val extraDetailsTxt =
                getString(R.string.add_car_extra_details) + "\n" + car.extraDetails.capitalize(
                    Locale.getDefault()
                )
            val replaceTiresTxt =
                getString(R.string.car_tyres_to_replace_num_hint) + " " + car.noOfTiresToReplace.toString()
            bodyStyle.text = bodyStyleTxt
            make.text = makeTxt
            model.text = modelTxt
            year.text = yearTxt
            color.text = colorTxt
            condition.text = conditionTxt
            mileage.text = mileageTxt
            extraDetails.text = extraDetailsTxt
            hasBeenInAccident.isChecked = car.hasBeenInAccident
            hasFloodDamage.isChecked = car.hasFloodDamage
            hasFlameDamage.isChecked = car.hasFlameDamage
            hasIssuesOnDashboard.isChecked = car.hasIssuesOnDashboard
            hasBrokenOrReplacedOdometer.isChecked = car.hasBrokenOrReplacedOdometer
            noOfTiresToReplace.text = replaceTiresTxt
            hasCustomizations.isChecked = car.hasCustomizations

            /* the visibility */
            noOfTiresToReplace.isVisible = (car.noOfTiresToReplace > 0)
            hasBeenInAccident.isVisible = car.hasBeenInAccident
            hasFloodDamage.isVisible = car.hasFloodDamage
            hasFlameDamage.isVisible = car.hasFlameDamage
            hasIssuesOnDashboard.isVisible = car.hasIssuesOnDashboard
            hasBrokenOrReplacedOdometer.isVisible = car.hasBrokenOrReplacedOdometer
            hasCustomizations.isVisible = car.hasCustomizations
            loadingBar.isVisible = false
        }
    }


    private fun makeInitialOffer(){
        //todo
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dealer_car_details_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_msg_seller -> {
                makeInitialOffer()
                true
            }
            else -> item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}