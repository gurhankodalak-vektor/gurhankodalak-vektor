package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektor.vshare_api_ktx.model.DamageRegion
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarInternalDamageControlFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.adapter.DamagePhotosAdapter
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertMillisecondsToMinutesSeconds
import com.vektortelekom.android.vservice.utils.dpToPx
import java.util.*
import javax.inject.Inject

class PoolCarInternalDamageControlFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarInternalDamageControlFragmentBinding

    private var createRentalTime: Long?= null

    var timer: Timer? = null

    var handler: Handler? = null

    private var frontDamage: DamageModel? = null
    private var rearDamage: DamageModel? = null

    private var frontDamageAdapter: DamagePhotosAdapter? = null
    private var rearDamageAdapter: DamagePhotosAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarInternalDamageControlFragmentBinding>(inflater, R.layout.pool_car_internal_damage_control_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarInternalDamageControlFragment
            viewModel = this@PoolCarInternalDamageControlFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        viewModel.getVehicleDamages()

        viewModel.rental.observe(viewLifecycleOwner) { rental ->

            createRentalTime = rental.startDate.convertBackendDateToLong()

            timer?.cancel()

            timer = Timer()

            timer?.schedule(getTimerTask(), 0, 1000)
        }

        viewModel.vehicleDamages.observe(viewLifecycleOwner) { damages ->

            frontDamage = null
            rearDamage = null

            if (damages.isEmpty()) {
                binding.textViewNoDamage.visibility = View.VISIBLE
            } else {
                binding.textViewNoDamage.visibility = View.GONE
                for (damage in damages) {
                    damageAdded(damage)
                }
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

                val timeDiff = createRentalTime!! + 1000*60*5 - System.currentTimeMillis()

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
            DamageRegion.FRONT_INTERIOR -> {
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
            DamageRegion.REAR_INTERIOR -> {
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
        const val TAG: String = "PoolCarInternalDamageControlFragment"

        fun newInstance() = PoolCarInternalDamageControlFragment()

    }

}