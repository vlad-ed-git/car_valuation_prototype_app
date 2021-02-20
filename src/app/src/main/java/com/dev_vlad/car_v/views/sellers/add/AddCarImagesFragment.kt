package com.dev_vlad.car_v.views.sellers.add

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentAddCarBinding
import com.dev_vlad.car_v.databinding.FragmentAddCarImagesBinding
import com.dev_vlad.car_v.view_models.sellers.add.AddCarImagesViewModel
import com.dev_vlad.car_v.view_models.sellers.add.AddCarImagesViewModelFactory


class AddCarImagesFragment  : Fragment() {

    companion object {
        private val TAG = AddCarImagesFragment::class.java.simpleName
    }

    private var _binding: FragmentAddCarImagesBinding? = null
    private val binding get() = _binding!!
    private val addCarImgsVm :  AddCarImagesViewModel by viewModels {
        val carApp = (activity?.application as CarVApp)
        AddCarImagesViewModelFactory(carApp.carRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddCarImagesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initViews()
        return binding.root
    }

    private val args: AddCarImagesFragmentArgs by navArgs()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addCarImgsVm.setCarId(args.CarId)
    }

    private fun initViews(){
        binding.apply {
            laterBtn.setOnClickListener {
                confirmAddPhotosLater()
            }
        }
    }

    private var alertDialog : AlertDialog? = null
    private fun confirmAddPhotosLater(){
        if (alertDialog != null){
                if (alertDialog!!.isShowing)
                    return
                else
                    alertDialog = null
            }

         alertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.add_photos_later_confirm_title)
                setPositiveButton(R.string.ok_txt,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        goToSellersHome()
                    })
                setNegativeButton(R.string.cancel_adding_photos_later,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                    })
            }
            builder.create()
        }
        alertDialog?.show()
    }

    private fun goToSellersHome(){
        val action = AddCarImagesFragmentDirections.actionAddCarImagesFragmentToSellersHomeFragment()
        findNavController().navigate(action)
    }


    private fun saveCar(){
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (alertDialog?.isShowing == true)
            alertDialog?.dismiss()
        alertDialog = null
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sellers_add_car, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(item.itemId == R.id.action_save){
            saveCar()
            true
        } else item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }


}
