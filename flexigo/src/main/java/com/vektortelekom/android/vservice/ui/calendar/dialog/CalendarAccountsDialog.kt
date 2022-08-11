package com.vektortelekom.android.vservice.ui.calendar.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.common.SignInButton
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.CalendarAccountsDialogBinding

class CalendarAccountsDialog(context: Context, val activity: Activity, val listener: CalendarAccountsListener): Dialog(context) {

    private lateinit var binding: CalendarAccountsDialogBinding
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = CalendarAccountsDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setGoogleSignInView(AppDataManager.instance.googleCalendarAccessToken.isNullOrEmpty().not())
        setOutlookSignInView(AppDataManager.instance.outlookCalendarAccessToken.isNullOrEmpty().not())


    }

    fun setGoogleSignInView(isSignIn: Boolean) {
        if (isSignIn) {
            binding.buttonGoogleSignIn.visibility = View.GONE
            binding.buttonGoogleSignOut.visibility = View.VISIBLE

            binding.buttonGoogleSignOut.setOnClickListener {
                listener.googleSignOut()

            }
        } else {
            binding.buttonGoogleSignOut.visibility = View.GONE
            binding.buttonGoogleSignIn.visibility = View.VISIBLE

            binding.buttonGoogleSignIn.setSize(SignInButton.SIZE_STANDARD)

            binding.buttonGoogleSignIn.setOnClickListener {
                listener.googleSignIn()
            }
        }
    }

    fun setOutlookSignInView(isSignIn: Boolean) {
        if (isSignIn) {
            binding.buttonOutlookSignIn.visibility = View.GONE
            binding.buttonOutlookSignOut.visibility = View.VISIBLE

            binding.buttonOutlookSignOut.setOnClickListener {
                listener.outlookSignOut()

            }
        } else {
            binding.buttonOutlookSignOut.visibility = View.GONE
            binding.buttonOutlookSignIn.visibility = View.VISIBLE

            binding.buttonOutlookSignIn.setOnClickListener {
                listener.outlookSignIn()
            }
        }
    }


    interface CalendarAccountsListener {
        fun googleSignIn()
        fun googleSignOut()
        fun outlookSignIn()
        fun outlookSignOut()
    }

}