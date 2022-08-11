package com.vektortelekom.android.vservice.ui.login.fragment

import androidx.fragment.app.FragmentFactory

class LoginFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

            when (className) {
                LoginFragment::class.java.name -> LoginFragment()
                ForgotPasswordFragment::class.java.name -> ForgotPasswordFragment()
                else -> super.instantiate(classLoader, className)
            }


}