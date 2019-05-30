package com.domatix.yevbes.nucleus.activities

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.activities.callbacks.OnCheckClicked
import com.domatix.yevbes.nucleus.activities.entities.Activity
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityRowBinding
import kotlinx.android.synthetic.main.activity_row.view.*
import kotlin.collections.ArrayList


class ActivityDataAdapter(
        val fragment: ActivitiesFragment,
        items: ArrayList<Any>,
        private val listener: OnCheckClicked
) : RecyclerBaseAdapter(items, fragment.binding.activitiesRecyclerView) {

    companion object {
        const val TAG: String = "ActivityAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<Activity> = ArrayList(
            items.filterIsInstance<Activity>()
    )


    fun addRowItems(rowItemsAll: ArrayList<Activity>) {
        this.rowItems.addAll(rowItemsAll)
        addAll(rowItemsAll.toMutableList<Any>() as ArrayList<Any>)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is Activity) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<Activity>()))
    }

    fun removeItemAdapter(position: Int): Activity {
        val activity = this.rowItems.removeAt(position)
        removeItem(position)
        return activity
    }

    fun restoreItem(item: Activity, position: Int) {
        add(item, position)
        this.rowItems.add(position, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ActivityDataAdapter.VIEW_TYPE_ITEM -> {
                val binding = ActivityRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )

                binding.root.imageView10.setOnClickListener {
                    listener.onCheckClicked( binding.root)
                }

                return ActivityViewHolder(binding,
                        binding.root.findViewById(R.id.view_background) as View,
                        binding.root.findViewById(R.id.view_foreground) as View
                )
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }

    val rowItemCount: Int get() = rowItems.size

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as ActivityViewHolder
                val item = items[position] as Activity
                val binding = holder.binding
                binding.activityObj = item

                /*val handler = Handler()

                val anim = (binding.imageView10.drawable as Animatable)*/


                /*binding.pattern = "EEE dd MMM HH:mm"
                binding.timezone = TimeZone.getTimeZone("GMT+01:00")
                binding.locale = Locale.getDefault()*/
                /*UtilsKt.getDateToFriendlyFormat(activityObj.dateDeadline, pattern, locale, timezone).toLowerCase()*/

                /*val dataDecoder = getDateToFriendlyFormat(item.dateDeadline, "yyyy/MM/dd HH:mm:ss", Locale.getDefault(), TimeZone.getTimeZone("GMT+01:00"))
                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                val dateActivity = sdf.parse(dataDecoder)

                val calendar = Calendar.getInstance()
                val currentDay = calendar.time

                //val millis = dateActivity.time
                val p = PrettyTime(Locale(Locale.getDefault().displayLanguage))
                calendar.time = dateActivity

                if (DateUtils.isToday(dateActivity)) {
                    binding.timeago = fragment.getString(R.string.today)
                } else {
                    binding.timeago = " " + p.format(dateActivity) //TimeAgo.using(millis)
                }

                val drawable: Drawable?

                drawable = when {
                    DateUtils.isSameDay(dateActivity, currentDay) -> {
                        getDrawable(fragment.context!!, R.drawable.ic_calendar_check_today)
                    }

                    DateUtils.isAfterDay(dateActivity, currentDay) -> {
                        getDrawable(fragment.context!!, R.drawable.ic_calendar_check_future)
                    }

                    DateUtils.isBeforeDay(dateActivity, currentDay) -> {
                        getDrawable(fragment.context!!, R.drawable.ic_calendar_check_late)
                    }

                    else -> {
                        getDrawable(fragment.context!!, R.drawable.ic_calendar_check_late)
                    }
                }

                binding.buttonDatePicker.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

                //binding.timeago = " "+TimeAgo.using(millis)
                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as Activity

                        Timber.v(String.format("%s : %s", clickedPosition, clickedItem.summary))
                    }
                }

                binding.imageView10.setOnClickListener {
                    val clickedPosition = holder.adapterPosition
                    *//*anim.start()
                    AsyncTask.execute {
                        while (anim.isRunning);
                        handler.post {
                            listener.onItemClick(item.id, item.summary, baseHolder.adapterPosition)
                            anim.stop()
                        }
                    }*//*
                    listener.onItemClick(item.id, item.summary, clickedPosition)
                }


                if (!item.calendarEventId.isJsonPrimitive) {
                    val idEventCalendar = item.calendarEventId.asManyToOne.id

                    compositeDisposable = CompositeDisposable()

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
                                    Timber.w("result of calendar.event $result")
                                    if (DateUtils.isToday(dateActivity)) {
                                        binding.timeago = fragment.getString(R.string.today) + " ${aux.getAsJsonPrimitive("display_start").asString}"
                                    } else {
                                        binding.timeago = " " + p.format(dateActivity) *//*TimeAgo.using(millis)*//* + " ${aux.getAsJsonPrimitive("display_start").asString}"
                                    }

                                    compositeDisposable.dispose()
                                    compositeDisposable = CompositeDisposable()
                                } else {
                                    // Odoo specific error
                                    Timber.w("load() failed with ${load.errorMessage}")
                                }
                            } else {
                                Timber.w("request failed with ${response.code()}:${response.message()}")
                            }
                        }

                        onError { error ->
                            error.printStackTrace()
                        }

                        onComplete { }
                    }

                }*/
            }
        }
    }
}