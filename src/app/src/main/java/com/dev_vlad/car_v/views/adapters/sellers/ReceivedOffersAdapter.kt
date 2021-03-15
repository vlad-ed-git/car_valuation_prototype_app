package com.dev_vlad.car_v.views.adapters.sellers

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.ReceivedOfferItemBinding
import com.dev_vlad.car_v.view_models.sellers.offers.CarNReceivedOfferWrapper
import java.util.*

class ReceivedOffersAdapter(private val actionListener: ReceivedOffersActionsListener) :
    ListAdapter<CarNReceivedOfferWrapper, ReceivedOffersAdapter.ReceivedOffersAdapterVH>(
        ReceivedOffersAdapterDifUtil()
    ) {

    companion object {
        private val TAG = ReceivedOffersAdapter::class.java.simpleName
    }

    interface ReceivedOffersActionsListener {
        fun onReceivedOffersClicked(item: CarNReceivedOfferWrapper)
    }

    class ReceivedOffersAdapterVH(private val binding: ReceivedOfferItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CarNReceivedOfferWrapper, actionListener: ReceivedOffersActionsListener) {
            binding.apply {
                val car = item.car
                val titleTxt =
                    car.make.capitalize(Locale.getDefault()) + " " + car.model.capitalize(Locale.getDefault())
                carTitle.text = titleTxt
                itemView.setOnClickListener {
                    actionListener.onReceivedOffersClicked(item)
                }
                val formattedPrice =
                    item.offer.offerPrice.toString() + " " + itemView.context.getString(R.string.currency_suffix)
                initialPrice.text = formattedPrice
                val imgUrl =
                    if (car.imageUrls.isNotEmpty() && car.imageUrls[0].length > 4) car.imageUrls[0]
                    else ""
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
                                carImg.scaleType = ImageView.ScaleType.CENTER_CROP
                                return false
                            }

                        }
                    )
                    .into(carImg)

            }
        }

    }

    class ReceivedOffersAdapterDifUtil : DiffUtil.ItemCallback<CarNReceivedOfferWrapper>() {
        override fun areItemsTheSame(
            oldItem: CarNReceivedOfferWrapper,
            newItem: CarNReceivedOfferWrapper
        ): Boolean {
            return (oldItem.car.carId == newItem.car.carId) && (oldItem.offer.offerId == newItem.offer.offerId)
        }

        override fun areContentsTheSame(
            oldItem: CarNReceivedOfferWrapper,
            newItem: CarNReceivedOfferWrapper
        ): Boolean {
            return (oldItem.car == newItem.car) &&
                    (oldItem.offer == newItem.offer)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedOffersAdapterVH {
        val binding =
            ReceivedOfferItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceivedOffersAdapterVH(binding)
    }

    override fun onBindViewHolder(holder: ReceivedOffersAdapterVH, position: Int) {
        val car = getItem(position)
        if (car != null)
            holder.bind(car, actionListener)
    }
}