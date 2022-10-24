package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.databinding.CarpoolRiderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolAdapter
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import javax.inject.Inject

class CarPoolRiderFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolRiderFragmentBinding

    var adapter: CarPoolAdapter? = null
    var matchedAdapter: CarPoolMatchedAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolRiderFragmentBinding>(inflater, R.layout.carpool_rider_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolRiderFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CarPoolAdapter("riders", object: CarPoolAdapter.CarPoolSwipeListener{
            override fun onDislikeSwipe(item: CarPoolListModel) {
                Toast.makeText(requireContext(), "onDislikeSwipe", Toast.LENGTH_SHORT).show()
            }

            override fun onLikeSwipe(item: CarPoolListModel) {
                Toast.makeText(requireContext(), "onLikeSwipe", Toast.LENGTH_SHORT).show()

            }

        })

        matchedAdapter = CarPoolMatchedAdapter("riders")

        viewModel.isRider.observe(viewLifecycleOwner){
            adapter!!.setIsRider(it)
        }

        viewModel.carPoolPreferences.observe(viewLifecycleOwner){
            if (it == null)
                adapter!!.isOnlyReadMode(true)
            else
                adapter!!.isOnlyReadMode(false)
        }

        viewModel.closeRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerviewRiders.visibility = View.VISIBLE

                adapter!!.setList(it)
                binding.recyclerviewRiders.adapter = adapter
            } else{
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerviewRiders.visibility = View.GONE

            }
        }

        viewModel.matchedRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.textviewRidersTitle.visibility = View.VISIBLE
                binding.textviewMatchedRidersTitle.visibility = View.VISIBLE
                binding.imageviewOrangeCircle.visibility = View.VISIBLE

                matchedAdapter!!.setList(it)
                binding.recyclerviewMatchedRiders.adapter = matchedAdapter
            } else{

                binding.textviewRidersTitle.visibility = View.GONE
                binding.textviewMatchedRidersTitle.visibility = View.GONE
                binding.imageviewOrangeCircle.visibility = View.GONE

            }
        }

    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolRiderFragment"
        fun newInstance() = CarPoolRiderFragment()
    }


}