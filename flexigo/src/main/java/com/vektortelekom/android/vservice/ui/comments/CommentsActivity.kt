package com.vektortelekom.android.vservice.ui.comments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CommentsActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsAddFragment
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsMainFragment
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsPhotoPreviewFragment
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import javax.inject.Inject

class CommentsActivity : BaseActivity<CommentsViewModel>(), CommentsNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CommentsViewModel

    private lateinit var binding: CommentsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<CommentsActivityBinding>(this, R.layout.comments_activity).apply {
            lifecycleOwner = this@CommentsActivity
            viewModel = this@CommentsActivity.viewModel
        }
        viewModel.navigator = this

        showCommentsMainFragment(null)
        viewModel.getTickets(getString(R.string.generic_language))

    }

    override fun getViewModel(): CommentsViewModel {
        viewModel = ViewModelProvider(this, factory)[CommentsViewModel::class.java]
        return viewModel
    }

    override fun showCommentsMainFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, CommentsMainFragment.newInstance(), CommentsMainFragment.TAG)
                .commit()
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun showAddCommentFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, CommentsAddFragment.newInstance(), CommentsAddFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPhotoPreviewFragment(view: View?) {
        binding.layoutToolbar.visibility = View.GONE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, CommentsPhotoPreviewFragment.newInstance(), CommentsPhotoPreviewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    override fun closePhotoPreviewFragment(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        onBackPressed()
    }

    override fun takePhotoAgain(view: View?) {
        if(supportFragmentManager.findFragmentByTag(CommentsPhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(CommentsAddFragment.TAG)
        if(fragment is CommentsAddFragment) {
            fragment.takePhotoAgain()
        }
    }

    override fun useTakenPhoto(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        if(supportFragmentManager.findFragmentByTag(CommentsPhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }
        val fragment = supportFragmentManager.findFragmentByTag(CommentsAddFragment.TAG)
        if(fragment is CommentsAddFragment) {
            fragment.addNewPhoto()
        }
    }

    override fun returnCommentsMainFragment(view: View?) {
        viewModel.getTickets()
        onBackPressed()
    }

}