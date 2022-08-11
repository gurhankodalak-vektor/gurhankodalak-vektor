package com.vektortelekom.android.vservice.ui.comments

import android.view.View
import com.vektortelekom.android.vservice.ui.base.BaseNavigator

interface CommentsNavigator: BaseNavigator {

    fun showCommentsMainFragment(view: View?)

    fun backPressed(view: View?)

    fun showAddCommentFragment(view: View?)

    fun returnCommentsMainFragment(view: View?)

    fun showPhotoPreviewFragment(view: View?)

    fun closePhotoPreviewFragment(view: View?)

    fun takePhotoAgain(view: View?)

    fun useTakenPhoto(view: View?)

    fun showMenuActivity(view: View?)

}