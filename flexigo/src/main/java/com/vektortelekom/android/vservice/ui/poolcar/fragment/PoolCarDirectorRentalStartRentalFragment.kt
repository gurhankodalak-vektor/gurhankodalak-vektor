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
import com.vektortelekom.android.vservice.databinding.PoolcarDirectorStartRentalFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningDialog
import org.joda.time.DateTime
import org.joda.time.Seconds
import javax.inject.Inject

class PoolCarDirectorRentalStartRentalFragment: BaseFragment<PoolCarViewModel>(){
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolcarDirectorStartRentalFragmentBinding

    private val maxDuration: Long = 90_000
    private var startTime: DateTime? = null
    private var watchDuration: Long = maxDuration
    private var isOperationFailedWithTimeout: Boolean = false
    private var isWatcherTimerStarted: Boolean = false
    private var watcherTimer: CountDownTimer? = null
    private var requestTimer: CountDownTimer? = null

    private var doorsOpeningDialog: DoorsOpeningDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<PoolcarDirectorStartRentalFragmentBinding>(inflater, R.layout.poolcar_director_start_rental_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarDirectorRentalStartRentalFragment

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.createRental(binding.root)

        viewModel.createRentalResponse.observe(viewLifecycleOwner) {
            viewModel.startRental(requireContext())
        }
        viewModel.startRentalResponse.observe(viewLifecycleOwner) {
            if (doorsOpeningDialog == null) {
                doorsOpeningDialog = DoorsOpeningDialog(requireContext(), true)
            }
            doorsOpeningDialog?.show()
            viewModel.checkDoorStatus()
        }

        viewModel.checkDoorResponse.observe(viewLifecycleOwner, Observer { response ->
            if(response == null) {
                return@Observer
            }

            if (response.status == DoorStatus.OPEN.toString()) {
                watcherTimer?.cancel()
                isWatcherTimerStarted = false
                var diff = 3000
                startTime?.let {
                    val time = it.plusSeconds(3)
                    diff = Seconds.secondsBetween(DateTime.now(), time).seconds * 1000
                    AppLogger.d("diff: $diff")
                }
                if (diff >= 1000) {
                    viewModel.checkDoorResponse.value = null
                    Handler().postDelayed({
                        doorsOpeningDialog?.dismiss()
                        viewModel.navigator?.showRentalFragment(null)
                    }, diff.toLong())
                } else {
                    viewModel.checkDoorResponse.value = null
                    doorsOpeningDialog?.dismiss()
                    viewModel.navigator?.showRentalFragment(null)
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
        const val TAG: String = "PoolCarDirectorRentalStartRentalFragment"

        fun newInstance() = PoolCarDirectorRentalStartRentalFragment()

    }
}