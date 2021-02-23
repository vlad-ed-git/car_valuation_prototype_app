package com.dev_vlad.car_v.views.adapters.dealers

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
import com.dev_vlad.car_v.databinding.CarsItemBinding
import com.dev_vlad.car_v.view_models.dealers.home.CarsWrapperForDealers
import java.util.*

class CarsAdapter(private val actionListener: CarsActionsListener) :
    ListAdapter<CarsWrapperForDealers, CarsAdapter.CarsAdapterVH>(CarsAdapterDifUtil()) {

    companion object {
        private val TAG = CarsAdapter::class.java.simpleName
    }

    interface CarsActionsListener {
        fun onCarClicked(clickedCar: CarsWrapperForDealers)
    }

    class CarsAdapterVH(private val binding: CarsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(carWrapperForDealer: CarsWrapperForDealers, actionListener: CarsActionsListener) {
            binding.apply {
                val car = carWrapperForDealer.car
                val titleTxt =
                    car.make.capitalize(Locale.getDefault()) + " " + car.model.capitalize(Locale.getDefault())
                title.text = titleTxt

                //offer sent or not
                hasMadeOffer.isVisible = (carWrapperForDealer.offerSent != null)

                carCard.setOnClickListener {
                    actionListener.onCarClicked(carWrapperForDealer)
                }
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
                                featuredImage.scaleType = ImageView.ScaleType.CENTER_CROP
                                return false
                            }

                        }
                    )
                    .into(featuredImage)

            }
        }

    }

    class CarsAdapterDifUtil : DiffUtil.ItemCallback<CarsWrapperForDealers>() {
        override fun areItemsTheSame(
            oldItem: CarsWrapperForDealers,
            newItem: CarsWrapperForDealers
        ): Boolean {
            return oldItem.car.carId == newItem.car.carId
        }

        override fun areContentsTheSame(
            oldItem: CarsWrapperForDealers,
            newItem: CarsWrapperForDealers
        ): Boolean {
            return (oldItem.car.carId == newItem.car.carId) &&
                    (oldItem.car.updatedAt == newItem.car.updatedAt) &&
                    (oldItem.offerSent?.offerPrice == newItem.offerSent?.offerPrice)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsAdapterVH {
        val myCarsItemBinding =
            CarsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarsAdapterVH(myCarsItemBinding)
    }

    override fun onBindViewHolder(holder: CarsAdapterVH, position: Int) {
        val car = getItem(position)
        if (car != null)
            holder.bind(car, actionListener)
    }
}