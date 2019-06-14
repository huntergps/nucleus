package com.domatix.yevbes.nucleus.activities.entities

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources.getDrawable
import android.support.v7.widget.CardView
import android.view.View
import android.widget.*
import com.domatix.yevbes.nucleus.DateUtils
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.asManyToOne
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.generic.models.CalendarEvent
import com.domatix.yevbes.nucleus.gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
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
        val calendarEventId: JsonElement
) {

    companion object {
        @JvmStatic
        @BindingAdapter("setVisibility")
        fun setVisibility(view: CardView, item: Activity) {
            if (!item.calendarEventId.isJsonPrimitive) {
                view.visibility = View.VISIBLE
            }
        }

        @JvmStatic
        @BindingAdapter("setAttendees")
        fun setAttendees(lv: ListView, item: Activity) {
            if (!item.calendarEventId.isJsonPrimitive) {
                val compositeDisposable = CompositeDisposable()
                var calendarEvent: CalendarEvent? = null
                var arrayOfPartners: JsonArray? = null
                Odoo.load(id = item.calendarEventId.asJsonArray[0].asInt, model = "calendar.event", fields = CalendarEvent.fields) {
                    onSubscribe {
                        compositeDisposable.add(it)
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
                                    compositeDisposable.add(disposable)
                                }

                                onNext { response ->
                                    if (response.isSuccessful) {
                                        val read = response.body()!!
                                        if (read.isSuccessful) {
                                            val result = read.result
                                            arrayOfPartners = result.asJsonArray
                                            arrayOfPartners?.let {

                                            }
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
        @BindingAdapter("textDate")
        fun setDateText(view: Button, item: Activity) {
            val p = PrettyTime(Locale(Locale.getDefault().displayLanguage))

            val dateOnly = SimpleDateFormat("yyyy-MM-dd")
            val formattedDate = dateOnly.format(DateTime().toDate())
            val currentDate = DateTime(formattedDate).toDate()

            val deadLineDate = DateTime(item.dateDeadline).plusHours(DateTime().hourOfDay).toDate()

            val drawable: Drawable?
            when {
                DateUtils.isSameDay(deadLineDate, currentDate) -> {
                    drawable = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_today)
                    view.text = Odoo.app.getString(R.string.today)
                }

                DateUtils.isAfterDay(deadLineDate, currentDate) -> {
                    drawable = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_future)
                    view.text = " " + p.format(deadLineDate)
                }

                DateUtils.isBeforeDay(deadLineDate, currentDate) -> {
                    drawable = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_late)
                    view.text = " " + p.format(deadLineDate)
                }

                else -> {
                    drawable = getDrawable(Odoo.app.applicationContext, R.drawable.ic_calendar_check_late)
                    view.text = " " + p.format(deadLineDate)
                }
            }
            view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

            if (isTomorrow(deadLineDate)) {
                view.text = Odoo.app.getString(R.string.tomorrow)
            }
            if (isYesterday(deadLineDate)) {
                view.text = Odoo.app.getString(R.string.before_day)
            }

            if (!item.calendarEventId.isJsonPrimitive) {
                val idEventCalendar = item.calendarEventId.asManyToOne.id

                val compositeDisposable = CompositeDisposable()

                Odoo.load(id = idEventCalendar, model = "calendar.event", fields = listOf("display_time")) {
                    onSubscribe { disposable ->
                        compositeDisposable.add(disposable)
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
                                    view.text = Odoo.app.getString(R.string.today) + " a las ${dateTime.toString("HH:mm")}"
                                } else {
//                                    view.text = " " + p.format(deadLineDate)
                                }
                            }
                        }
                    }

                }
            }
        }

        @JvmStatic
        @BindingAdapter("durationText")
        fun setDurationText(view: TextView, item: Activity) {
            if (!item.calendarEventId.isJsonPrimitive) {
                val idEventCalendar = item.calendarEventId.asManyToOne.id

                val compositeDisposable = CompositeDisposable()

                Odoo.load(id = idEventCalendar, model = "calendar.event") {
                    onSubscribe { disposable ->
                        compositeDisposable.add(disposable)
                    }

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

                                view.text = Odoo.app.getString(com.domatix.yevbes.nucleus.R.string.duration_activ) + " ${diffHours}h:${diffMinutes}m"
                                view.visibility = View.VISIBLE
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