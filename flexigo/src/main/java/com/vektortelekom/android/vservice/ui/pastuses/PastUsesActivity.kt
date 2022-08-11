package com.vektortelekom.android.vservice.ui.pastuses

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PastUsesActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.pastuses.fragment.PastUsesMainFragment
import javax.inject.Inject

class PastUsesActivity: BaseActivity<PastUsesViewModel>(), PastUsesNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PastUsesViewModel

    private lateinit var binding: PastUsesActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<PastUsesActivityBinding>(this, R.layout.past_uses_activity).apply {
            lifecycleOwner = this@PastUsesActivity
            viewModel = this@PastUsesActivity.viewModel
        }

        viewModel.navigator = this

        showPastUsesMainFragment(null)

    }

    override fun getViewModel(): PastUsesViewModel {
        viewModel = ViewModelProvider(this, factory)[PastUsesViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun showPastUsesMainFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PastUsesMainFragment.newInstance(), PastUsesMainFragment.TAG)
                .commit()
    }

}