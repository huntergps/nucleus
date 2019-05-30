package com.domatix.yevbes.nucleus.sga.view.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityDetailTransferBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sga.service.model.StockMove
import com.domatix.yevbes.nucleus.sga.view.adapter.DetailTransferAdapter
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_detail_transfer.*

class DetailTransferActivity : AppCompatActivity() {
    lateinit var compositeDisposable: CompositeDisposable private set
    private val moveLineListType = object : TypeToken<ArrayList<StockMove>>() {}.type
    private lateinit var binding: ActivityDetailTransferBinding

    companion object {
        const val SELECTED_MOVE_LINES = "SELECTED"
    }

    val mAdapter: DetailTransferAdapter by lazy {
        DetailTransferAdapter(binding, arrayListOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moveLines = intent.extras.getString(SELECTED_MOVE_LINES)
        val aux = moveLines.substring(1, moveLines.length - 1)
        val delimiter = ','

        val lines = aux.split(delimiter)

        val list: ArrayList<Int> = ArrayList()

        for (i in lines) {
            list.add(i.toInt())
        }

        //val param = jsonMoveLines.get(0).toString()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_transfer)
        compositeDisposable = CompositeDisposable()
        setSupportActionBar(tb)


        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        tb.setNavigationOnClickListener {
            onBackPressed()
        }

        val mLayoutManager = LinearLayoutManager(applicationContext)

        rv.layoutManager = mLayoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = mAdapter
        // recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        fetchMoveLines(list)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_transfer_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            /*    R.id.action_options_products -> {
                //VOLVER A PRODUCTOS
            }

            R.id.action_options_inventory -> {
                val intent = Intent(this, InventoryActivity::class.java)
                startActivity(intent)
            }

            //R.id.action_options_picking -> {}

            R.id.action_options_transfer -> {
                val intent = Intent(this, TransfersActivity::class.java)
                startActivity(intent)
            }

            R.id.action_options_stockmove -> {}*/
            R.id.action_options_stockmove -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchMoveLines(param1: ArrayList<Int>) {
        Odoo.searchRead("stock.move", StockMove.fields,
                listOf(
                        listOf(
                                "id", "in", param1
                        )
                ), mAdapter.rowItemCount, 0, "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (response.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<StockMove> = gson.fromJson(searchRead.result.records, moveLineListType)
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
                mAdapter.finishedMoreLoading()
            }
            onComplete {


            }
        }

    }
}