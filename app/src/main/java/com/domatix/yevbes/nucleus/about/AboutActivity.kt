package com.domatix.yevbes.nucleus.about

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)

        setSupportActionBar(binding.tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tb.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.tvLinkDomatix1.apply {
            movementMethod = LinkMovementMethod.getInstance()

            val domatixUrl = "https://www.domatix.com/"

            val domatix = this.text.toString()

            val message = SpannableString("$domatix").apply {
                setLinkSpan(domatix, domatixUrl)
            }
            text = message
            this.setLinkTextColor(ContextCompat.getColor(context,R.color.linkColor))
        }

        binding.tvLinkDomatix2.apply {
            movementMethod = LinkMovementMethod.getInstance()

            val domatixUrl = "https://www.domatix.com/"

            val domatix = this.text.toString()

            val message = SpannableString("$domatix").apply {
                setLinkSpan(domatix, domatixUrl)
            }
            text = message
            this.setLinkTextColor(ContextCompat.getColor(context,R.color.linkColor))
        }
    }

    private fun SpannableString.setLinkSpan(text: String, url: String) {
        val textIndex = this.indexOf(text)
        setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }.also { startActivity(it) }
                    }
                },
                textIndex,
                textIndex + text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
