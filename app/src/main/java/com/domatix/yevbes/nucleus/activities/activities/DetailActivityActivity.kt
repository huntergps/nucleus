package com.domatix.yevbes.nucleus.activities.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_ID
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_NAME
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_POSITION
import com.domatix.yevbes.nucleus.activities.entities.Activity
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityDetailActivityBinding
import com.domatix.yevbes.nucleus.generic.models.IrModel
import com.domatix.yevbes.nucleus.generic.ui.dialogs.CustomDialogFragment
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_detail_activity.*
import timber.log.Timber


class DetailActivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailActivityBinding
    private lateinit var activity: Activity
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var dialog: CustomDialogFragment
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        dialog = CustomDialogFragment.newInstance(
                this,
                supportFragmentManager,
                message = getString(R.string.loading_data),
                title = getString(R.string.loading),
                showInstantly = false)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_activity)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val activityGsonString = intent.extras.getString(ACTIVITY_ITEM)
        position = intent.extras.getInt(ACTIVITY_ITEM_POSITION)
        activity = gson.fromJson<Activity>(activityGsonString, object : TypeToken<Activity>() {
        }.type)
        binding.activity = activity
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.actionCheckActivity.setOnClickListener {
            val name = jsonElementToString(activity.activityTypeId) + ": " + activity.summary.trimFalse()
            val data = Intent()
            data.putExtra(ACTIVITY_ITEM_POSITION, position)
            data.putExtra(ACTIVITY_ITEM_ID, activity.id)
            data.putExtra(ACTIVITY_ITEM_NAME, name)
            setResult(android.app.Activity.RESULT_OK, data)
            finish()
        }

        binding.resName.setOnClickListener(null)
        getModel(activity.modelId.asJsonArray[0].asInt)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable
    }

    private fun getModel(id: Int) {
        dialog.showDialog()
        var model: IrModel? = null
        Odoo.load(id = id, model = "ir.model") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val load = response.body()!!
                    if (load.isSuccessful) {
                        val result = load.result
                        model = gson.fromJson<IrModel>(result.value, object : TypeToken<IrModel>() {
                        }.type)
                    } else {
                        Timber.w("load() failed with ${load.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                model?.let {
                    when (it.model) {
                        "res.partner" -> {
                            binding.resName.let { resName ->
                                resName.setTextColor(ContextCompat.getColor(this@DetailActivityActivity,R.color.colorAccent))
                                resName.setOnClickListener(modelDetailsListener(activity.resId.asInt, this@DetailActivityActivity, resName, "res.partner"))
                            }
                        }
                    }
                    dialog.dismissDialog()
                }

                if (model == null) {
                    dialog.dismissDialog()
                    onBackPressed()
                }
            }
        }
    }
}
