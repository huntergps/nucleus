package com.domatix.yevbes.nucleus

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.databinding.BindingAdapter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.core.OdooUser
import com.domatix.yevbes.nucleus.core.authenticator.SplashActivity
import com.domatix.yevbes.nucleus.core.entities.Many2One
import com.domatix.yevbes.nucleus.core.entities.session.authenticate.AuthenticateResult
import com.domatix.yevbes.nucleus.core.utils.encryptAES
import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


const val RECORD_LIMIT = 5

val gson: Gson by lazy {
    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
}

fun Context.createOdooUser(authenticateResult: AuthenticateResult): Boolean {
    val accountManager = AccountManager.get(this)
    val account = Account(authenticateResult.androidName, App.KEY_ACCOUNT_TYPE)
    val result = accountManager.addAccountExplicitly(
            account,
            authenticateResult.password.encryptAES(),
            authenticateResult.toBundle
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        accountManager.notifyAccountAuthenticated(account)
    }
    return result
}

fun Context.getOdooUsers(): List<OdooUser> {
    val manager = AccountManager.get(this)
    val odooUsers = ArrayList<OdooUser>()
    manager.getAccountsByType(App.KEY_ACCOUNT_TYPE)
            .map {
                Odoo.fromAccount(manager, it)
            }
            .forEach { odooUsers += it }
    return odooUsers.toList()
}

fun Context.odooUserByAndroidName(androidName: String): OdooUser? {
    getOdooUsers()
            .filter { it.androidName == androidName }
            .forEach { return it }
    return null
}

fun Context.getActiveOdooUser(): OdooUser? {
    getOdooUsers()
            .filter { it.isActive }
            .forEach { return it }
    return null
}

fun Context.loginOdooUser(odooUser: OdooUser): OdooUser? {
    do {
        val user = getActiveOdooUser()
        if (user != null) {
            logoutOdooUser(user)
        }
    } while (user != null)
    val accountManager = AccountManager.get(this)
    accountManager.setUserData(odooUser.account, "active", "true")

    return getActiveOdooUser()
}

fun Context.logoutOdooUser(odooUser: OdooUser) {
    val accountManager = AccountManager.get(this)
    accountManager.setUserData(odooUser.account, "active", "false")
}

fun Context.deleteOdooUser(odooUser: OdooUser): Boolean {
    val accountManager = AccountManager.get(this)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        accountManager.removeAccountExplicitly(odooUser.account)
    } else {
        @Suppress("DEPRECATION")
        val result = accountManager.removeAccount(odooUser.account, { _ ->

        }, Handler(this.mainLooper))
        result != null && result.result != null && result.result!!
    }
}

val JsonElement.isManyToOne: Boolean get() = isJsonArray && asJsonArray.size() == 2

val JsonElement.asManyToOne: Many2One
    get() = if (isManyToOne) {
        Many2One(asJsonArray)
    } else {
        Many2One(JsonArray().apply { add(0); add("") })
    }

val JsonArray.asIntList: List<Int>
    get() = this.map {
        it.asInt
    }

@Suppress("DEPRECATION")
val Response<*>.errorBodySpanned: Spanned
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(errorBody()!!.string(), Html.FROM_HTML_MODE_COMPACT)
    else
        Html.fromHtml(errorBody()!!.string())

fun AppCompatActivity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun AppCompatActivity.restartApp() {
    TaskStackBuilder.create(this)
            .addNextIntent(Intent(this, SplashActivity::class.java))
            .startActivities()
}

var alertDialog: AlertDialog? = null

fun AppCompatActivity.showMessage(
        title: CharSequence? = null,
        message: CharSequence?,
        cancelable: Boolean = false,
        positiveButton: CharSequence = getString(R.string.ok),
        positiveButtonListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> },
        showNegativeButton: Boolean = false,
        negativeButton: CharSequence = getString(R.string.cancel),
        negativeButtonListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog {
    alertDialog?.dismiss()
    alertDialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setTitle(title)
            .setMessage(if (message?.isNotEmpty() == true) {
                message
            } else {
                getString(R.string.generic_error)
            })
            .setCancelable(cancelable)
            .setPositiveButton(positiveButton, positiveButtonListener)
            .apply {
                if (showNegativeButton) {
                    setNegativeButton(negativeButton, negativeButtonListener)
                }
            }
            .show()
    return alertDialog!!
}

@Suppress("DEPRECATION")
fun AppCompatActivity.showServerErrorMessage(
        response: Response<*>,
        positiveButtonListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog =
        showMessage(
                title = getString(R.string.server_request_error, response.code(), response.body()),
                message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml(response.errorBody()!!.string(), Html.FROM_HTML_MODE_COMPACT)
                else
                    Html.fromHtml(response.errorBody()!!.string()),
                positiveButtonListener = positiveButtonListener
        )

fun AppCompatActivity.closeApp(message: String = getString(R.string.generic_error)): AlertDialog =
        showMessage(getString(R.string.fatal_error), message, false, getString(R.string.exit), DialogInterface.OnClickListener { _, _ ->
            ActivityCompat.finishAffinity(this)
        })

fun String.trimFalse(): String = if (this != "false") this else ""

fun AppCompatActivity.filteredErrorMessage(errorMessage: String): String = when (errorMessage) {
    "Expected singleton: res.users()" -> {
        getString(R.string.login_credential_error)
    }
    else -> {
        errorMessage
    }
}

@Suppress("DEPRECATION")
fun AppCompatActivity.getProgressDialog(): android.app.ProgressDialog {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return android.app.ProgressDialog(this, R.style.AppAlertDialogTheme)
    }
    return android.app.ProgressDialog(this)
}

fun AppCompatActivity.isDeviceOnline(): Boolean {
    var isConnected = false
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nInfo = manager.activeNetworkInfo
    if (nInfo != null && nInfo.isConnectedOrConnecting) {
        isConnected = true
    }
    return isConnected
}

fun String.toJsonElement(): JsonElement = gson.fromJson(this, JsonElement::class.java)

fun String.toJsonPrimitive(): JsonPrimitive = toJsonElement().asJsonPrimitive

fun String.toJsonObject(): JsonObject = toJsonElement().asJsonObject

fun String.toJsonArray(): JsonArray = toJsonElement().asJsonArray

fun jsonElementToString(value: JsonElement): String {
    return if (value.isJsonArray)
        value.asJsonArray.get(1).asString.trimFalse()
    else
        value.asJsonPrimitive.asString.trimFalse()
}

fun integerToString(value: Int): String {
    return value.toString()
}

fun getDateToFriendlyFormat(receiveDate: Date, patter: String, locale: Locale, timeZone: TimeZone): String {
    val myFormat = SimpleDateFormat(patter, locale)
    myFormat.timeZone = timeZone
    return myFormat.format(receiveDate)
}

fun fromStringToDate(receiveString: String, receivePattern: String, locale: Locale): Date {
    val format = SimpleDateFormat(receivePattern, locale)
    try {
        return format.parse(receiveString)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date()
}

fun convertDateToSpecificTimeZone(currentDate: Date, currentTimeZone: TimeZone, newTimeZone: DateTimeZone): Date {
    // Set current timezone and convert Date to another pattern
    val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    readFormat.timeZone = currentTimeZone
    val dateStr = readFormat.format(currentDate)
    val writeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = writeFormat.parse(dateStr)

    // Convert to Joda-Time
    val dateTime = DateTime(date)
    val convertedDate = dateTime.withZone(newTimeZone)
    return convertedDate.toLocalDateTime().toDate()
}


fun parseServerDateToLocal(raw_date: String, formatDateServer: String): DateTime {
    //            val formatDateServer = "yyyy-MM-dd'T'HH:mm:ssZ"
    return DateTime.parse(raw_date, DateTimeFormat.forPattern(formatDateServer))
}

fun saleStates(receiveString: String, fragment: Fragment): String {
    when (receiveString) {
        "draft" -> {
            return fragment.getString(R.string.quotation)
        }
        "sent" -> {
            return fragment.getString(R.string.quotation_sent)
        }
        "sale" -> {
            return fragment.getString(R.string.sale_order)
        }
        "done" -> {
            return fragment.getString(R.string.order_done)
        }
        "cancel" -> {
            return fragment.getString(R.string.order_canceled)
        }
    }
    return " "
}

fun worksheetsStates(receiveString: String): String {
    when (receiveString) {
        "draft" -> {
            return Odoo.app.getString(R.string.worksheet_state_draft)
        }
        "cancel" -> {
            return Odoo.app.getString(R.string.worksheet_state_cancel)
        }
        "confirmed" -> {
            return Odoo.app.getString(R.string.worksheet_state_confirmed)
        }
        "under_repair" -> {
            return Odoo.app.getString(R.string.worksheet_state_under_repaired)
        }
        "ready" -> {
            return Odoo.app.getString(R.string.worksheet_state_ready)
        }
        "2binvoiced" -> {
            return Odoo.app.getString(R.string.worksheet_state_to_be_invoiced)
        }
        "invoice_except" -> {
            return Odoo.app.getString(R.string.worksheet_state_invoice_except)
        }
        "done" -> {
            return Odoo.app.getString(R.string.worksheet_state_done)
        }
    }
    return " "
}

/*fun getDate(context: Context, listener: OnDateSelected) {
    var textDate = ""
    val zero = "0"
    val slash = "/"
    val c = Calendar.getInstance()

    val monthCal = c.get(Calendar.MONTH)
    val dayCal = c.get(Calendar.DAY_OF_MONTH)
    val yearCal = c.get(Calendar.YEAR)

    var actualMonth: Int
    var formattedDay: String = ""
    var formattedMonth: String = ""
    var formattedYear: Int = 2019

    val getDate = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->

    },
            yearCal, monthCal, dayCal)
    getDate.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, p1 ->
        if (p1 == DialogInterface.BUTTON_POSITIVE) {
            val datePicker = getDate.datePicker

            actualMonth = datePicker.month + 1
            formattedYear = datePicker.year
            formattedDay = if (datePicker.dayOfMonth < 10) zero + datePicker.dayOfMonth.toString() else datePicker.dayOfMonth.toString()
            formattedMonth = if (actualMonth < 10) zero + actualMonth.toString() else actualMonth.toString()
            textDate = formattedDay + slash + formattedMonth + slash + formattedYear
            listener.onDateSelected(textDate)
            //binding.saleDate.text = formattedDay + slash + formattedMonth + slash + formattedYear
        }
    }
    getDate.setOnDismissListener {
    }
    getDate.show()
}*/

@BindingAdapter("android:layout_marginStart")
fun setStartMargin(view: View, startMargin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(Math.round(startMargin), layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin)
    view.layoutParams = layoutParams

}


