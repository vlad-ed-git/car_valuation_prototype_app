package com.dev_vlad.car_v.views.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentWelcomeBinding
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.hideKeyBoard
import com.dev_vlad.car_v.util.showSnackBarToUser
import com.dev_vlad.car_v.view_models.welcome.WelcomeViewModel
import com.dev_vlad.car_v.view_models.welcome.WelcomeViewModelFactory

class WelcomeFragment : Fragment() {

    companion object {
        private val TAG = WelcomeFragment::class.java.simpleName
    }

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val welcomeViewModel: WelcomeViewModel by viewModels {
        WelcomeViewModelFactory((activity?.application as CarVApp).userRepo)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        welcomeViewModel.errorOccurred.observe(viewLifecycleOwner, Observer { errorOccurred ->
            if (errorOccurred) {
                binding.apply {
                    doneBtn.isEnabled = true
                    loadingBar.isVisible = false
                    container.showSnackBarToUser(
                            msgResId = R.string.updating_user_status_failed,
                            isErrorMsg = true
                    )
                }
            }
        })
        welcomeViewModel.userState.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                val userEntity = it[0]
                welcomeViewModel.setUserData(userEntity)
                if (userEntity.isSeller || userEntity.isDealer) {
                    //one of them is set
                    binding.apply {
                        doneBtn.isEnabled = false
                        loadingBar.isVisible = true
                    }

                    if (userEntity.isSeller) {
                        val action =
                                WelcomeFragmentDirections.actionWelcomeFragmentToSellersHomeFragment()
                        findNavController().navigate(action)
                    } else if (userEntity.isDealer) {
                        val action = WelcomeFragmentDirections.actionWelcomeFragmentToDealersHomeFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        })
    }


    private fun initViews() {
        binding.apply {
            buyerCard.setOnLongClickListener {
                toggleBuyer()
                true
            }

            sellerCard.setOnLongClickListener {
                toggleSeller()
                true
            }

            buyerCard.setOnClickListener {
                toggleBuyer()
            }

            sellerCard.setOnClickListener {
                toggleSeller()
            }


            doneBtn.setOnClickListener {
                hideKeyBoard(requireContext(), binding.container)
                MyLogger.logThis(
                        TAG,
                        "setOnClickListener()",
                        "params isBuyer ${buyerCard.isChecked} isSeller ${sellerCard.isChecked}"
                )
                if (buyerCard.isChecked || sellerCard.isChecked) {
                    val userName = userName.editText?.text?.toString()
                    if (buyerCard.isChecked) {
                        welcomeViewModel.updateUser(
                                setAsBuyer = true,
                                setAsSeller = false,
                                userGivenName = userName
                        )
                    } else {
                        welcomeViewModel.updateUser(
                                setAsBuyer = false,
                                setAsSeller = true,
                                userGivenName = userName
                        )
                    }
                    binding.doneBtn.isEnabled = false
                    binding.loadingBar.isVisible = true
                } else {
                    showSnackBar(
                            errorRes = R.string.select_dealer_or_seller_err,
                            isError = false
                    )
                }
            }

        }
    }

    private fun toggleSeller() {
        binding.apply {
            sellerCard.isChecked = !sellerCard.isChecked
            buyerCard.isChecked = !sellerCard.isChecked
        }
    }

    private fun toggleBuyer() {
        binding.apply {
            buyerCard.isChecked = !buyerCard.isChecked
            sellerCard.isChecked = !buyerCard.isChecked
        }
    }


    private fun showSnackBar(errorRes: Int, isError: Boolean, actionMsgRes: Int? = null) {
        binding.container.showSnackBarToUser(
                msgResId = errorRes,
                isErrorMsg = isError,
                actionMessage = actionMsgRes
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}