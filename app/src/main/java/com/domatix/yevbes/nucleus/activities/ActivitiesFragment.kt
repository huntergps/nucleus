package com.domatix.yevbes.nucleus.activities


import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.activities.activities.DetailActivityActivity
import com.domatix.yevbes.nucleus.activities.callbacks.OnCheckClicked
import com.domatix.yevbes.nucleus.activities.entities.Activity
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentActivitiesBinding
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 *
 */
const val ACTIVITY_ITEM = "ACTIVITY_ITEM"
const val ACTIVITY_ITEM_ID = "ACTIVITY_ITEM_ID"
const val ACTIVITY_ITEM_NAME = "ACTIVITY_ITEM_NAME"
const val ACTIVITY_ITEM_POSITION = "ACTIVITY_ITEM_POSITION"
const val REQUEST_CODE = 1

class ActivitiesFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, SearchView.OnQueryTextListener {

    companion object {
        enum class ActivityType {
            Activity
        }

        private const val TYPE = "type"

        fun newInstance(activityType: ActivityType) =
                ActivitiesFragment().apply {
                    arguments = Bundle().apply {
                        putString(TYPE, activityType.name)
                    }
                }
    }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var activitiesList: ArrayList<Activity> = ArrayList()
    private val activityListType = object : TypeToken<ArrayList<Activity>>() {}.type
    private val limit = RECORD_LIMIT
    private var coordinatorLayout: CoordinatorLayout? = null
    private var filterMenu: Menu? = null
    private var sortByMenu: Menu? = null
    private lateinit var activityType: ActivityType
    private var isFilterActive: Boolean = true

    private val mAdapter: ActivityDataAdapter by lazy {
        ActivityDataAdapter(this, arrayListOf(), object : OnCheckClicked {
            override fun onCheckClicked(view: View) {
                val position = binding.activitiesRecyclerView.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as Activity
                val name = jsonElementToString(item.activityTypeId) + ": " + item.summary.trimFalse()
                checkActivityAsDone(item.id, name, position)
            }
        }, object : OnShortLongAdapterItemClickListener {
            override fun onShortAdapterItemPressed(view: View) {
                val position = binding.activitiesRecyclerView.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as Activity
                val activityGsonString = gson.toJson(item)
                val data = Intent(activity, DetailActivityActivity::class.java)
                data.putExtra(ACTIVITY_ITEM, activityGsonString)
                data.putExtra(ACTIVITY_ITEM_POSITION, position)
                startActivityForResult(data, REQUEST_CODE)
            }

            override fun onLongAdapterItemPressed(view: View) {
                // Nothing
            }

        })
    }

    lateinit var activity: MainActivity private set
    lateinit var binding: FragmentActivitiesBinding private set
    lateinit var compositeDisposable: CompositeDisposable private set

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                when (resultCode) {
                    android.app.Activity.RESULT_OK -> {
                        val id = data?.getIntExtra(ACTIVITY_ITEM_ID, -1)
                        val name = data?.getStringExtra(ACTIVITY_ITEM_NAME)
                        val position = data?.getIntExtra(ACTIVITY_ITEM_POSITION, -1)

                        if (position != -1 && id != -1) {
                            checkActivityAsDone(id!!, name!!, position!!)
                        }
                    }

                    android.app.Activity.RESULT_CANCELED -> {

                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        compositeDisposable = CompositeDisposable()

        // Inflate the layout for this fragment
        binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        coordinatorLayout = view?.findViewById(R.id.coordinator_layout) as CoordinatorLayout
        //val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        activity = getActivity() as MainActivity

        arguments?.let {
            activityType = ActivitiesFragment.Companion.ActivityType.valueOf(it.getString(ActivitiesFragment.TYPE))
        }

        activity.setTitle(R.string.action_activities)
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        activity.binding.nv.menu.findItem(R.id.nav_activities).isChecked = true

        //activity.setSupportActionBar(toolbar)
        activity.setSupportActionBar(binding.tb)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        drawerToggle = ActionBarDrawerToggle(activity, activity.binding.dl,
                binding.tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        activity.binding.dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()


        val layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
        )

        binding.activitiesRecyclerView.layoutManager = layoutManager
        binding.activitiesRecyclerView.itemAnimator = DefaultItemAnimator()
        //binding.activitiesRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        mAdapter.setupScrollListener(binding.activitiesRecyclerView)

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                fetchActivities("date_deadline")
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                fetchActivities("date_deadline")
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            fetchActivities("date_deadline")
        }

        binding.activitiesRecyclerView.adapter = mAdapter

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.activitiesRecyclerView)

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is ActivityViewHolder) {
//            val pos = viewHolder.adapterPosition
            // get the removed item name to display it in snack bar
//            val name = activitiesList[pos].summary
//            val id = activitiesList[pos].id
            val item = mAdapter.items[position] as Activity
            val name = jsonElementToString(item.activityTypeId) + ": " + item.summary.trimFalse()
            checkActivityAsDone(item.id, name, position)
        }
    }

    private fun checkActivityAsDone(id: Int, name: String, position: Int) {
        val item = mAdapter.removeItemAdapter(position)
        var flag = true
        val snackbar = Snackbar
                .make(coordinatorLayout as View, "$name ${getString(R.string.check)}", Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.undo)) {
            flag = false
            mAdapter.restoreItem(item, position)
        }

        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (flag) {
//                    activitiesList.removeAt(position)
                    Odoo.callKw(model = "mail.activity", method = "action_done", args = listOf(id)) {
                        onSubscribe { disposable ->
                            compositeDisposable.add(disposable)
                        }

                        onNext { response ->
                            if (response.isSuccessful) {
                                val callKw = response.body()!!
                                if (callKw.isSuccessful) {
                                    val result = callKw.result
                                    //snackbar.show()
                                } else {
                                    Timber.w("callkw() failed with ${callKw.errorMessage}")
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
                }
            }
        })
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_activities).isChecked = false
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_activities, menu)

        // Menu for filter
        filterMenu = menu?.getItem(0)?.subMenu

        // Menu for sort
        sortByMenu = menu?.getItem(2)?.subMenu?.getItem(0)?.subMenu

        val searchManager = getActivity()?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu?.findItem(R.id.action_activities_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity()?.componentName))
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_filter_my_activities -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchActivities(filterType: String) {
        Odoo.searchRead("mail.activity", Activity.fields,
                when (activityType) {
                    ActivityType.Activity -> {
                        if (isFilterActive) {
                            listOf(
                                    listOf("user_id", '=', Odoo.user.id)
                            )
                        } else {
                            listOf()
                        }
                    }
                }
                , mAdapter.rowItemCount, RECORD_LIMIT, "$filterType ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<Activity> = gson.fromJson(searchRead.result.records, activityListType)

//                        activitiesList.addAll(items)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchActivities(filterType)
                                }
                            }
                        }
                        mAdapter.addRowItems(items)
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
                    } else {
                        mAdapter.showError(searchRead.errorMessage)
                    }
                } else {
                    mAdapter.showError(response.errorBodySpanned)
                }
                mAdapter.finishedMoreLoading()
            }

            onError { error ->
                error.printStackTrace()
                mAdapter.showError(error.message ?: getString(R.string.generic_error))
                mAdapter.finishedMoreLoading()
            }
        }
    }

    private fun fetchActivitiesQuery(query: String?) {
        Odoo.searchRead("mail.activity", Activity.fields,
                when (activityType) {
                    ActivityType.Activity -> {
                        listOf(
                                "|",
                                "|",
                                listOf("summary", "ilike", query),
                                listOf("note", "ilike", query),
                                listOf("res_name", "ilike", query)
                        )
                    }
                }
                , mAdapter.rowItemCount, RECORD_LIMIT, "date_deadline ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<Activity> = gson.fromJson(searchRead.result.records, activityListType)

//                        activitiesList.addAll(items)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchActivitiesQuery(query)
                                }
                            }
                        }
                        mAdapter.addRowItems(items)
                    } else {
                        mAdapter.showError(searchRead.errorMessage)
                    }
                } else {
                    mAdapter.showError(response.errorBodySpanned)
                }
                mAdapter.finishedMoreLoading()
            }

            onError { error ->
                error.printStackTrace()
                mAdapter.showError(error.message ?: getString(R.string.generic_error))
                mAdapter.finishedMoreLoading()
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchActivitiesQuery(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchActivitiesQuery(newText)
        }
        return true
    }
}
