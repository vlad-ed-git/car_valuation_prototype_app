package com.dev_vlad.car_v.views.sellers.add

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AlertDialog
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
import com.dev_vlad.car_v.databinding.FragmentAddCarImagesBinding
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.VerticalSpacingItemDecorator
import com.dev_vlad.car_v.util.showSnackBarToUser
import com.dev_vlad.car_v.view_models.sellers.add.AddCarImagesViewModel
import com.dev_vlad.car_v.view_models.sellers.add.AddCarImagesViewModelFactory
import com.dev_vlad.car_v.views.adapters.sellers.MyCarImagesAdapter
import com.dev_vlad.car_v.util.REQUEST_IMAGE_IN_GALLERY


class AddCarImagesFragment : Fragment(), MyCarImagesAdapter.ImageActionsListeners {

    companion object {
        private val TAG = AddCarImagesFragment::class.java.simpleName
    }

    private var _binding: FragmentAddCarImagesBinding? = null
    private val binding get() = _binding!!
    private val addCarImgsVm: AddCarImagesViewModel by viewModels {
        val carApp = (activity?.application as CarVApp)
        AddCarImagesViewModelFactory(carApp.carRepo)
    }

    private val photosAdapter = MyCarImagesAdapter(this)
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
        addCarImgsVm.loadCarDataById(args.CarId)

        addCarImgsVm.observeCarImages().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            MyLogger.logThis(
                TAG,
                ".getCarImages().observe()",
                "has ${it?.size} images"
            )
            if (it != null)
                photosAdapter.setNewImages(it)
        })

        addCarImgsVm.getSavingState().observe(viewLifecycleOwner, Observer {
            when (it) {
                AddCarImagesViewModel.SavingState.SAVING -> {
                    binding.subtitle.setText(R.string.saving_please_wait)
                    binding.loadingBar.isVisible = true
                }
                AddCarImagesViewModel.SavingState.SAVED -> {
                    binding.loadingBar.isVisible = false
                    goToSellersHome()
                }
                AddCarImagesViewModel.SavingState.ERROR -> {
                    binding.apply {
                        container.showSnackBarToUser(
                            msgResId = addCarImgsVm.errMsgRes ?: R.string.saving_unknown_err,
                            isErrorMsg = true,
                            actionMessage = R.string.ok_txt
                        )
                        subtitle.setText(R.string.please_add_photos)
                        loadingBar.isVisible = false
                    }
                }
                else -> {
                }
            }
        })

    }

    private fun initViews() {
        binding.apply {
            photosRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            photosRv.adapter = photosAdapter
            photosRv.addItemDecoration(VerticalSpacingItemDecorator(30))
            laterBtn.setOnClickListener {
                confirmAddPhotosLater()
            }
            addPicBtn.setOnClickListener {
                addPhoto()
            }
        }
    }

    private fun addPhoto() {
        //todo take photo feature
        addPhotoFromGallery()
    }

    private fun addPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent,REQUEST_IMAGE_IN_GALLERY)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_IN_GALLERY && resultCode == RESULT_OK) {
            MyLogger.logThis(
                TAG,
                " addPhotoFromGallery() -> onActivityResult()", "returned data ${data?.data}"
            )
            data?.data?.let { imgUri ->
                addCarImgsVm.addImage(imgUri.toString())
            }
        }

    }


    private var alertDialog: AlertDialog? = null
    private fun confirmAddPhotosLater() {
        if (alertDialog != null) {
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

    private fun goToSellersHome() {
        val action =
            AddCarImagesFragmentDirections.actionAddCarImagesFragmentToSellersHomeFragment()
        findNavController().navigate(action)
    }


    private fun saveCarImages() {
        addCarImgsVm.save()
    }

    private fun deleteSelectedImages() {
        val imgsSelected = addCarImgsVm.getSelectedImagesNum()
        val title: Int
        val positiveMessage: Int
        if (imgsSelected == 0) {
            title = R.string.select_imgs_to_delete
            positiveMessage = R.string.got_it_txt
        } else {
            title = R.string.delete_selected_images
            positiveMessage = R.string.yes_txt
        }

        if (alertDialog != null) {
            if (alertDialog!!.isShowing)
                return
            else
                alertDialog = null
        }

        alertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(title)
                setPositiveButton(positiveMessage,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        addCarImgsVm.deleteSelectedImages()
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
                saveCarImages()
                true
            }
            R.id.action_delete -> {
                deleteSelectedImages()
                true
            }
            else -> item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
            )
        }
    }


    /************ DELETE OR UN-DELETE IMAGES *****************/
    override fun onSelectImage(imgUrl: String) {
        addCarImgsVm.selectCarImg(imgUrl)
    }

    override fun onUnSelectImage(imgUrl: String) {
        addCarImgsVm.unSelectCarImg(imgUrl)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (alertDialog?.isShowing == true)
            alertDialog?.dismiss()
        alertDialog = null
        _binding = null
    }


}
