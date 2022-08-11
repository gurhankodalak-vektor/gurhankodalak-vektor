package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DeviceType
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarFindCarFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.ImageZoomDialog
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertMillisecondsToMinutesSeconds
import java.util.*
import javax.inject.Inject

class PoolCarFindCarFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarFindCarFragmentBinding

    private var createRentalTime: Long?= null

    var timer: Timer? = null

    var handler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarFindCarFragmentBinding>(inflater, R.layout.pool_car_find_car_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarFindCarFragment
            viewModel = this@PoolCarFindCarFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        viewModel.selectedStation.observe(viewLifecycleOwner) { park ->
            binding.textViewCarPark.text = park.name
        }

        viewModel.rental.observe(viewLifecycleOwner) { rental ->

            createRentalTime = rental.creationTime.convertBackendDateToLong()

            timer?.cancel()

            timer = Timer()

            timer?.schedule(getTimerTask(), 0, 1000)

            val url: String = AppApiHelper().baseUrl2
                    .plus("/")
                    .plus("report/fileViewer/uuid/")
                    .plus(rental.locationPhotoUuid)

            val requestOptions = RequestOptions()
                    .placeholder(R.drawable.placeholder_black)
                    .error(R.drawable.placeholder_black)

            GlideApp.with(requireActivity()).setDefaultRequestOptions(requestOptions).load(url).into(binding.imageViewCarPhoto)

        }

        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->

            binding.textViewCarLocation.text = vehicle.location?.address
            binding.textViewPlate.text = vehicle.plate

            if (vehicle.deviceType != DeviceType.REMOTE_DOOR) {
                binding.viewDivider3.visibility = View.VISIBLE
                binding.textViewVehicleKey.visibility = View.VISIBLE
            }


        }

        binding.imageViewCarPhoto.setOnClickListener {

            viewModel.rental.value?.locationPhotoUuid?.let { uuid ->

                val imageZoomDialog = ImageZoomDialog(requireContext(), AppApiHelper(), uuid, false)
                imageZoomDialog.show()

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

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarFindCarFragment"

        fun newInstance() = PoolCarFindCarFragment()

    }

}