package com.domatix.yevbes.nucleus.country

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.View
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RecyclerTouchListener
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.country.entities.Country
import com.domatix.yevbes.nucleus.customer.CustomerProfileActivity
import com.domatix.yevbes.nucleus.gson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class CountryListActivity : AppCompatActivity() {
    private var countryList: ArrayList<Country> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var mAdapter: CountryAdapter? = null
    lateinit var compositeDisposable: CompositeDisposable private set
    private val countryListType = object : TypeToken<ArrayList<Country>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        filterCountry()

        compositeDisposable = CompositeDisposable()

        recyclerView = findViewById(R.id.rv_country_list)

        mAdapter = CountryAdapter(countryList)
        val mLayoutManager = LinearLayoutManager(applicationContext)


        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView?.adapter = mAdapter
        recyclerView?.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onClick(view: View, position: Int) {
                val country = countryList[position]
                val intent = Intent()
                val countryGson = Gson()
                val countryGsonAsAString = countryGson.toJson(country)
                intent.putExtra(CustomerProfileActivity.COUNTRY_ITEM_SELECTED, countryGsonAsAString)
                setResult(RESULT_OK, intent)
                finish()
                //Toast.makeText(applicationContext,  "${country.id} : ${country.name}  is selected!", Toast.LENGTH_SHORT).show()
            }

            /*override fun onLongClick(view: View?, position: Int) {
            }*/

        }))
        fetchCountries()
    }

    private fun filterCountry() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.queryHint = getString(R.string.search_country)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    mAdapter?.filter(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    mAdapter?.filter(newText)
                }
                return true
            }

        })

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
    }

    private fun fetchCountries() {
        Odoo.searchRead(model = "res.country", fields = listOf("id", "name", "image"), domain = listOf(), offset = 0, limit = 0, sort = "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        countryList.addAll(gson.fromJson(searchRead.result.records, countryListType))
                        /*val country = Country(0,getString(R.string.no_country),getString(R.string.no_country))
                        countryList.add(0,country)*/
                        mAdapter?.addRowItems(countryList)
                        mAdapter?.notifyDataSetChanged()
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
                    } else {
                        // Odoo specific error
                        Timber.w("searchRead() failed with ${searchRead.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {

            }
        }
    }
}
