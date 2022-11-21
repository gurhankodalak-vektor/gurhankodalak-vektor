package com.vektortelekom.android.vservice.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DriverModel
import com.vektortelekom.android.vservice.data.model.VehicleModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

val patternEmail = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}".toRegex()

fun fromHtml(html: String?): Spanned? {
    return when{
        html == null ->  SpannableString("")
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        else ->Html.fromHtml(html)
    }

}

fun String?.isValidEmail(): Boolean {
    if (this == null) {
        return false
    }
    return patternEmail.matches(this)
}

fun String?.fromCamelCaseToSnakeCase(): String {
    if (this == null) {
        return ""
    }

    return this.map {
        if (it.isUpperCase()) {
            "_${it.toLowerCase()}"
        } else {
            "$it"
        }
    }
            .joinToString(separator = "")
}

fun VehicleModel?.carInfo(): String {
    if (this == null) {
        return ""
    }
    return if(make == null) {
        plateId
    } else {
        var resultString = plateId.plus(" - ").plus(make)
        if(name != null) {
            resultString = resultString.plus(" ").plus(name)
        }
        resultString
    }
}

fun DriverModel?.fullName(): String {
    return when{
        this == null -> ""
        name == null -> ""
        else -> name.plus(" ").plus(surname)
    }
}

fun Date?.convertToShuttleDayTitle() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertToTicketTime() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertToShuttleDayItem() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("EEEE, dd", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForBackend() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForTimeCompare() : Date? {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return this?.let { formatter.format(it) }?.let { formatter.parse(it) }
}

fun Date?.convertForBackend2() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(this)
}

fun Long?.convertForDate() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForTicketFullDate() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("MMMM d,yyyy, HH:mm", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForShuttleDay() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("MMMM d,yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Calendar.convertForShuttleDate() : String {

    val date = Date(timeInMillis)

    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun Calendar.convertForShuttleDay() : String {

    val date = Date(timeInMillis)

    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun longToCalendar(time: Long?): Calendar? {
    var c: Calendar? = null
    if (time != null) {
        c = Calendar.getInstance()
        c.timeInMillis = time
    }
    return c
}
fun Double.convertMetersToMile(): Double {
    return this * 0.00062137119
}

fun Date.convertToShuttleDate() : String {

    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(this)
}


fun Date?.convertForMonth() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
    return formatter.format(this)
}


fun Date?.convertForWeekDaysLiteral() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("EEEE", Locale.ENGLISH)
    return formatter.format(this)
}
fun Date?.convertForWeekDaysLocal() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForDay() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("d", Locale.getDefault())
    return formatter.format(this)
}

fun Date?.convertForDayAndMonth() : String {
    if(this == null) {
        return ""
    }
    val formatterDay = SimpleDateFormat("d", Locale.getDefault())
    val formatterMonth = SimpleDateFormat("MMM", Locale.getDefault())
    return formatterDay.format(this) + " " + formatterMonth.format(this)
}

fun Date?.convertForReservationDialog() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd''MMMM, HH:mm", Locale.getDefault())
    return formatter.format(this)
}

fun String?.isValidPhoneNumber(): Boolean {
    if(this == null) {
        return false
    }
    return try {
        val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        val phoneNumber: Phonenumber.PhoneNumber = phoneUtil.parse(this.replace(" ", "").trim(), "TR")
        phoneUtil.isValidNumber(phoneNumber)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
        false
    }
}

fun Int?.convertHourMinutes(): String? {
    return if(this == null) {
        return null
    } else {
        val startArrivalText = toString()
        if(startArrivalText.length > 2) {
            val hours = startArrivalText.substring(0, startArrivalText.length-2)
            val minutes = startArrivalText.substring(startArrivalText.length-2)
            "$hours:$minutes"
        } else {
            return  null
        }

    }
}
fun String?.convertHourMinutes(): String? {
    return if(this == null) {
        return null
    } else {
        val startArrivalText = toString()
        if(startArrivalText.length > 2) {
            val hours = startArrivalText.substring(0, startArrivalText.length-2)
            val minutes = startArrivalText.substring(startArrivalText.length-2)
            "$hours:$minutes"
        } else {
            return  null
        }

    }
}

fun Int.convertHoursAndMinutes(): String {
    val hours = this/60
    val minutes = this - hours*60
    return if(minutes < 10) {
        hours.toString().plus(":0").plus(minutes)
    }
    else {
        hours.toString().plus(":").plus(minutes)
    }

}

fun String?.convertFullDateAddTime(time: String) : String {

    val hoursAndMinutes = this?.split(" ")

    val first = hoursAndMinutes?.get(0).toString()

    return first.plus(" ").plus(time).plus(":00")
}

fun Long?.convertNowToTotalMinutesOfDay() : Int {
    if(this == null) {
        return 0
    }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val format = formatter.format(Date(this))

    val hoursAndMinutes = format.split(":")

    return hoursAndMinutes[0].toInt()*60 + hoursAndMinutes[1].toInt()
}

fun Long?.convertISO8601String() : String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000000", Locale.getDefault())

    return formatter.format(Date(this))
}

fun String?.convertISO8601toTotalMinutesOfDay(): Int {
    if(this == null) {
        return 0
    }
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).parse(this)
    date?.let {
        return it.time.convertNowToTotalMinutesOfDay()
    }
    return 0
}

fun String?.convertISO8601toLong(): Long {
    if(this == null) {
        return 0
    }
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).parse(this)
    date?.let {
        return it.time
    }
    return 0
}

fun Long.getHourOfTimestamp() : Int {
    val formatter = SimpleDateFormat("HH", Locale.getDefault())
    val format = formatter.format(Date(this))

    return format.toInt()
}

fun String?.convertBackendDateToLong(): Long? {
    if(this == null) {
        return null
    }
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(this)
    return dateFormatter.toDate().time
}

fun String.convertBackendDateToLong(): Long {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(this)
    return dateFormatter.toDate().time
}

fun String.convertBackendDateToDate(): Date {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(this)
    return dateFormatter.toDate()
}

fun String?.convertFromBackendToLong(): Long? {
    if(this == null) {
        return null
    }
    val dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy").parseDateTime(this)
    return dateFormatter.toDate().time
}

fun String?.convertBackendDateToReservationString(): String? {
    if(this == null) {
        return null
    }
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(it)
    }
    return null
}

fun String?.convertBackendDateToMMMddyyyyFormat(): String? {
    if(this == null) {
        return null
    }
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
    }
    return null
}

fun String?.convertBackendDateToShuttleReservationString(): String? {
    if(this == null) {
        return null
    }
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
    }
    return null
}

fun String.convertBackendDateToShuttleChangeString(): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(it)
    }
    return ""
}

fun String?.convertBackendDateToIntercityString(): String? {
    if(this == null) {
        return null
    }
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("dd MM yyyy HH:mm", Locale.getDefault()).format(it)
    }
    return null
}

fun String?.convertBackendDateToHourMin(): String? {
    if(this == null) {
        return null
    }
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    date?.let {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    }
    return null
}

fun String?.formatForService(): String {
    val formatterView = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val formatterService = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val date = formatterView.parse(this!!)
    return formatterService.format(date!!)
}

fun String?.formatForService2(): String {

    val formatterView = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val formatterService = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val date = formatterView.parse(this!!)
    return formatterService.format(date!!)
}

fun Long?.convertMillisecondsToMinutesSeconds(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(this))
}

fun Long?.convertBackendDateToNotificationDate(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("MMMM dd - HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(this))
}

fun Int?.convertMinutesToDayText(context: Context): String {
    if(this == null) {
        return ""
    }

    val hours = this/60
    val days = hours/1440
    val minute = this%60

    return if(days > 0) {
        context.getString(R.string.date_day_hour_min, days, hours, minute)
    } else {
        context.getString(R.string.date_hour_min, hours, minute)
    }

}

fun Float.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).roundToInt()
}

fun DateTime.formatNow(): String {

    val formatter = SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault())
    return formatter.format(toDate())
}

fun String?.isOlderThanYear(years: Int): Boolean {

    if(this == null) {
        return false
    }

    val formatterView = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    val date = formatterView.parse(this)
    return DateTime(date).plusYears(years).millis < Date().time
}


fun Date.getDayWithoutHoursAndMinutesAsLong(): Long  {
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val dateString = formatter.format(this)

    val dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(dateString)
    return dateFormatter.toDate().time

}

fun Long?.convertToShuttleTime(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long?.convertToShuttleDate(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.getDateWithZeroHour(): Long {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(this))

    val dateFormatter = DateTimeFormat.forPattern("dd MMMM yyyy").parseDateTime(dateString)
    return dateFormatter.toDate().time

}

fun Long?.convertToShuttleDateTime(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * April 25th, 2012**
 */
fun Date.getCustomDateStringEN(withYear: Boolean, withComma: Boolean): String {
    var tmp = SimpleDateFormat("MMMM d")
    var str = tmp.format(this)
    str = str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)

    if (withComma){
        str = str.plus(", ")
//        str = if (this.date in 11..13) str + "th, "
//        else {
//            if (str.endsWith("1")) str + "st, "
//            else if (str.endsWith("2")) str + "nd, "
//            else if (str.endsWith("3")
//            ) str + "rd, " else str + "th, "
//        }
    } else
    {
        str = str.plus(" ")
//        str = if (this.date in 11..13) str + "th "
//        else {
//            if (str.endsWith("1")) str + "st "
//            else if (str.endsWith("2")) str + "nd "
//            else if (str.endsWith("3")
//            ) str + "rd " else str + "th "
//        }
    }

    if (withYear){
        tmp = SimpleDateFormat("yyyy")
        str += tmp.format(this)
    }

    return str
}
fun Date.getCustomDateStringEN(): String {
    var tmp = SimpleDateFormat("MMMM d")
    var str = tmp.format(this)

    str = str.plus(", ")
    tmp = SimpleDateFormat("yyyy")
    str += tmp.format(this)

    return str
}

fun Long?.convertToShuttleReservationTime(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long?.convertToShuttleReservationTime2(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long?.convertToShuttleReservationDate(): String {
    if(this == null) {
        return ""
    }
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long?.convertToShuttleReservationJustDate(): String {
    if(this == null) {
        return ""
    }

    val sdf = SimpleDateFormat("dd/M/yyyy")
    return sdf.format(Date())
}
//05 Eylül 2022 -> 05 Eylül
fun String?.convertFullDateChangeDayAndMonth(): String {
    if(this == null) {
        return ""
    }
    val strs = this.split(" ").toTypedArray()

    return strs.first().plus(" ").plus(strs[1])
}
//05 Eylül 2022 -> 05 Eylül
fun String?.convertFullDateChangeDayAndMonthEN(): String {
    if(this == null) {
        return ""
    }
    val strs = this.split(" ").toTypedArray()
    return plus(strs[1]).plus(" ").plus(strs.first())
}

