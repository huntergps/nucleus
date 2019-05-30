package com.domatix.yevbes.nucleus.customer

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.*
import android.widget.*
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.company.entities.Company
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.country.entities.Country
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.FragmentCustomerProfileEditBinding
import com.domatix.yevbes.nucleus.utils.PreferencesManager
import com.google.gson.Gson
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.ByteArrayOutputStream


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"


class CustomerProfileEditFragment : Fragment() {
    private lateinit var binding: FragmentCustomerProfileEditBinding
    private lateinit var customer: Customer
    private lateinit var prefs: PreferencesManager
    private var listener: PersistDataListener? = null

    lateinit var compositeDisposable: CompositeDisposable private set
    private lateinit var imgBitmap: Bitmap




    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is PersistDataListener) {
            this.listener = context
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        prefs = PreferencesManager
        val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setNavigationOnClickListener {
            showAlertDialog()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_profile_edit, container, false)

        val fab: View = activity!!.findViewById(R.id.favIconPhoto)
        fab.visibility = View.VISIBLE
        fab.setOnClickListener {
            if (checkAndRequestPermissions()) {
                dispatchPickPicture()
            }

        }

        val customerGsonAsAString: String = arguments!!.getString(ARG_PARAM1)
        val customerGson = Gson()
        val customer: Customer = customerGson.fromJson(customerGsonAsAString, Customer::class.java)


        if (customer.countryId.isJsonArray) {
            binding.country = customer.countryId.asJsonArray.get(1).asString
        } else {
            binding.country = getString(R.string.no_country)
        }

        binding.customer = customer
        this.customer = customer

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        val companySelected: Any = listener!!.onEditPerfilGetCompany()
        if (customer.isCompany) {
            activity!!.findViewById<ConstraintLayout>(R.id.clickableListCompany).visibility = View.INVISIBLE
            view?.findViewById<TextView>(R.id.tvlistCompanies)?.text = getString(R.string.no_company)
        } else {
            activity!!.findViewById<ConstraintLayout>(R.id.clickableListCompany).visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.tvlistCompanies)?.text = (companySelected as Company).name
        }

        val imgView = activity?.findViewById<ImageView>(R.id.ivProfile)
        imgBitmap = (imgView?.drawable as BitmapDrawable).bitmap
    }

    fun dispatchPickPicture() {
        PickImageDialog.build(
                PickSetup()
                        .setTitle(getString(R.string.pick_setup_title))
                        .setCameraButtonText(getString(R.string.pick_setup_camera))
                        .setGalleryButtonText(getString(R.string.pick_setup_gallery))
                        .setCancelText(getString(R.string.pick_setup_cancel))
                        .setProgressText(getString(R.string.pick_setup_progress))
        ).show(context as CustomerProfileActivity)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        val fab: View = activity!!.findViewById(R.id.favIconPhoto)
        fab.visibility = View.INVISIBLE
        compositeDisposable.dispose()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerProfileEditFragment.
         */
        @JvmStatic
        fun newInstance(param1: String) =
                CustomerProfileEditFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_customer_profile_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item != null) {
            when (item.itemId) {
                R.id.action_customer_profile_edit_save -> {
                    /*if (listener != null) {
                        listener?.onEditPerfilButtonSavePressed()
                    }*/
                    persistData()
                }

                R.id.action_customer_profile_edit_cancel -> {
                    showAlertDialog()
                }


                else -> {

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    fun showAlertDialog() {
        val builder = AlertDialog.Builder(context)

        // Set the alert dialog title
        builder.setTitle(getString(R.string.dialog_title))

        builder.setIcon(R.drawable.ic_alert)

        // Display a message on alert dialog
        builder.setMessage(getString(R.string.dialog_message))

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
            // Do something when user press the positive button
            if (activity != null) {
                activity?.findViewById<ImageView>(R.id.ivProfile)?.setImageBitmap(imgBitmap)
            }
            listener = null
            fragmentManager!!.popBackStack()
        }

        // Display a negative button on alert dialog
        builder.setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ ->
            // Do nothing
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }


    fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(context as CustomerProfileActivity, Manifest.permission.CAMERA)
        val writepermission = ContextCompat.checkSelfPermission(context as CustomerProfileActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context as CustomerProfileActivity, listPermissionsNeeded.toTypedArray(), CustomerProfileActivity.REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    fun persistData() {
        val tvlistCountry = view!!.findViewById<TextView>(R.id.tvlistCountry)
        val editTextName = view!!.findViewById<EditText>(R.id.editTextName)
        val checkBoxIsCompany = view!!.findViewById<CheckBox>(R.id.checkBoxIsCompany)
        val editTextWeb = view!!.findViewById<EditText>(R.id.editTextWeb)
        val editTextEmail = view!!.findViewById<EditText>(R.id.editTextEmail)
        val editTextPhone = view!!.findViewById<EditText>(R.id.editTextPhone)
        val editTextMobile = view!!.findViewById<EditText>(R.id.editTextMobile)
        val editTextState = view!!.findViewById<EditText>(R.id.editTextState)
        val editTextStreet = view!!.findViewById<EditText>(R.id.editTextStreet)
        val editTextStreet2 = view!!.findViewById<EditText>(R.id.editTextStreet2)
        val editTextZip = view!!.findViewById<EditText>(R.id.editTextZip)
        val editTextNote = view!!.findViewById<EditText>(R.id.editTextNote)

        val companySelected: Any = listener!!.onEditPerfilGetCompany()
        val countrySelected: Any = listener!!.onEditPerfilGetCountry()

        val companyId: Any
        companyId = if (checkBoxIsCompany.isChecked || (companySelected as Company).id == 0) {
            false
        } else {
            companySelected.id
        }

        val countryId: Any
        countryId = if ((countrySelected as Country).id == 0 || tvlistCountry.text == "no_country") {
            false
        } else {
            countrySelected.id
        }


        imgBitmap = listener!!.onEditPerfilGetBMP()!!
        val byteArrayOutputStream = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedBMP = Base64.encodeToString(byteArray, Base64.DEFAULT)

        Odoo.write(model = "res.partner", ids = listOf(customer.id),
                values = mapOf("name" to editTextName.text.toString(),
                        "website" to editTextWeb.text.toString(),
                        "email" to editTextEmail.text.toString(),
                        "phone" to editTextPhone.text.toString(),
                        "mobile" to editTextMobile.text.toString(),
                        "street" to editTextStreet.text.toString(),
                        "street2" to editTextStreet2.text.toString(),
                        "zip" to editTextZip.text.toString(),
                        "comment" to editTextNote.text.toString(),
                        "is_company" to checkBoxIsCompany.isChecked,
                        "image" to encodedBMP,
                        "country_id" to countryId,
                        "city" to editTextState.text.toString(),
                        "parent_id" to companyId)) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val write = response.body()!!
                    if (write.isSuccessful) {
                        val result = write.result

                    } else {
                        // Odoo specific error
                        Timber.w("write() failed with ${write.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                prefs.isContactProfileEdited(true)
                Toast.makeText(context, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()
                fragmentManager!!.popBackStack()
            }
        }
    }
}


