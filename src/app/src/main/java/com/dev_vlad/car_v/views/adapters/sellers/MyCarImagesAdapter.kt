package com.dev_vlad.car_v.views.adapters.sellers

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.databinding.MyCarImageItemBinding


class MyCarImagesAdapter(private val actionsListener: MyCarImagesAdapter.ImageActionsListeners? = null) :
    RecyclerView.Adapter<MyCarImagesAdapter.MyCarImagesViewHolder>() {

    private val images = ArrayList<String>()

    interface ImageActionsListeners {
        fun onSelectImage(imgUrl: String)
        fun onUnSelectImage(imgUrl: String)
    }

    class MyCarImagesViewHolder(private val binding: MyCarImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: String, actionsListener: ImageActionsListeners?) {
            binding.apply {
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
                                img.scaleType = ImageView.ScaleType.CENTER_CROP
                                return false
                            }

                        }
                    )
                    .into(img)

                //if listening for actions
                actionsListener?.let { listener ->
                    imgCard.setOnClickListener {
                        imgCard.isChecked = !imgCard.isChecked
                        if (imgCard.isChecked)
                            listener.onSelectImage(imgUrl)
                        else
                            listener.onUnSelectImage(imgUrl)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCarImagesAdapter.MyCarImagesViewHolder {
        val binding =
            MyCarImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyCarImagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyCarImagesAdapter.MyCarImagesViewHolder, position: Int) {
        val imgUrl = images[position]
        holder.bind(imgUrl, actionsListener)
    }

    fun setNewImages(newImages: List<String>) {
        this.images.clear()
        this.images.addAll(newImages)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return images.size
    }
}