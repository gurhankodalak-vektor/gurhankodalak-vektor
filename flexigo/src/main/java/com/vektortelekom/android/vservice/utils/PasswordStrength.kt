package com.vektortelekom.android.vservice.utils

import android.graphics.Color
import com.vektortelekom.android.vservice.R

enum class PasswordStrength(color: Int){
    NONE(Color.RED),
    WEAK(Color.RED),
    MEDIUM(R.color.orangeYellow),
    STRONG(Color.YELLOW),
    VERY_STRONG(Color.GREEN);

    var color: Int = 0
        internal set

    init {
        this.color = color
    }

    companion object{

        private var REQUIRED_LENGTH = 6
        private var REQUIRE_DIGITS = true
        private var REQUIRE_LOWER_CASE = true
        private var REQUIRE_UPPER_CASE = true

     fun calculateStrength(password: String): PasswordStrength{
         var currentScore = 0
         var sawUpper = false
         var sawLower = false
         var sawDigit = false
         var sawSpecial = false

         for (element in password) {

             if (!sawSpecial && !Character.isLetterOrDigit(element)) {
                 sawSpecial = true
             } else {
                 if (!sawDigit && Character.isDigit(element)) {
                     sawDigit = true
                 } else {
                     if (!sawUpper || !sawLower) {
                         if (Character.isUpperCase(element))
                             sawUpper = true
                         else
                             sawLower = true
                     }
                 }
             }

         }

         if (password.isNotEmpty()){
             if (REQUIRE_UPPER_CASE && sawUpper)
                 currentScore += 1
             if (REQUIRE_LOWER_CASE && sawLower)
                 currentScore += 1
             if (REQUIRE_DIGITS && sawDigit)
                 currentScore += 1
             if (password.length >= REQUIRED_LENGTH)
                 currentScore += 1
         }


         when (currentScore) {
             1 -> return WEAK
             2 -> return MEDIUM
             3 -> return STRONG
             4 -> return VERY_STRONG
         }

         return NONE
     }
    }

}