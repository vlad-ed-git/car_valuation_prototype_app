package com.dev_vlad.car_v.views.chat

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev_vlad.car_v.CarVApp
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.FragmentChatBinding
import com.dev_vlad.car_v.models.persistence.chat.ChatEntity
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.util.hideKeyBoard
import com.dev_vlad.car_v.view_models.chat.ChatViewModel
import com.dev_vlad.car_v.view_models.chat.ChatViewModelFactory
import com.dev_vlad.car_v.views.adapters.chat.ChatAdapter
import java.util.*

class ChatFragment : Fragment(), ChatAdapter.ChatActionsListener {

    companion object {
        private val TAG = ChatFragment::class.java.simpleName
    }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: ChatViewModel by viewModels {
        val carVApp = (activity?.application as CarVApp)
        ChatViewModelFactory(
            userRepo = carVApp.userRepo,
            chatRepo = carVApp.chatRepo
        )
    }
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var chatAdapter: ChatAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatViewModel.initializedData(args.ChatInitializationData)
        observeCurrentUser()
        displayInitialData()
        observeMessages()
    }

    private fun observeCurrentUser() {
        chatViewModel.observeCurrentUser().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                val userType = if (it.isDealer) ChatAdapter.UserType.DEALER
                else ChatAdapter.UserType.SELLER
                chatAdapter = ChatAdapter(this@ChatFragment, userType)
                initRv()
            }
        })
    }

    private fun displayInitialData() {
        binding.apply {
            val titleTxt =
                chatViewModel.chatInitiateData.carTitle.capitalize(Locale.getDefault())
            carTitle.text = titleTxt

            val formattedPrice =
                getString(R.string.starting_offer_prefix) + " " + chatViewModel.chatInitiateData.initialOffer.toString() + " " + getString(
                    R.string.currency_suffix
                )
            initialPrice.text = formattedPrice
            Glide.with(requireContext())
                .load(chatViewModel.chatInitiateData.featuredImgUrl)
                .placeholder(R.drawable.logo_grey)
                .listener(
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            carImg.scaleType = ImageView.ScaleType.CENTER_CROP
                            return false
                        }

                    }
                )
                .into(carImg)
            loadingBar.isVisible = false
        }
    }

    private fun initRv() {
        binding.apply {
            messagesRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
            messagesRv.adapter = chatAdapter
            send.setOnClickListener {
                val newMsg = newMessage.text.toString()
                if (newMsg.isNotBlank()) {
                    chatViewModel.sendNewMessage(newMsg)
                    newMessage.setText("")
                    hideKeyBoard(requireContext(), messageContainer)
                }
            }
        }
    }

    private fun observeMessages() {
        chatViewModel.observeMessages().observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it != null) {
                    chatAdapter.submitList(it)
                    MyLogger.logThis(
                        TAG, "observeMessages()", "messages ${it.size} received"
                    )
                }
            }
        )
    }

    /******************** MENU ****************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //todo menu?
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
            item
        )
    }

    override fun onMessageClicked(clickedMessage: ChatEntity) {
        //TODO
    }

}