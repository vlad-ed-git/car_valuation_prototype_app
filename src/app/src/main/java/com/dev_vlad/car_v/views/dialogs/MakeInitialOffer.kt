package com.dev_vlad.car_v.views.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dev_vlad.car_v.R
import com.dev_vlad.car_v.util.myTxt
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class MakeInitialOffer(private val listener: MakeInitialOfferListener) : DialogFragment() {


    interface MakeInitialOfferListener {
        fun onSendOfferClicked(initialOfferPrice: String?, message: String?)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view: View = inflater.inflate(R.layout.dialog_make_offer, null)
            builder.setView(view)

            view.findViewById<MaterialButton>(R.id.later_btn).setOnClickListener {
                dismiss()
            }

            view.findViewById<MaterialButton>(R.id.send_offer_btn).setOnClickListener {
                val initialOffer = view.findViewById<TextInputLayout>(R.id.initial_offer)
                val initialOfferPrice = initialOffer.myTxt(initialOffer)
                val initialMsg = view.findViewById<TextInputLayout>(R.id.message)
                val message = initialMsg.myTxt(initialMsg)
                listener.onSendOfferClicked(initialOfferPrice, message)
                dismiss()
            }


            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}