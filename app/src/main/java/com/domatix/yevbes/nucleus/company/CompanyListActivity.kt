package com.domatix.yevbes.nucleus.company

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.View
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RecyclerTouchListener
import com.domatix.yevbes.nucleus.company.entities.Company
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.customer.CustomerProfileActivity
import com.domatix.yevbes.nucleus.gson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class CompanyListActivity : AppCompatActivity() {
    private var companyList: ArrayList<Company> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var mAdapter: CompanyAdapter? = null
    lateinit var compositeDisposable: CompositeDisposable private set
    private val companyListType = object : TypeToken<ArrayList<Company>>() {}.type


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        filterCompany()

        compositeDisposable = CompositeDisposable()

        recyclerView = findViewById(R.id.rv_company_list)

        mAdapter = CompanyAdapter(companyList)
        val mLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView?.adapter = mAdapter
        recyclerView?.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView, object : RecyclerTouchListener.ClickListener {

            override fun onClick(view: View, position: Int) {
                val company = companyList[position]


                val intent = Intent()
                val companyGson = Gson()
                val companyGsonAsAString = companyGson.toJson(company)
                intent.putExtra(CustomerProfileActivity.COMPANY_ITEM_SELECTED, companyGsonAsAString)
                setResult(RESULT_OK, intent)
                finish()
                //Toast.makeText(applicationContext,  "${country.id} : ${country.name}  is selected!", Toast.LENGTH_SHORT).show()
            }

            override fun onLongClick(view: View?, position: Int) {

            }

        }))
        fetchCompanies()
    }

    private fun filterCompany() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.queryHint = getString(R.string.search_company)

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

    private fun fetchCompanies() {
        Odoo.searchRead("res.partner", Company.fields,
                listOf(
                        listOf("is_company", "=", true)
                )
                , offset = 0, limit = 0, sort = "name ASC") {

            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        companyList.addAll(gson.fromJson(searchRead.result.records, companyListType))
                        val company = Company(0, getString(R.string.no_company), getString(R.string.no_company))
                        companyList.add(0, company)
                        mAdapter?.addRowItems(companyList)
                        mAdapter?.notifyDataSetChanged()
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
                    } else {
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
