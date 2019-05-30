package com.domatix.yevbes.nucleus.core.preferences

import android.app.Activity
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.databinding.ActivitySettingsBinding
import com.domatix.yevbes.nucleus.utils.ConstantManager
import com.domatix.yevbes.nucleus.utils.LocaleHelper
import com.domatix.yevbes.nucleus.utils.PreferencesManager

class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private lateinit var app: App
    lateinit var binding: ActivitySettingsBinding private set
    private var clicked = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            //            val intentAux = Intent()
//            intentAux.putExtra("MENU_ITEM_ID", intent.extras.getInt("MENU_ITEM_ID"))
            if (clicked)
                setResult(Activity.RESULT_OK)
            else
                setResult(Activity.RESULT_CANCELED)
            finish()
//            finishActivity(Activity.RESULT_OK)
//            onBackPressed()
        }

        binding.englishOption.setOnClickListener(this)
        binding.spanishOption.setOnClickListener(this)
        val localeLanguage = PreferencesManager.getLocaleLanguage()

        when (localeLanguage) {
            "en" -> {
                binding.englishOption.isChecked = true
                binding.spanishOption.isChecked = false
            }
            "es" -> {
                binding.spanishOption.isChecked = true
                binding.englishOption.isChecked = false
            }
        }
        binding.toolbar.title = resources.getString(R.string.action_settings)
        binding.spanishOption.text = resources.getString(R.string.spanish_language)
        binding.englishOption.text = resources.getString(R.string.english_language)
        binding.textView23.text = resources.getString(R.string.set_default_language_text)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.englishOption -> {
                changeLanguage(ConstantManager.EN)
                clicked = true
            }

            R.id.spanishOption -> {
                clicked = true
                changeLanguage(ConstantManager.ES)
            }
        }
    }

    private fun changeLanguage(langCode: String) {
        LocaleHelper.setLocale(this, langCode)
        PreferencesManager.saveLocaleLanguage(langCode)
        binding.toolbar.title = resources.getString(R.string.action_settings)
        binding.spanishOption.text = resources.getString(R.string.spanish_language)
        binding.englishOption.text = resources.getString(R.string.english_language)
        binding.textView23.text = resources.getString(R.string.set_default_language_text)
    }

}
