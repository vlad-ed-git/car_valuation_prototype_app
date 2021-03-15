package com.dev_vlad.car_v.views.chat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.dev_vlad.car_v.util.REQUEST_IMAGE_IN_GALLERY
import com.dev_vlad.car_v.util.hideKeyBoard
import com.dev_vlad.car_v.view_models.chat.ChatViewModel
import com.dev_vlad.car_v.view_models.chat.ChatViewModelFactory
import com.dev_vlad.car_v.views.adapters.chat.ChatAdapter
import com.dev_vlad.car_v.views.dialogs.DialogFullImage
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
        chatViewModel.setInitializationData(args.ChatInitializationData)
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        chatViewModel.observeCurrentUser().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                val userType = if (it.isDealer) ChatAdapter.UserType.DEALER
                else ChatAdapter.UserType.SELLER
                chatAdapter = ChatAdapter(this@ChatFragment, userType)
                initRv()
                displayInitialData()
                observeMessages()
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
            messagesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!messagesRv.canScrollVertically(1)) {
                        //we have reached the bottom of the list
                        chatViewModel.fetchMoreCars(totalItemsInListNow = chatAdapter.itemCount)
                    }
                }
            })
            messagesRv.adapter = chatAdapter
            send.setOnClickListener {
                val newMsg = newMessage.text.toString()
                if (newMsg.isNotBlank()) {
                    chatViewModel.sendNewMessage(newMsg)
                    newMessage.setText("")
                    hideKeyBoard(requireContext(), messageContainer)
                }
            }

           addPic.setOnClickListener{
               addPhotoFromGallery()
           }
        }
    }

    private fun addPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, REQUEST_IMAGE_IN_GALLERY)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_IN_GALLERY && resultCode == Activity.RESULT_OK) {
            MyLogger.logThis(
                    TAG,
                    " addPhotoFromGallery() -> onActivityResult()", "returned data ${data?.data}"
            )
            data?.data?.let { imgUri ->
                chatViewModel.sendImageMessage(imgUri.toString())
            }
        }

    }


    private fun observeMessages() {
        chatViewModel.observeMessages().observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
            chatViewModel.isLoading = false
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

    private var dialogShowFullImage : DialogFullImage? = null
    override fun onMessageClicked(clickedMessage: ChatEntity) {
        if (clickedMessage.messageIsImage){
            if (dialogShowFullImage != null) {
                if (dialogShowFullImage?.isVisible == true)
                    dialogShowFullImage?.dismiss()
                dialogShowFullImage = null
            }
            dialogShowFullImage = DialogFullImage(clickedMessage.message)
            dialogShowFullImage?.show(parentFragmentManager, "com.dev_vlad.car_v.zoom_img_dialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (dialogShowFullImage != null) {
            if (dialogShowFullImage?.isVisible == true)
                dialogShowFullImage?.dismiss()
            dialogShowFullImage = null
        }
    }

}