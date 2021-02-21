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
import com.dev_vlad.car_v.databinding.MyCarsItemBinding
import com.dev_vlad.car_v.models.persistence.cars.CarEntity
import java.util.*

class MyCarsAdapter(private val actionListener: MyCarsActionsListener)
    : ListAdapter<CarEntity, MyCarsAdapter.MyCarsAdapterVH>(MyCarsAdapterDifUtil()) {

    companion object {
        private val TAG = MyCarsAdapter::class.java.simpleName
    }

    interface MyCarsActionsListener {
        fun onCarClicked(clickedCar: CarEntity)
    }

    class MyCarsAdapterVH(private val binding: MyCarsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: CarEntity, actionListener: MyCarsActionsListener) {
            binding.apply {
                val titleTxt = car.make.capitalize(Locale.getDefault()) + " " + car.model.capitalize(Locale.getDefault())
                title.text = titleTxt
                carCard.setOnClickListener {
                    actionListener.onCarClicked(car)
                }
                val imgUrl = car.image_urls[0] //todo car.image_urls is never empty?!
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

    class MyCarsAdapterDifUtil : DiffUtil.ItemCallback<CarEntity>() {
        override fun areItemsTheSame(oldItem: CarEntity, newItem: CarEntity): Boolean {
            return oldItem.carId == newItem.carId
        }

        override fun areContentsTheSame(oldItem: CarEntity, newItem: CarEntity): Boolean {
            return (oldItem.carId == newItem.carId) &&
                    (oldItem.updated_at == newItem.updated_at)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCarsAdapterVH {
        val myCarsItemBinding = MyCarsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyCarsAdapterVH(myCarsItemBinding)
    }

    override fun onBindViewHolder(holder: MyCarsAdapterVH, position: Int) {
        val car = getItem(position)
        if (car != null)
            holder.bind(car, actionListener)
    }
}