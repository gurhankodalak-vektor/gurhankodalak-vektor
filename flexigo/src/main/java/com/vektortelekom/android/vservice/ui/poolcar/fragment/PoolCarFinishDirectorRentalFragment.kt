package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.DoorStatus
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DeviceType
import com.vektortelekom.android.vservice.databinding.PoolCarFinishDirectorRentalFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningDialog
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningNoDeviceDialog
import org.joda.time.DateTime
import org.joda.time.Seconds
import javax.inject.Inject

class PoolCarFinishDirectorRentalFragment: BaseFragment<PoolCarViewModel>(){
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarFinishDirectorRentalFragmentBinding

    private val maxDuration: Long = 90_000
    private var startTime: DateTime? = null
    private var watchDuration: Long = maxDuration
    private var isOperationFailedWithTimeout: Boolean = false
    private var isWatcherTimerStarted: Boolean = false
    private var watcherTimer: CountDownTimer? = null
    private var requestTimer: CountDownTimer? = null

    private var doorsOpeningDialog: DoorsOpeningDialog? = null
    var handler: Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<PoolCarFinishDirectorRentalFragmentBinding>(inflater, R.layout.pool_car_finish_director_rental_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarFinishDirectorRentalFragment

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        viewModel.finishRental(requireContext())

        viewModel.finishRentalResponse.observe(viewLifecycleOwner) {
            if (viewModel.selectedVehicle.value?.deviceType == DeviceType.REMOTE_DOOR) {
                startTime = DateTime.now()
                watchDuration = maxDuration
                checkDoorStatus()
                if (doorsOpeningDialog == null) {
                    doorsOpeningDialog = DoorsOpeningDialog(requireContext(), false)
                    doorsOpeningDialog?.show()
                }
            } else {
                val dialog = DoorsOpeningNoDeviceDialog(requireContext(), false)
                dialog.show()

                handler?.postDelayed({
                    dialog.dismiss()
                    viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                }, 6000)
            }
        }

        viewModel.checkDoorResponse.observe(viewLifecycleOwner, Observer { response ->

            if(response == null) {
                return@Observer
            }

            if (response.status == DoorStatus.CLOSED.toString()) {
                watcherTimer?.cancel()
                isWatcherTimerStarted = false
                var diff = 3000
                startTime?.let {
                    val time = it.plusSeconds(3)
                    diff = Seconds.secondsBetween(DateTime.now(), time).seconds * 1000
                    AppLogger.d("diff: $diff")
                }
                if (diff >= 1000) {
                    Handler().postDelayed({
                        doorsOpeningDialog?.dismiss()
                        viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                    }, diff.toLong())
                } else {
                    doorsOpeningDialog?.dismiss()
                    viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                }
            } else {
                requestTimer?.start()
            }
        })

        requestTimer = object : CountDownTimer(3_000, 3_000) {
            override fun onTick(l: Long) {
            }

            override fun onFinish() {
                AppLogger.d("requestTimer.onFinish")
                checkDoorStatus()
                requestTimer?.cancel()
            }
        }
    }

    private fun checkDoorStatus() {
        if (!isOperationFailedWithTimeout) {
            if (!isWatcherTimerStarted) {
                watcherTimer = object : CountDownTimer(watchDuration, 1_000) {
                    override fun onTick(l: Long) {
                        AppLogger.d("watcherTimer.onTick")
                    }

                    override fun onFinish() {
                        AppLogger.d("watcherTimer.onFinish")
                        isOperationFailedWithTimeout = true
                    }
                }
                watcherTimer?.start()
                AppLogger.d("watcherTimer?.start()")
                isWatcherTimerStarted = true
            }
            viewModel.checkDoorStatus()
        } else {
            isWatcherTimerStarted = false
            viewModel.navigator?.showNotOpenedDoorDialog()
        }
    }
    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarFinishDirectorRentalFragment"

        fun newInstance() = PoolCarFinishDirectorRentalFragment()

    }
}