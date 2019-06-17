package com.domatix.yevbes.nucleus.activities.entities

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources.getDrawable
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.domatix.yevbes.nucleus.DateUtils
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.activities.activities.DetailActivityActivity
import com.domatix.yevbes.nucleus.activities.adapters.CustomerAdapter
import com.domatix.yevbes.nucleus.asManyToOne
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.generic.models.CalendarEvent
import com.domatix.yevbes.nucleus.gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.ocpsoft.prettytime.PrettyTime
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.text.SimpleDateFormat
import java.util.*

data class Activity(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("summary")
        val summary: String,

        @Expose
        @SerializedName("note")
        val description: String,

        @Expose
        @SerializedName("date_deadline")
        val dateDeadline: String,

        @Expose
        @SerializedName("user_id")
        val userId: JsonElement,

        @Expose
        @SerializedName("res_model_id")
        val modelId: JsonElement,

        @Expose
        @SerializedName("res_id")
        val resId: JsonElement,

        @Expose
        @SerializedName("activity_type_id")
        val activityTypeId: JsonElement,

        @Expose
        @SerializedName("res_name")
        val resName: String,

        @Expose
        @SerializedName("create_uid")
        val createUid: JsonElement,

        @Expose
        @SerializedName("calendar_event_id")
        val calendarEventId: JsonElement,

        @Expose
        var duration: String? = null,

        var textDate: String? = null,
        var drawableTextDate: Drawable? = null
) {

    companion object {
        @JvmStatic
        @BindingAdapter("setVisibility")
        fun setVisibility(view: View, item: Activity) {
            if (!item.calendarEventId.isJsonPrimitive) {
                view.visibility = View.VISIBLE
            }
        }

        @JvmStatic
        @BindingAdapter("setResponsible")
        fun setResponsible(linearLayout: LinearLayout, filterChecked: Boolean) {
            if (filterChecked) {
                linearLayout.visibility = View.GONE
            } else {
                linearLayout.visibility = View.VISIBLE
            }
        }

        @JvmStatic
        @Synchronized
        @BindingAdapter(value = ["setAttendees", "setActivity"], requireAll = true)
        fun setAttendees(rv: RecyclerView, item: Activity, activity: DetailActivityActivity) {
            val customerAdapter = CustomerAdapter(arrayListOf(), activity)

            val layoutManager = LinearLayoutManager(
                    Odoo.app, LinearLayoutManager.VERTICAL, false
            )

            rv.layoutManager = layoutManager
            rv.itemAnimator = DefaultItemAnimator()
            rv.adapter = customerAdapter

            if (!item.calendarEventId.isJsonPrimitive) {
                var calendarEvent: CalendarEvent? = null
                var arrayOfPartners: JsonArray? = null
                Odoo.load(id = item.calendarEventId.asJsonArray[0].asInt, model = "calendar.event", fields = CalendarEvent.fields) {
                    onSubscribe {
                    }

                    onNext { response ->
                        if (response.isSuccessful) {
                            val load = response.body()!!
                            if (load.isSuccessful) {
                                val result = load.result
                                calendarEvent = gson.fromJson<CalendarEvent>(result.value, object : TypeToken<CalendarEvent>() {
                                }.type)
                            }
                        }
                    }

                    onComplete {
                        calendarEvent?.let {
                            val listIds = gson.fromJson<List<Int>>(it.partnerIds.asJsonArray, object : TypeToken<List<Int>>() {

                            }.type)
                            Odoo.read(model = "res.partner", ids = listIds, fields = listOf("id", "display_name")) {
                                onSubscribe { disposable ->
                                }

                                onNext { response ->
                                    if (response.isSuccessful) {
                                        val read = response.body()!!
                                        if (read.isSuccessful) {
                                            val result = read.result
                                            arrayOfPartners = result.asJsonArray
                                            val arrayList = gson.fromJson<ArrayList<JsonElement>>(arrayOfPartners, object : TypeToken<ArrayList<JsonElement>>() {}.type)
                                            customerAdapter.addItems(arrayList)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic
        @BindingAdapter("htmlText")
        fun setHtmlText(view: HtmlTextView, text: String) {
            val txt = text.replace("<img src=\"", "<img src=\"" + Odoo.protocol.toString().toLowerCase() + "://" + Odoo.host.toLowerCase())
            view.setHtml(txt, HtmlHttpImageGetter(view))
            if (view.text.toString().isBlank()) {
                view.text = Odoo.app.getString(R.string.note)
            }
        }


        @JvmStatic
        @Synchronized
        @BindingAdapter("textDate")
        fun setDateText(view: Button, item: Activity) {
            if (item.textDate == null) {
                val p = PrettyTime(Locale(Locale.getDefault().displayLanguage))

                val dateOnly = SimpleDateFormat("yyyy-MM-dd")
                val formattedDate = dateOnly.format(DateTime().toDate())
                val currentDate = DateTime(formattedDate).toDate()

                val deadLineDate = DateTime(item.dateDeadline).plusHours(DateTime().hourOfDay).toDate()

                when {
                    DateUtils.isSameDay(deadLineDate, currentDate) -> {
                        item.drawableTextDate = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_today)
                        item.textDate = Odoo.app.getString(R.string.today)
                        view.text = item.textDate
                    }

                    DateUtils.isAfterDay(deadLineDate, currentDate) -> {
                        item.drawableTextDate = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_future)
                        item.textDate = " " + p.format(deadLineDate)
                        view.text = item.textDate
                    }

                    DateUtils.isBeforeDay(deadLineDate, currentDate) -> {
                        item.drawableTextDate = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_late)
                        item.textDate = " " + p.format(deadLineDate)
                        view.text = item.textDate
                    }

                    else -> {
                        item.drawableTextDate = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_late)
                        item.textDate = " " + p.format(deadLineDate)
                        view.text = item.textDate
                    }
                }
                view.setCompoundDrawablesWithIntrinsicBounds(item.drawableTextDate, null, null, null)

                if (isTomorrow(deadLineDate)) {
                    item.textDate = Odoo.app.getString(R.string.tomorrow)
                    view.text = item.textDate
                }
                if (isYesterday(deadLineDate)) {
                    item.textDate = Odoo.app.getString(R.string.before_day)
                    view.text = item.textDate
                }

                if (!item.calendarEventId.isJsonPrimitive) {
                    val idEventCalendar = item.calendarEventId.asManyToOne.id


                    Odoo.load(id = idEventCalendar, model = "calendar.event", fields = listOf("display_time")) {
                        onSubscribe { disposable ->
                        }

                        onNext { response ->
                            if (response.isSuccessful) {
                                val load = response.body()!!
                                if (load.isSuccessful) {
                                    val result = load.result
                                    val aux = result.value

                                    val formatDateTime = aux.getAsJsonPrimitive("display_start").asString
                                    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC()
                                    val dateTime = dtf.parseDateTime(formatDateTime).withZone(DateTimeZone.getDefault())

                                    if (DateUtils.isToday(deadLineDate)) {
                                        item.textDate = Odoo.app.getString(R.string.today) + " a las ${dateTime.toString("HH:mm")}"
                                        view.text = item.textDate
                                    } else {
//                                    view.text = " " + p.format(deadLineDate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic
        @Synchronized
        @BindingAdapter("durationText")
        fun setDurationText(view: TextView, item: Activity) {
            if (!item.calendarEventId.isJsonPrimitive && view.visibility == View.VISIBLE && item.duration == null) {
                val idEventCalendar = item.calendarEventId.asManyToOne.id

                Odoo.load(id = idEventCalendar, model = "calendar.event") {
                    onNext { response ->
                        if (response.isSuccessful) {
                            val load = response.body()!!
                            if (load.isSuccessful) {
                                val result = load.result
                                val aux = result.value

                                val startDateTimeFormatted = aux.getAsJsonPrimitive("start_datetime").asString
                                val stopDateTimeFormatted = aux.getAsJsonPrimitive("stop_datetime").asString
                                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                                val startDateTime = format.parse(startDateTimeFormatted)
                                val stopDateTime = format.parse(stopDateTimeFormatted)
                                val diff = stopDateTime.time - startDateTime.time

                                val diffMinutes = diff / (60 * 1000) % 60
                                val diffHours = diff / (60 * 60 * 1000) % 24

                                item.duration = Odoo.app.getString(com.domatix.yevbes.nucleus.R.string.duration_activ) + " ${diffHours}h:${diffMinutes}m"
                                view.text = item.duration
                            }
                        }
                    }

                }
            }
        }

        private fun isYesterday(d: Date): Boolean {
            return android.text.format.DateUtils.isToday(d.time + android.text.format.DateUtils.DAY_IN_MILLIS)
        }

        private fun isTomorrow(d: Date): Boolean {
            return android.text.format.DateUtils.isToday(d.time - android.text.format.DateUtils.DAY_IN_MILLIS)
        }

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "create_uid" to "create_uid", "summary" to "Summary", "note" to "Description", "date_deadline" to "Deadline",
                "user_id" to "User ID", "res_model_id" to "res Model ID", "res_id" to "Document ID", "activity_type_id" to "Activity Type ID", "res_name" to "Document Name",
                "calendar_event_id" to "Calendar Event")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}