package com.vektortelekom.android.vservice.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.AppCustomDialogBinding

class AlertDialog(private var param: AppDialogParam) : Dialog(param.context) {

    private lateinit var binding: AppCustomDialogBinding

       override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = AppCustomDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.closeDialog.setOnClickListener {
            this.dismiss()
        }

        this.setCancelable(false)

        param.apply(this)
    }

    private fun setIcon(@DrawableRes iconId: Int) {
        setIcon(context.getDrawable(iconId)!!)
    }

    private fun setIcon(value: Drawable?) {
        if (value == null) {
            binding.icon.visibility = View.GONE
        } else {
            binding.icon.visibility = View.VISIBLE
            binding.icon.setImageDrawable(value)
        }
    }

    private fun setTitle(value: String) {
        binding.title.visibility = View.VISIBLE
        binding.title.text = value
    }

    private fun setSubtitle(value: String) {
        binding.subtitle.visibility = View.VISIBLE
        binding.subtitle.text = value
    }

    private fun setOkButton(name: String, listener: OnClickListener) {
        binding.okButton.visibility = View.VISIBLE
        binding.okButton.text = name
        binding.okButton.setOnClickListener {
            listener.onClick(this)
        }
    }

    private fun setCancelButton(name: String, listener: OnClickListener) {
        binding.cancelButton.visibility = View.VISIBLE
        binding.cancelButton.text = name
        binding.cancelButton.setOnClickListener {
            listener.onClick(this)
        }
    }

    private fun setOtherButton(name: String, listener: OnClickListener) {
        binding.otherButton.visibility = View.VISIBLE
        binding.otherButton.text = name
        binding.otherButton.setOnClickListener {
            listener.onClick(this)
        }
    }

    private fun setIconVisibility(isVisible: Boolean) {
        binding.icon.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setOkButtonIcon(okIcon: Drawable, okIconDirection: Int) {
        binding.okButton.icon = okIcon
        binding.okButton.iconGravity = okIconDirection
    }

    private fun setCloseButtonVisibility(isVisible: Boolean) {
        binding.closeDialog.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    class Builder constructor(val context: Context) {
        var param = AppDialogParam(context)

        fun setIcon(@DrawableRes iconId: Int): Builder {
            param.iconId = iconId
            return this
        }

        fun setIcon(icon: Drawable): Builder {
            param.icon = icon
            return this
        }

        fun setTitle(@StringRes title: Int): Builder {
            param.title = param.context.getText(title).toString()
            return this
        }

        fun setTitle(title: String): Builder {
            param.title = title
            return this
        }

        fun setSubtitle(@StringRes subtitle: Int): Builder {
            param.subtitle = param.context.getText(subtitle).toString()
            return this
        }

        fun setSubtitle(subtitle: String): Builder {
            param.subtitle = subtitle
            return this
        }

        inline fun setOkButton(@StringRes textId: Int, crossinline listener: (AlertDialog) -> Unit): Builder {
            return setOkButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setOkButton(buttonName: String, crossinline listener: (AlertDialog) -> Unit): Builder {
            param.okButtonText = buttonName
            param.mOkButtonListener = object : OnClickListener {
                override fun onClick(dialog: AlertDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        inline fun setCancelButton(@StringRes textId: Int, crossinline listener: (AlertDialog) -> Unit): Builder {
            return setCancelButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setCancelButton(buttonName: String, crossinline listener: (AlertDialog) -> Unit): Builder {
            param.cancelButtonText = buttonName
            param.mCancelButtonListener = object : OnClickListener {
                override fun onClick(dialog: AlertDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        inline fun setOtherButton(@StringRes textId: Int, crossinline listener: (AlertDialog) -> Unit): Builder {
            return setOtherButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setOtherButton(buttonName: String, crossinline listener: (AlertDialog) -> Unit): Builder {
            param.otherButtonText = buttonName
            param.mOtherButtonListener = object : OnClickListener {
                override fun onClick(dialog: AlertDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        fun setIconVisibility(isVisible: Boolean): Builder {
            param.isIconVisible = isVisible
            return this
        }

        fun setOkButtonIcon(iconId: Drawable, iconDirection: Int): Builder {
            param.okButtonIcon = iconId
            param.okIconDirection = iconDirection
            return this
        }

        fun setCloseButtonVisibility(isVisible: Boolean): Builder {
            param.isCloseButtonVisible = isVisible
            return this
        }

        fun create(): AlertDialog {
            return AlertDialog(param)
        }

    }

    interface OnClickListener {
        fun onClick(dialog: AlertDialog)
    }

    class AppDialogParam constructor(val context: Context) {

        var iconId: Int? = null
        var icon: Drawable? = null
        var title: String? = null
        var subtitle: String? = null
        var okButtonText: String? = null
        var cancelButtonText: String? = null
        var otherButtonText: String? = null
        var isIconVisible: Boolean? = null
        var isCloseButtonVisible: Boolean? = true

        var okButtonIcon: Drawable? = null
        var okIconDirection: Int? = null

        var mOkButtonListener: OnClickListener? = null
        var mCancelButtonListener: OnClickListener? = null
        var mOtherButtonListener: OnClickListener? = null

        fun apply(dialog: AlertDialog) {
            iconId?.let {
                dialog.setIcon(it)
            }

            if (icon != null)
                dialog.setIcon(icon!!)

            if (title != null)
                dialog.setTitle(title!!)

            if (subtitle != null)
                dialog.setSubtitle(subtitle!!)

            if (okButtonText != null)
                dialog.setOkButton(okButtonText!!, mOkButtonListener!!)

            if (cancelButtonText != null)
                dialog.setCancelButton(cancelButtonText!!, mCancelButtonListener!!)

            if (otherButtonText != null)
                dialog.setOtherButton(otherButtonText!!, mOtherButtonListener!!)

            if (isIconVisible != null)
                dialog.setIconVisibility(isIconVisible!!)

            if (okButtonIcon != null && okIconDirection != null)
                dialog.setOkButtonIcon(okButtonIcon!!, okIconDirection!!)

            if (isCloseButtonVisible != null)
                dialog.setCloseButtonVisibility(isCloseButtonVisible!!)

        }
    }
}

