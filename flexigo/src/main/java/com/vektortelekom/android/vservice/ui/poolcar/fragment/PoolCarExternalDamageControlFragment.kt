package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektor.vshare_api_ktx.model.DamageRegion
import com.vektor.vshare_api_ktx.model.DoorStatus
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DeviceType
import com.vektortelekom.android.vservice.databinding.PoolCarExternalDamageControlFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.adapter.DamagePhotosAdapter
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningDialog
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningNoDeviceDialog
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertMillisecondsToMinutesSeconds
import com.vektortelekom.android.vservice.utils.dpToPx
import org.joda.time.DateTime
import org.joda.time.Seconds
import java.util.*
import javax.inject.Inject

class PoolCarExternalDamageControlFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding:PoolCarExternalDamageControlFragmentBinding

    private var createRentalTime: Long?= null

    var timer: Timer? = null

    var handler: Handler? = null

    private var frontDamage: DamageModel? = null
    private var rearDamage: DamageModel? = null
    private var rightDamage: DamageModel?= null
    private var leftDamage: DamageModel? = null

    private var frontDamageAdapter: DamagePhotosAdapter? = null
    private var rearDamageAdapter: DamagePhotosAdapter? = null
    private var rightDamageAdapter: DamagePhotosAdapter? = null
    private var leftDamageAdapter: DamagePhotosAdapter? = null

    private val maxDuration: Long = 90_000
    private var startTime: DateTime? = null
    private var watchDuration: Long = maxDuration
    private var isOperationFailedWithTimeout: Boolean = false
    private var isWatcherTimerStarted: Boolean = false
    private var watcherTimer: CountDownTimer? = null
    private var requestTimer: CountDownTimer? = null

    private var doorsOpeningDialog: DoorsOpeningDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarExternalDamageControlFragmentBinding>(inflater, R.layout.pool_car_external_damage_control_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarExternalDamageControlFragment
            viewModel = this@PoolCarExternalDamageControlFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        viewModel.getVehicleDamages()

        viewModel.rental.observe(viewLifecycleOwner) { rental ->

            createRentalTime = rental.creationTime.convertBackendDateToLong()

            timer?.cancel()

            timer = Timer()

            timer?.schedule(getTimerTask(), 0, 1000)
        }

        viewModel.vehicleDamages.observe(viewLifecycleOwner) { damages ->

            frontDamage = null
            rearDamage = null
            rightDamage = null
            leftDamage = null

            if (damages.isEmpty()) {
                binding.textViewNoDamage.visibility = View.VISIBLE
            } else {
                binding.textViewNoDamage.visibility = View.GONE
                for (damage in damages) {
                    damageAdded(damage)
                }
            }

        }

        viewModel.startRentalResponse.observe(viewLifecycleOwner) {

            if (viewModel.selectedVehicle.value?.deviceType == DeviceType.REMOTE_DOOR) {
                startTime = DateTime.now()
                watchDuration = maxDuration
                checkDoorStatus()
                if (doorsOpeningDialog == null) {
                    doorsOpeningDialog = DoorsOpeningDialog(requireContext(), true)
                    doorsOpeningDialog?.show()
                }
            } else {
                val dialog = DoorsOpeningNoDeviceDialog(requireContext(), true)
                dialog.show()

                handler?.postDelayed({
                    dialog.dismiss()
                    viewModel.navigator?.showRentalFragment(null)
                }, 6000)
            }


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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        timer?.cancel()

        timer = Timer()

        timer?.schedule(getTimerTask(), 0, 1000)
    }

    override fun onDetach() {
        timer?.cancel()
        super.onDetach()
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

    private fun getTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                if(createRentalTime == null) {
                    return
                }

                val timeDiff = createRentalTime!! + 1000*60*15 - System.currentTimeMillis()

                if(timeDiff > 0) {
                    handler?.post {
                        binding.textViewRemainingTime.text = timeDiff.convertMillisecondsToMinutesSeconds()
                    }
                }
                else {
                    handler?.post {
                        binding.textViewRemainingTime.text = getString(R.string.rental_started)
                        timer?.cancel()
                    }
                }
            }

        }
    }

    fun damageAdded(damage: DamageModel) {
        when(damage.region2) {
            DamageRegion.FRONT -> {
                if(frontDamage == null) {
                    frontDamage = damage
                    frontDamageAdapter = DamagePhotosAdapter(damage)
                    binding.recyclerViewFrontImages.adapter = frontDamageAdapter
                    binding.recyclerViewFrontImages.addItemDecoration(getItemDecoration(damage))
                    binding.textViewFront.visibility = View.VISIBLE
                    binding.recyclerViewFrontImages.visibility = View.VISIBLE
                }
                else {
                    damage.fileUuids?.let { uuids ->
                        frontDamage?.fileUuids?.addAll(uuids)
                    }
                    binding.recyclerViewFrontImages.adapter?.notifyDataSetChanged()
                }
            }
            DamageRegion.REAR -> {
                if(rearDamage == null) {
                    rearDamage = damage
                    rearDamageAdapter = DamagePhotosAdapter(damage)
                    binding.recyclerViewRearImages.adapter = rearDamageAdapter
                    binding.recyclerViewRearImages.addItemDecoration(getItemDecoration(damage))
                    binding.textViewRear.visibility = View.VISIBLE
                    binding.recyclerViewRearImages.visibility = View.VISIBLE
                }
                else {
                    damage.fileUuids?.let { uuids ->
                        rearDamage?.fileUuids?.addAll(uuids)
                    }
                    binding.recyclerViewRearImages.adapter?.notifyDataSetChanged()
                }
            }
            DamageRegion.RIGHT -> {
                if(rightDamage == null) {
                    rightDamage = damage
                    rightDamageAdapter = DamagePhotosAdapter(damage)
                    binding.recyclerViewRightImages.adapter = rightDamageAdapter
                    binding.recyclerViewRightImages.addItemDecoration(getItemDecoration(damage))
                    binding.textViewRight.visibility = View.VISIBLE
                    binding.recyclerViewRightImages.visibility = View.VISIBLE
                }
                else {
                    damage.fileUuids?.let { uuids ->
                        rightDamage?.fileUuids?.addAll(uuids)
                    }
                    binding.recyclerViewRightImages.adapter?.notifyDataSetChanged()
                }
            }
            DamageRegion.LEFT -> {
                if(leftDamage == null) {
                    leftDamage = damage
                    leftDamageAdapter = DamagePhotosAdapter(damage)
                    binding.recyclerViewLeftImages.adapter = leftDamageAdapter
                    binding.recyclerViewLeftImages.addItemDecoration(getItemDecoration(damage))
                    binding.textViewLeft.visibility = View.VISIBLE
                    binding.recyclerViewLeftImages.visibility = View.VISIBLE
                }
                else {
                    damage.fileUuids?.let { uuids ->
                        leftDamage?.fileUuids?.addAll(uuids)
                    }
                    binding.recyclerViewLeftImages.adapter?.notifyDataSetChanged()
                }
            }
            else -> {

            }
        }
    }

    private fun getItemDecoration(damage: DamageModel): RecyclerView.ItemDecoration {
        return object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)

                with(outRect) {
                    left = if (parent.getChildAdapterPosition(view) == 0) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
                    }
                    right = if (parent.getChildAdapterPosition(view) == (damage.fileUuids?.size?:0) -1) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
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
        const val TAG: String = "PoolCarExternalDamageControlFragment"

        fun newInstance() = PoolCarExternalDamageControlFragment()

    }

}