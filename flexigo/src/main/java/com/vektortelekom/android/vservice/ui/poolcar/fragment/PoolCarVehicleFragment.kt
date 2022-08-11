package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.bumptech.glide.request.RequestOptions
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarVehicleFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.PhotoHelper
import javax.inject.Inject

class PoolCarVehicleFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarVehicleFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarVehicleFragmentBinding>(inflater, R.layout.pool_car_vehicle_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarVehicleFragment
            viewModel = this@PoolCarVehicleFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isStartAgreementApproved.observe(viewLifecycleOwner) {
            binding.startPersonalRental.backgroundTintList = if (it) ContextCompat.getColorStateList(requireContext(), R.color.purpley) else ContextCompat.getColorStateList(requireContext(), R.color.paleGrey4)
        }

        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->

            binding.textViewCarName.text = vehicle.make.plus(" ").plus(vehicle.model)
            binding.textViewCarPark.text = viewModel.selectedStation.value?.name

            when (vehicle.transmissionType) {
                "otomatik" -> {
                    binding.textViewCarGear.text = getString(R.string.automatic)
                }
                "manuel" -> {
                    binding.textViewCarGear.text = getString(R.string.manual)
                }
                else -> {
                    binding.textViewCarGear.text = vehicle.transmissionType
                }
            }



            when (vehicle.fuelType) {
                "benzinli" -> {
                    binding.textViewCarFuel.text = getString(R.string.gasoline)
                }
                "dizel" -> {
                    binding.textViewCarFuel.text = getString(R.string.diesel)
                }
                else -> {
                    binding.textViewCarFuel.text = vehicle.fuelType
                }
            }

            if (viewModel.selectedVehicleImageUuid.isNullOrEmpty().not()) {
                val url: String = AppApiHelper().baseUrl2
                        .plus("/")
                        .plus("report/fileViewer/uuid/")
                        .plus(viewModel.selectedVehicleImageUuid)

                val requestOptions = RequestOptions()
                        .placeholder(R.drawable.placeholder_black)
                        .error(R.drawable.placeholder_black)

                GlideApp.with(requireActivity()).setDefaultRequestOptions(requestOptions).load(url).into(binding.imageViewCar)
            } else if (vehicle.imageName.isNullOrEmpty()) {
                binding.imageViewCar.setImageResource(R.drawable.ic_car_icon)
            } else {
                PhotoHelper.loadCarImageToImageViewWithCache(requireContext(), vehicle.imageName, binding.imageViewCar, false)
            }


            /*if (vehicle.imageName.isNullOrEmpty()) {
                binding.imageViewCar.setImageResource(R.drawable.ic_car_icon)
            } else {
                PhotoHelper.loadCarImageToImageViewWithCache(requireContext(), vehicle.imageName, binding.imageViewCar, false)
            }*/

        }

        viewModel.createRentalResponse.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                val dialog = FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.congratulations))
                        .setText1(getString(R.string.rental_start_successfully))
                        .setText2(getString(R.string.redirecting_to_page))
                        .create()

                dialog.show()

                Handler().postDelayed({
                    viewModel.navigator?.showFindCarFragment(null)
                    dialog.dismiss()
                }, 3000)

                viewModel.createRentalResponse.value = null
            }


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

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarVehicleFragment"

        fun newInstance() = PoolCarVehicleFragment()

    }

}