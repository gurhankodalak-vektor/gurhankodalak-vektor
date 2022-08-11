package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.vshare_api_ktx.model.DamageRegion
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ReservationWorkFlowType
import com.vektortelekom.android.vservice.databinding.PoolCarRentalFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poi.gasstation.GasStationActivity
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertMillisecondsToMinutesSeconds
import com.vektortelekom.android.vservice.utils.convertMinutesToDayText
import java.util.*
import javax.inject.Inject

class PoolCarRentalFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarRentalFragmentBinding

    private var startRentalTime: Long?= null

    var timer: Timer? = null

    var handler: Handler? = Handler()

    private var trackRunnable: Runnable = object : Runnable {
        override fun run() {
            viewModel.getRentalBillInfo(viewModel.rental.value?.id!!.toLong())
            handler?.postDelayed(this, 30_000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarRentalFragmentBinding>(inflater, R.layout.pool_car_rental_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarRentalFragment
            viewModel = this@PoolCarRentalFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.rental.observe(viewLifecycleOwner) { rental ->

            startRentalTime = rental.startDate.convertBackendDateToLong()

            timer?.cancel()

            timer = Timer()

            timer?.schedule(getTimerTask(), 0, 1000)

            binding.textViewTotalDistance.text = getString(R.string.total_km, rental.km ?: 0.0)

            binding.textViewTotalTime.text = rental.minute.convertMinutesToDayText(requireContext())

            binding.textViewTotalPrice.text = getString(R.string.total_price, rental.billInfo?.total
                    ?: 0.0)

            viewModel.getVehicleDamages()

        }

        viewModel.vehicleDamages.observe(viewLifecycleOwner) { damages ->

            var count = 0

            for (damage in damages) {
                when (damage.region2) {
                    DamageRegion.FRONT_INTERIOR, DamageRegion.REAR_INTERIOR -> {
                        count += damage.fileUuids?.size ?: 0
                    }
                    else -> {

                    }
                }
            }

            binding.textViewDamageCount.text = count.toString()

        }

        binding.buttonGasStation.setOnClickListener {
            val intent = Intent(requireActivity(), GasStationActivity::class.java)
            intent.putExtra("type", "gas")
            startActivity(intent)
        }

        binding.buttonStations.setOnClickListener {
            val intent = Intent(requireActivity(), GasStationActivity::class.java)
            intent.putExtra("type", "station")
            startActivity(intent)
        }

        binding.buttonFinishRental.setOnClickListener {
            if (viewModel.rental.value?.workflowType == ReservationWorkFlowType.LIGHT) {
                viewModel.navigator?.showPoolCarDirectorUsageFinishFragment(null)
            }
            else {
                viewModel.navigator?.showPoolCarRentalFinishControlFragment(null)
            }
        }

        viewModel.rentalBillInfoResponse.observe(viewLifecycleOwner) { billInfoModel ->

            binding.textViewTotalDistance.text = getString(R.string.total_km, billInfoModel.km
                    ?: 0.0)

            binding.textViewTotalTime.text = billInfoModel.minutes?.toInt().convertMinutesToDayText(requireContext())

            //binding.textViewTotalPrice.text = getString(R.string.total_price, rental.billInfo?.total?:0.0)

        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        timer?.cancel()

        timer = Timer()

        timer?.schedule(getTimerTask(), 0, 1000)

        handler?.removeCallbacks(trackRunnable)
        handler?.postDelayed(trackRunnable, 300)
    }

    override fun onDetach() {
        timer?.cancel()
        handler?.removeCallbacks(trackRunnable)
        super.onDetach()
    }

    private fun getTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                if(startRentalTime == null) {
                    return
                }

                val timeDiff = startRentalTime!! + 1000*60*5 - System.currentTimeMillis()

                if(timeDiff > 0) {
                    handler?.post {
                        binding.textViewRemainingTime.text = timeDiff.convertMillisecondsToMinutesSeconds()
                    }
                }
                else {
                    handler?.post {
                        binding.layoutInternalDamage.visibility = View.GONE
                        timer?.cancel()
                    }
                }
            }

        }
    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarRentalFragment"

        fun newInstance() = PoolCarRentalFragment()

    }

}