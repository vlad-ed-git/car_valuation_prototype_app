package com.dev_vlad.car_v.views.dialogs

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev_vlad.car_v.R

class DialogFullImage (
        private val imgUrlStr : String
) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view: View = inflater.inflate(R.layout.dialog_full_image, null)
            builder.setView(view)

            val img = view.findViewById<ImageView>(R.id.zoomed_image)
            Glide.with(view.context)
                    .load(imgUrlStr)
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


            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}