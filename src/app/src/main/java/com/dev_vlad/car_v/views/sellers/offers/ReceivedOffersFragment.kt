package com.dev_vlad.car_v.views.sellers.offers

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
import androidx.recyclerview.widget.RecyclerView
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentReceivedOffersBinding
import com.dev_vlad.car_v.models.persistence.chat.ChatInitiateData
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.view_models.sellers.offers.CarNReceivedOfferWrapper
import com.dev_vlad.car_v.view_models.sellers.offers.ReceivedOffersViewModel
import com.dev_vlad.car_v.view_models.sellers.offers.ReceivedOffersViewModelFactory
import com.dev_vlad.car_v.views.adapters.sellers.ReceivedOffersAdapter

class ReceivedOffersFragment : Fragment(), ReceivedOffersAdapter.ReceivedOffersActionsListener {

    companion object {
        private val TAG = ReceivedOffersFragment::class.java.simpleName
    }

    private var _binding: FragmentReceivedOffersBinding? = null
    private val binding get() = _binding!!
    private val receivedOffersViewModel: ReceivedOffersViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        ReceivedOffersViewModelFactory(carVApp.userRepo, carVApp.carRepo, carVApp.offerRepo)
    }
    private val receivedOffersAdapter = ReceivedOffersAdapter(this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReceivedOffersBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        receivedOffersViewModel.getCurrentUser().observe(
            viewLifecycleOwner, Observer {
                if (it != null) {
                    observeReceivedOffers()
                }

            }
        )
    }

    private fun observeReceivedOffers() {
        receivedOffersViewModel.observeReceivedOffers().observe(
            viewLifecycleOwner, Observer {
                binding.loadingBar.isVisible = false
                receivedOffersViewModel.isLoading = false
                if (it == null) {
                    MyLogger.logThis(
                        TAG,
                        "observeReceivedOffers()",
                        "offers list is null"
                    )
                } else {
                    MyLogger.logThis(
                        TAG,
                        "observeReceivedOffers()",
                        "Found ${it.size} offers"
                    )
                    receivedOffersAdapter.submitList(it)
                }
            }
        )
    }

    private fun initViews() {
        binding.apply {
            receivedOffersRv.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!receivedOffersRv.canScrollVertically(1)){
                            //we have reached the bottom of the list
                            receivedOffersViewModel.fetchMoreOffers(totalItemsInListNow = receivedOffersAdapter.itemCount)
                        }
                    }
                })
                adapter = receivedOffersAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
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

    override fun onReceivedOffersClicked(item: CarNReceivedOfferWrapper) {
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
            ReceivedOffersFragmentDirections.actionReceivedOffersFragmentToChatFragment(data)
        findNavController().navigate(action)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}