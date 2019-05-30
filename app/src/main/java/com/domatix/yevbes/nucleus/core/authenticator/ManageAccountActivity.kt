package com.domatix.yevbes.nucleus.core.authenticator

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.utils.recycler.decorators.VerticalLinearItemDecorator
import com.domatix.yevbes.nucleus.databinding.ActivityManageAccountBinding
import com.domatix.yevbes.nucleus.getOdooUsers
import io.reactivex.disposables.CompositeDisposable

class ManageAccountActivity : AppCompatActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    lateinit var app: App private set
    lateinit var compositeDisposable: CompositeDisposable private set
    lateinit var binding: ActivityManageAccountBinding private set
    lateinit var adapter: ManageAccountAdapter private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        compositeDisposable = CompositeDisposable()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_account)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val users = getOdooUsers()
        val layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        )
        binding.rv.layoutManager = layoutManager
        binding.rv.addItemDecoration(VerticalLinearItemDecorator(
                resources.getDimensionPixelOffset(R.dimen.default_8dp)
        ))

        adapter = ManageAccountAdapter(this, ArrayList(users))
        binding.rv.adapter = adapter
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
