package com.dev_vlad.car_v.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentLoginBinding
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.hideKeyBoard
import com.dev_vlad.car_v.util.showSnackBarToUser
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import com.dev_vlad.car_v.view_models.auth.AuthViewModelFactory
import com.dev_vlad.car_v.view_models.auth.SIGNINSTATE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {
    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }


    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        AuthViewModelFactory(
            userRepo = carVApp.userRepo,
            carRepo = carVApp.carRepo,
            offersRepo = carVApp.offerRepo
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        //init country codes array
        val countryAndCodes = resources.getStringArray(R.array.country_codes)
        authViewModel.initCountryAndCodes(countryAndCodes)

        initViews()

        return binding.root
    }

    private fun initViews() {
        binding.apply {
            val adapter =
                ArrayAdapter(requireContext(), R.layout.country_item, authViewModel.getCountries())
            val countryInput = (countryPicker.editText as? AutoCompleteTextView)
            countryInput?.setAdapter(adapter)
            countryInput?.onItemClickListener =
                OnItemClickListener { _, _, position, _ ->
                    val selectedCountry: String? = adapter.getItem(position)
                    val countryCode = authViewModel.setCountryAndGetCode(selectedCountry)
                    MyLogger.logThis(
                        TAG, "initViews()", "country $selectedCountry code $countryCode"
                    )
                    phoneCode.editText?.setText(countryCode)

                }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = Firebase.auth
        binding.getVerificationCode.setOnClickListener {
            hideKeyBoard(requireContext(), binding.container)
            onGetCodeClicked()
        }
        binding.signIn.setOnClickListener {
            hideKeyBoard(requireContext(), binding.container)
            signInWithCode()
        }


        //user data
        observeUserData()

        //login process
        observeSignInProcess()

        //USER SIGN IN STATE
        observeAuthState()
    }


    private fun observeUserData() {
        authViewModel.getUserData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.apply {
                    val countryInput = (countryPicker.editText as? AutoCompleteTextView)
                    countryInput?.setText(it.userLocationCountry)
                    phoneNumber.editText?.setText(it.userPhone)
                    phoneCode.editText?.setText(it.userCode)
                }
            }
        })
    }

    private fun observeAuthState() {
        authViewModel.userState.observe(
            viewLifecycleOwner,
            Observer {
                if (it.isNullOrEmpty() || it.size != 1) {
                    authViewModel.setSignInState(SIGNINSTATE.IDLE)
                } else {
                    authViewModel.setSignInState(SIGNINSTATE.STATE_SIGN_IN_SUCCESS)
                }
            }
        )
    }

    private fun observeSignInProcess() {
        authViewModel.getLoginState().observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    SIGNINSTATE.VERIFICATION_IN_PROGRESS -> {
                        binding.loadingBar.isVisible = true
                    }
                    SIGNINSTATE.STATE_VERIFY_SUCCESS -> {
                        //auto verified
                        binding.getVerificationCode.setText(R.string.verified_txt)
                        binding.verificationCode.isEnabled = true
                        disableActions()
                        signInWithCredentials()
                    }
                    SIGNINSTATE.STATE_VERIFY_FAILED -> {
                        binding.getVerificationCode.setText(R.string.resend_code_txt)
                        binding.getVerificationCode.isEnabled = true
                        binding.loadingBar.isVisible = false
                        val errRes = authViewModel.signInErrRes ?: R.string.verification_err_unknown
                        authViewModel.signInErrRes = null //clear after user
                        showSnackBar(errRes, true)
                    }
                    SIGNINSTATE.STATE_CODE_SENT -> {
                        binding.getVerificationCode.setText(R.string.resend_code_txt)
                        binding.getVerificationCode.isEnabled = true
                        binding.loadingBar.isVisible = false
                        binding.signIn.isEnabled = true
                        showSnackBar(
                            errorRes = R.string.verification_code_was_sent_txt,
                            isError = false
                        )
                    }
                    SIGNINSTATE.STATE_SIGN_IN_FAILED -> {
                        //reset everything
                        resetViewsState()
                        val errorRes = authViewModel.signInErrRes ?: R.string.failed_to_sign_in_err
                        authViewModel.signInErrRes = null //clear
                        showSnackBar(errorRes, isError = true)
                    }

                    SIGNINSTATE.STATE_SIGN_IN_SUCCESS -> {
                        resetViewsState()
                        val action = LoginFragmentDirections.actionLoginFragmentToWelcomeFragment()
                        findNavController().navigate(action)
                    }

                    SIGNINSTATE.IDLE -> {
                        resetViewsState()
                    }

                    else -> {
                    }
                }
            }
        )

    }

    /******** reset views state ********/
    private fun resetViewsState() {
        binding.getVerificationCode.setText(R.string.get_code_txt)
        binding.getVerificationCode.isEnabled = true
        binding.signIn.isEnabled = true
        binding.loadingBar.isVisible = false
        binding.verificationCode.editText?.setText("")

    }

    /********** VERIFICATION *************/
    private fun onGetCodeClicked() {
        binding.getVerificationCode.isEnabled = false
        binding.getVerificationCode.setText(R.string.sending_verification_code)
        val code = binding.phoneCode.editText?.text.toString()
        if (code.isBlank()) {
            showSnackBar(R.string.country_code_empty_err_txt, true)
            binding.getVerificationCode.isEnabled = true
            return
        }

        val phone = binding.phoneNumber.editText?.text.toString()
        if (phone.isBlank()) {
            showSnackBar(R.string.phone_empty_err_txt, true)
            binding.getVerificationCode.isEnabled = true
            return
        }

        //situation - user types phone number asks for code changes phone clicks resend code
        if (authViewModel.isUserPhoneInitialized()) {
            if (phone.trim() != authViewModel.userPhone) {
                //user has added a new phone number
                authViewModel.resendToken = null //clear resend code
            }
        }
        authViewModel.userCode = code.trim()
        authViewModel.userPhone = phone.trim()

        val phoneNumber = code + phone
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(authViewModel.onVerificationStateChangedCallbacks)
        authViewModel.resendToken?.let {
            // callback's ForceResendingToken
            optionsBuilder.setForceResendingToken(it)
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        authViewModel.setSignInState(SIGNINSTATE.VERIFICATION_IN_PROGRESS)
    }


    /********** SIGN IN *************/
    private fun disableActions() {
        binding.getVerificationCode.isEnabled = false
        binding.signIn.isEnabled = false
        binding.loadingBar.isVisible = true
    }

    private fun signInWithCredentials() {
        val credentials = authViewModel.phoneAuthCredential
        if (credentials == null) {
            authViewModel.setSignInState(SIGNINSTATE.STATE_SIGN_IN_FAILED)
            showSnackBar(errorRes = R.string.verify_phone_first, isError = true)
            return
        }
        auth.signInWithCredential(credentials)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && (task.result?.user) != null) {

                    MyLogger.logThis(TAG, "signInWithCredentials()", "Success")

                    authViewModel.storeUserInFireStoreIfNotExist(task.result?.user!!)
                } else {
                    // Sign in failed, display a message and update the UI
                    MyLogger.logThis(
                        TAG,
                        "signInWithCredentials()",
                        "failed ${task.exception?.message}",
                        task.exception
                    )
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        authViewModel.signInErrRes = R.string.invalid_verification_code_err
                    }
                    authViewModel.setSignInState(SIGNINSTATE.STATE_SIGN_IN_FAILED)
                }
            }
    }

    private fun signInWithCode() {
        disableActions()
        val code = binding.verificationCode.editText?.text
        if (authViewModel.storedVerificationId != null
            && !code.isNullOrBlank()
        ) {
            authViewModel.phoneAuthCredential = PhoneAuthProvider.getCredential(
                authViewModel.storedVerificationId!!,
                code.toString()
            )
            signInWithCredentials()
        }
    }

    private fun showSnackBar(errorRes: Int, isError: Boolean) {
        binding.container.showSnackBarToUser(
            msgResId = errorRes,
            isErrorMsg = isError,
            actionMessage = if (isError) R.string.dismiss else null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}