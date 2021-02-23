package com.dev_vlad.car_v.views.adapters.chat

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.ChatMessageItemBinding
import com.dev_vlad.car_v.models.persistence.chat.ChatEntity

class ChatAdapter(private val actionListener: ChatActionsListener, private val userType: UserType) :
    ListAdapter<ChatEntity, ChatAdapter.ChatAdapterVH>(ChatAdapterDifUtil()) {

    enum class UserType {
        DEALER,
        SELLER
    }

    companion object {
        private val TAG = ChatAdapter::class.java.simpleName
        private const val IMG_URL_PREFIX = "http"
    }

    interface ChatActionsListener {
        fun onMessageClicked(clickedMessage: ChatEntity)
    }

    class ChatAdapterVH(private val binding: ChatMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun showImageMessage(imgUrl: String, inIv: ImageView) {
            Glide.with(itemView.context)
                .load(imgUrl)
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
                            inIv.scaleType = ImageView.ScaleType.CENTER_CROP
                            return false
                        }

                    }
                )
                .into(inIv)
        }

        private fun showSentMessage(msg: ChatEntity, binding: ChatMessageItemBinding) {
            binding.apply {
                receiverMessage.isVisible = false
                receiverMessageIv.isVisible = false
                if (msg.message.startsWith(IMG_URL_PREFIX)) {
                    senderMessage.isVisible = false
                    senderMessageIv.isVisible = true
                    showImageMessage(msg.message, senderMessageIv)
                } else {
                    senderMessageIv.isVisible = false
                    senderMessage.isVisible = true
                    senderMessage.text = msg.message
                }
            }
        }

        private fun showReceivedMessage(msg: ChatEntity, binding: ChatMessageItemBinding) {
            binding.apply {
                senderMessage.isVisible = false
                senderMessageIv.isVisible = false
                if (msg.message.startsWith(IMG_URL_PREFIX)) {
                    receiverMessage.isVisible = false
                    receiverMessageIv.isVisible = true
                    showImageMessage(msg.message, receiverMessageIv)

                } else {
                    receiverMessage.text = msg.message
                    receiverMessage.isVisible = true
                    receiverMessageIv.isVisible = false
                }
            }
        }

        fun bind(msg: ChatEntity, actionListener: ChatActionsListener, userType: UserType) {
            binding.apply {

                when (userType) {
                    UserType.DEALER -> {

                        when {
                            msg.sentByDealer -> {
                                //sent message
                                showSentMessage(msg, binding)
                            }
                            else -> {
                                //received message
                                showReceivedMessage(msg, binding)
                            }
                        }


                    }
                    else -> {
                        //user is seller
                        when {
                            msg.sentByOwner -> {
                                //sent message
                                showSentMessage(msg, binding)
                            }
                            else -> {
                                //received message
                                showReceivedMessage(msg, binding)
                            }
                        }

                    }
                }

            }
        }


    }

    class ChatAdapterDifUtil : DiffUtil.ItemCallback<ChatEntity>() {
        override fun areItemsTheSame(
            oldItem: ChatEntity,
            newItem: ChatEntity
        ): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(
            oldItem: ChatEntity,
            newItem: ChatEntity
        ): Boolean {
            return (oldItem.chatId == newItem.chatId) && (oldItem.sentOn == newItem.sentOn)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapterVH {
        val myChatMessageItemBinding =
            ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatAdapterVH(myChatMessageItemBinding)
    }

    override fun onBindViewHolder(holder: ChatAdapterVH, position: Int) {
        val msg = getItem(position)
        if (msg != null)
            holder.bind(msg, actionListener, userType)
    }

}