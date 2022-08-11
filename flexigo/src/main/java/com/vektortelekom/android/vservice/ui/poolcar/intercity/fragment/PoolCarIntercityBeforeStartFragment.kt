package com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarIntercityBeforeStartFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityViewModel
import javax.inject.Inject

class PoolCarIntercityBeforeStartFragment: BaseFragment<PoolCarIntercityViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarIntercityViewModel

    lateinit var binding: PoolCarIntercityBeforeStartFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarIntercityBeforeStartFragmentBinding>(inflater, R.layout.pool_car_intercity_before_start_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarIntercityBeforeStartFragment
            viewModel = this@PoolCarIntercityBeforeStartFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isStartAgreementApproved.observe(viewLifecycleOwner) {
            binding.startPersonalRental.backgroundTintList = if (it) ContextCompat.getColorStateList(requireContext(), R.color.purpley) else ContextCompat.getColorStateList(requireContext(), R.color.paleGrey4)
        }

        val agreementText = getString(R.string.rental_start_check_box_text)
        val ss = SpannableString(agreementText)
        val preliminaryInformationText = getString(R.string.vehicle_rules_form)
        val eulaText = " ${getString(R.string.vehicle_speed_rules_form)}"
        val firstIndex = agreementText.indexOf(preliminaryInformationText)
        val secondIndex = agreementText.indexOf(eulaText)

        val preliminarySpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

                viewModel.navigator?.showVehicleRulesFragment(null)

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(context!!, R.color.colorBlack)
            }
        }

        val eulaSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

                viewModel.navigator?.showVehicleSpeedRulesFragment(null)

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(context!!, R.color.colorBlack)
            }
        }
        ss.setSpan(preliminarySpan, firstIndex, firstIndex + preliminaryInformationText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(eulaSpan, secondIndex, secondIndex + eulaText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.agreementApproveText.text = ss
        binding.agreementApproveText.movementMethod = LinkMovementMethod.getInstance()
        binding.agreementApproveText.highlightColor = Color.TRANSPARENT

    }

    override fun getViewModel(): PoolCarIntercityViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarIntercityViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarIntercityBeforeStartFragment"

        fun newInstance() = PoolCarIntercityBeforeStartFragment()

    }

}