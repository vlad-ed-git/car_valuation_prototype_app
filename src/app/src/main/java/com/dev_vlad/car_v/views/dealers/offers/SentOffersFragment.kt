package com.dev_vlad.car_v.views.dealers.offers

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentSentOffersBinding
import com.dev_vlad.car_v.models.persistence.chat.ChatInitiateData
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.view_models.dealers.offers.CarNSentOfferWrapper
import com.dev_vlad.car_v.view_models.dealers.offers.SentOffersViewModel
import com.dev_vlad.car_v.view_models.dealers.offers.SentOffersViewModelFactory
import com.dev_vlad.car_v.views.adapters.dealers.SentOffersAdapter

class SentOffersFragment : Fragment(), SentOffersAdapter.SentOffersActionsListener {

    companion object {
        private val TAG = SentOffersFragment::class.java.simpleName
    }

    private var _binding: FragmentSentOffersBinding? = null
    private val binding get() = _binding!!
    private val sentOffersViewModel: SentOffersViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        SentOffersViewModelFactory(carVApp.userRepo, carVApp.carRepo, carVApp.offerRepo)
    }
    private val sentOffersAdapter = SentOffersAdapter(this)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSentOffersBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sentOffersViewModel.getCurrentUser().observe(
                viewLifecycleOwner, Observer {
            if (it != null) {
                observeSentOffers()
            }

        }
        )
    }

    private fun observeSentOffers() {
        sentOffersViewModel.observeSentOffers().observe(
                viewLifecycleOwner, Observer {
            binding.loadingBar.isVisible = false
            if (it == null) {
                MyLogger.logThis(
                        TAG,
                        "observeSentOffers()",
                        "offers list is null"
                )
            } else {
                MyLogger.logThis(
                        TAG,
                        "observeSentOffers()",
                        "Found ${it.size} offers"
                )
                sentOffersAdapter.submitList(it)
            }
        }
        )
    }

    private fun initViews() {
        binding.apply {
            sentOffersRv.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = sentOffersAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sellers_home_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //todo menu? -- about, profile, etc
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
        )
    }

    override fun onSentOffersClicked(item: CarNSentOfferWrapper) {
        val data = ChatInitiateData(
                carId = item.car.carId,
                carTitle = item.car.make + " " + item.car.model,
                ownerId = item.car.ownerId,
                dealerId = item.offer.dealerId,
                initialOffer = item.offer.offerPrice,
                initialOfferMsg = item.offer.offerMessage,
                offerId = item.offer.offerId,
                featuredImgUrl = item.car.imageUrls[0]
        )
        val action =
                SentOffersFragmentDirections.actionSentOffersFragmentToChatFragment(data)
        findNavController().navigate(action)

    }

}