package com.dev_vlad.car_v.view_models.sellers.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.util.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCarImagesViewModel(private val carRepo: CarRepo) : ViewModel() {
    companion object {
        private val TAG = AddCarImagesViewModel::class.java.simpleName
    }

    private val carImages = MutableLiveData<List<String>>()
    private var carData: CarEntity? = null
    fun observeCarImages(): LiveData<List<String>> = carImages
    fun loadCarDataById(carId: String) {
        MyLogger.logThis(
            TAG, "setCar()", "carId $carId"
        )
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedCar = carRepo.getNonObservableCarDetailsById(carId)
            withContext(Dispatchers.Main) {
                carData = fetchedCar
                carData?.let {
                    initCarImages(it.imageUrls)
                }
            }
        }
    }

    private fun initCarImages(ogCarImages: List<String>) {
        //TODO FIX the issue with image_urls array-list never being null
        val properImages = ogCarImages.filter { imgUrl -> imgUrl.length > 4 }
        MyLogger.logThis(
            TAG,
            "initCarImages()",
            "proper images in it -- ${properImages.size}"
        )
        carImages.value = properImages
    }


    fun addImage(newImgUri: String) {
        val imgTmp = ArrayList<String>()
        val existingImages = carImages.value
        if (!existingImages.isNullOrEmpty())
            imgTmp.addAll(existingImages)

        //add new image
        imgTmp.add(0, newImgUri)
        carImages.value = imgTmp
        MyLogger.logThis(
            TAG,
            "addImage()",
            "added $newImgUri new size == ${imgTmp.size}"
        )
    }

    private val selectedImages = ArrayList<String>()
    fun getSelectedImagesNum() = selectedImages.size
    fun selectCarImg(imgUrl: String) {
        selectedImages.add(imgUrl)
    }

    fun unSelectCarImg(imgUrl: String) {
        selectedImages.remove(imgUrl)
    }

    fun deleteSelectedImages() {
        val allImages = carImages.value
        if (allImages.isNullOrEmpty() || selectedImages.isNullOrEmpty())
            return
        else {
            val newImages = ArrayList<String>()
            newImages.addAll(
                allImages.filterNot { imgUrl -> selectedImages.contains(imgUrl) }
            )
            carImages.value = newImages
        }
    }

    enum class SavingState {
        IDLE,
        SAVING,
        SAVED,
        ERROR
    }

    private val savingState = MutableLiveData<SavingState>(SavingState.IDLE)
    var errMsgRes: Int? = null
    fun getSavingState(): LiveData<SavingState> = savingState
    fun save() {
        if (savingState.value == SavingState.SAVING) return
        if (carImages.value.isNullOrEmpty()) {
            //image must have data to actually save
            errMsgRes = R.string.please_add_photos
            savingState.value = SavingState.ERROR
            return
        }
        savingState.value = SavingState.SAVING
        viewModelScope.launch(Dispatchers.IO) {
            carData?.let { car ->

                val oldId = car.carId
                val photosToUpload = carImages.value!!
                val uploadedImages = carRepo.uploadImages(photosToUpload, car.ownerId)
                if (uploadedImages.isNullOrEmpty()) {
                    errMsgRes = R.string.failed_to_upload_images
                    savingState.postValue(SavingState.ERROR)
                } else {
                    car.imageUrls = uploadedImages
                    MyLogger.logThis(
                        TAG,
                        "aboutToCompress",
                        "compressed uris ${uploadedImages.size}"
                    )

                    //save on fire-store
                    val carId = carRepo.addMyCarToFireStore(car)
                    if (carId == null) {
                        errMsgRes = R.string.failed_to_save_car
                        savingState.postValue(SavingState.ERROR)

                    } else {
                        car.carId = carId
                        //save locally
                        MyLogger.logThis(
                            TAG, "savingCarLocally() --- ",
                            "car ${car.imageUrls} old id $oldId"
                        )
                        carRepo.deleteTmpCarAndSaveCar(car, oldId=oldId)
                        withContext(Dispatchers.Main) {
                            savingState.value = SavingState.SAVED
                        }
                    }
                }
            }
        }
    }

}
