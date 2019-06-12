package com.domatix.yevbes.nucleus.activities.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_ID
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_NAME
import com.domatix.yevbes.nucleus.activities.ACTIVITY_ITEM_POSITION
import com.domatix.yevbes.nucleus.activities.entities.Activity
import com.domatix.yevbes.nucleus.databinding.ActivityDetailActivityBinding
import com.domatix.yevbes.nucleus.generic.detailCards.ContactDetailActivity
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.jsonElementToString
import com.domatix.yevbes.nucleus.trimFalse
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_detail_activity.*

const val CUSTOMER_ID = "CUSTOMER_ID"
class DetailActivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailActivityBinding
    private lateinit var activity: Activity
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

      /*  when(activity.modelId.asJsonArray[1].asString)
        {
            ""
        }*/
        binding.resName.setOnClickListener {
            val intent = Intent(this, ContactDetailActivity::class.java)
            intent.putExtra(CUSTOMER_ID, activity.resId.asInt)
            startActivity(intent)
        }
    }

}
