package com.domatix.yevbes.nucleus.customer

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.company.CompanyListActivity
import com.domatix.yevbes.nucleus.company.entities.Company
import com.domatix.yevbes.nucleus.country.CountryListActivity
import com.domatix.yevbes.nucleus.country.entities.Country
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.ActivityCustomerProfileBinding
import com.google.gson.Gson
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.listeners.IPickResult

class CustomerProfileActivity : AppCompatActivity(), PersistDataListener, IPickResult, CustomerProfileFragment.OnFragmentInteractionListener {
    override fun onBackPressFragm() {
        onBackPressed()
    }

    lateinit var binding: ActivityCustomerProfileBinding
    private var imgBitmapActual: Bitmap? = null
    private lateinit var country: Country
    private lateinit var company: Company

    companion object {
        const val CUSTOMER_OBJECT = "customer"
        const val COUNTRY_ITEM_SELECTED = "CountryItem"
        const val COMPANY_ITEM_SELECTED = "CompanyItem"
        const val CUSTOMER_PROFILE_EDIT_FRAG_TAG = "CustomerProfileEditFragment"
        const val COMPANY_REQUEST_CODE = 1
        const val COUNTRY_REQUEST_CODE = 2
        const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val intent = intent
        val customerGsonAsAString = intent.getStringExtra(CUSTOMER_OBJECT)
        val customerGson = Gson()
        val customer: Customer = customerGson.fromJson(customerGsonAsAString, Customer::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_customer_profile)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.name = customer.name
        binding.imageSmall = customer.imageSmall
        //val imgView = findViewById<ImageView>(R.id.ivProfile)
        //(imgView?.drawable as BitmapDrawable).bitmap
        if (customer.imageSmall != "false") {
            val decodedString = Base64.decode(customer.imageSmall, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imgBitmapActual = decodedByte
        }

        if (customer.countryId.isJsonPrimitive && !customer.countryId.asJsonPrimitive.asBoolean) {
            country = Country(0, getString(R.string.base64_full_transparent_no_translate), getString(R.string.no_country))
        } else {
            country = Country(customer.countryId.asJsonArray.get(0).asInt, getString(R.string.base64_full_transparent_no_translate), customer.countryId.asJsonArray.get(1).asString)
        }

        if (customer.companyId.isJsonPrimitive && !customer.companyId.asJsonPrimitive.asBoolean) {
            company = Company(0, getString(R.string.base64_full_transparent_no_translate), getString(R.string.no_company))

        } else {
            company = Company(customer.companyId.asJsonArray.get(0).asInt, getString(R.string.base64_full_transparent_no_translate), customer.companyId.asJsonArray.get(1).asString)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, CustomerProfileFragment.newInstance(customerGsonAsAString))
                    .commit()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            COUNTRY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val countryString = data?.getStringExtra(COUNTRY_ITEM_SELECTED)
                    val countryGson = Gson()
                    country = countryGson.fromJson(countryString, Country::class.java)
                    findViewById<TextView>(R.id.tvlistCountry).text = country.name
                }
            }

            COMPANY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val companyString = data?.getStringExtra(COMPANY_ITEM_SELECTED)
                    val companyGson = Gson()
                    company = companyGson.fromJson(companyString, Company::class.java)
                    findViewById<TextView>(R.id.tvlistCompanies).text = company.name
                }
            }


            else -> {

            }
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            val frag = supportFragmentManager.findFragmentByTag(CUSTOMER_PROFILE_EDIT_FRAG_TAG)
            if (frag.isVisible && frag != null) {
                (frag as CustomerProfileEditFragment).showAlertDialog()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CustomerProfileActivity.REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                val frag = supportFragmentManager.findFragmentByTag(CUSTOMER_PROFILE_EDIT_FRAG_TAG)
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        // process the normal flow
                        if (frag.isVisible && frag != null) {
                            (frag as CustomerProfileEditFragment).dispatchPickPicture()
                        }

                        //else any one or both the permissions are not granted
                    } else {
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK(getString(R.string.explain_permissions_title),
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> {
                                                if (frag.isVisible && frag != null) {
                                                    (frag as CustomerProfileEditFragment).checkAndRequestPermissions()
                                                }
                                            }
                                            DialogInterface.BUTTON_NEGATIVE -> {
                                            }
                                            // proceed with logic by disabling the related features or quit the app.
                                            // finish()
                                        }
                                    })
                        } else {
                            explain(getString(R.string.explain_permissions))
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }
    }

    override fun onPickResult(p0: PickResult?) {
        if (p0?.error == null) {
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.
            onEditPerfilImageCaptured(p0?.bitmap!!)

            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, p0.error.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok_positive_button), okListener)
                .setNegativeButton(getString(R.string.cancel_negative_button), okListener)
                .create()
                .show()
    }

    private fun explain(msg: String) {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setMessage(msg)
                .setPositiveButton(getString(R.string.explain_permissions_positive_button)) { _, _ ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:com.domatix.yevbes.nucleus")))
                }
                .setNegativeButton(R.string.explain_permissions_negative_button) { _, _ -> finish() }
        dialog.show()
    }

    fun onCheckIsCompanyBox(view: View) {
        val ch = view as CheckBox
        if (ch.isChecked) {
            findViewById<ConstraintLayout>(R.id.clickableListCompany).visibility = View.INVISIBLE
        } else {
            findViewById<ConstraintLayout>(R.id.clickableListCompany).visibility = View.VISIBLE
        }
    }

    fun onClickSelectCompanyList(view: View) {
        intent = Intent(this, CompanyListActivity::class.java)
        startActivityForResult(intent, COMPANY_REQUEST_CODE)
    }

    fun onClickSelectCountryList(view: View) {
        intent = Intent(this, CountryListActivity::class.java)
        startActivityForResult(intent, COUNTRY_REQUEST_CODE)
    }

    override fun onEditPerfilGetBMP(): Bitmap? {
        return imgBitmapActual
    }

    override fun onEditPerfilGetCountry(): Country {
        return country
    }

    override fun onEditPerfilGetCompany(): Company {
        return company
    }

    private fun onEditPerfilImageCaptured(image: Bitmap) {
        val img = findViewById<ImageView>(R.id.ivProfile)
        img?.setImageBitmap(image)
        imgBitmapActual = image
    }


}
