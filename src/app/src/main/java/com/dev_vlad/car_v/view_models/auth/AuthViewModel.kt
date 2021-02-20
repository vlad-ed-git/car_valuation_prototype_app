package com.dev_vlad.car_v.view_models.auth

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.util.MyLogger
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import kotlinx.coroutines.launch
import com.dev_vlad.car_v.R
import com.google.firebase.auth.*
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers

class AuthViewModel (private val repository: UserRepo) : ViewModel() {

    companion object {
        private val TAG =  AuthViewModel::class.java.simpleName
    }


    /********** USER DATA ************/
    lateinit var userPhone: String
    fun isUserPhoneInitialized() = (::userPhone.isInitialized)
    lateinit var userCode: String
    lateinit var userCountry: String
    /**************** COUNTRY CODES *******************/
    private val countries = arrayListOf<String>()
    private val countryCodes = arrayListOf<String>()
    fun initCountryAndCodes(countryNCodes: Array<String>) {
        if (countries.isNotEmpty()) return
        for (country_n_code in countryNCodes){
            countries.add(country_n_code.substringBefore(" "))
            countryCodes.add(country_n_code.substringAfter(" "))
        }
    }
    fun getCountries() = countries
    fun setCountryAndGetCode(country : String?) : String{
        MyLogger.logThis(
                TAG,  "getCountryCode()", "country $country"
        )
        if (country.isNullOrBlank()) return ""
        userCountry = country //set country
        val index = countries.indexOf(country)
        return if(index < 0) "" else countryCodes[index]
    }


    /******************* VERIFICATION PROCESS ***********/
    private val signInState = MutableLiveData<SIGNINSTATE>()
    fun getLoginState() : LiveData<SIGNINSTATE> = signInState
    fun setSignInState(state: SIGNINSTATE) {
        signInState.value = state
    }

    var phoneAuthCredential : PhoneAuthCredential? = null
    var signInErrRes : Int? = null
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    val onVerificationStateChangedCallbacks = object :
        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            MyLogger.logThis(TAG, "onVerificationCompleted()", "credentials $credential")
            phoneAuthCredential = credential
            setSignInState(SIGNINSTATE.STATE_VERIFY_SUCCESS)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            MyLogger.logThis(TAG, "onVerificationFailed", e.message?:" no message")

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                signInErrRes = R.string.invalid_phone_err
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                //TODO log
                    signInErrRes = R.string.sms_quota_exceeded_err
            }

            setSignInState(SIGNINSTATE.STATE_VERIFY_FAILED)

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            MyLogger.logThis(TAG, "onCodeSent()", "verification id $verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            setSignInState(SIGNINSTATE.STATE_CODE_SENT)
        }
    }
    // [END phone_auth_callbacks]

    /*************** SIGN IN PROCESS **********/
    private val userData = MutableLiveData<UserEntity>()
    fun getUserData() : LiveData<UserEntity> = userData
    fun storeUserInFireStore(user: FirebaseUser) {
        setSignInState(SIGNINSTATE.SAVING_USER_IN_SERVER)
        MyLogger.logThis(
            TAG, "storeUserInFireStore()", "user id $user.uid"
        )
        val userEntity = UserEntity(
            userId = user.uid,
            userPhone = userPhone,
            userCode = userCode,
            userLocationCountry = userCountry,
            isDealer = false,
        isSeller = false)
        userData.value = userEntity
        viewModelScope.launch {
            val isSaved = repository.saveUserInFireStore(userEntity)
            if (!isSaved){
                setSignInState(SIGNINSTATE.STATE_SIGN_IN_FAILED)
            }else{
                saveUser(userEntity)
            }
        }

    }

    val userState: LiveData<List<UserEntity>> = repository.user.asLiveData()
    private fun saveUser(user: UserEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertUser(user)
        MyLogger.logThis(
            TAG,
            "saveUser()",
            "savingUser locally -- $user"
        )
    }


}

enum class SIGNINSTATE {
    IDLE,
    VERIFICATION_IN_PROGRESS,
    STATE_VERIFY_SUCCESS,
    STATE_VERIFY_FAILED,
    STATE_CODE_SENT,
    SAVING_USER_IN_SERVER,
    STATE_SIGN_IN_FAILED,
    STATE_SIGN_IN_SUCCESS
}