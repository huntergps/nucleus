package com.domatix.yevbes.nucleus.customer

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.FragmentCustomerProfileBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.utils.PreferencesManager
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CustomerProfileFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CustomerProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CustomerProfileFragment : Fragment() {

    //private var listener: OnFragmentInteractionListener? = null
    private lateinit var binding: FragmentCustomerProfileBinding
    private lateinit var customerGsonAsAString: String
    lateinit var prefs: PreferencesManager private set
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var activityCustomerProfile: CustomerProfileActivity
    lateinit var customer: Customer private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        customerGsonAsAString = arguments!!.getString(ARG_PARAM1)
        val customerGson = Gson()
        customer = customerGson.fromJson(customerGsonAsAString, Customer::class.java)
    }

    private fun readCustomer(id: Int) {
        Odoo.load(id, "res.partner") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }
            onNext { response ->
                if (response.isSuccessful) {
                    val load = response.body()!!
                    if (load.isSuccessful) {
                        val result = load.result
                        val item: Customer = gson.fromJson(result.value, Customer::class.java)
                        customer = item // ...
                    } else {
                        // Odoo specific error
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
                setBindings(customer)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_profile, container, false)
        prefs = PreferencesManager
        compositeDisposable = CompositeDisposable()

        /*if (prefs.isContainsProfileContact() && prefs.isContactProfileEdited()) {
            readCustomer(customer.id)
            prefs.isContactProfileEdited(false)
        }else{
            setBindings(customer)
        }*/
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityCustomerProfile = (activity as CustomerProfileActivity)
        readCustomer(customer.id)
    }

    private fun setBindings(customer: Customer) {
        if (customer.countryId.isJsonArray) {
            binding.country = customer.countryId.asJsonArray.get(1).asString
        } else {
            binding.country = getString(R.string.country)
        }

        if (customer.stateId.isJsonArray) {
            binding.state = customer.stateId.asJsonArray.get(1).asString
        } else {
            binding.state = getString(R.string.state)
        }

        if (customer.isCompany) {
            binding.company = "Is Company"

        } else {
            binding.imageViewCompany.visibility = View.GONE
            binding.tvCompany.visibility = View.GONE
         /*   binding.linearLayoutCustomerProfile.removeView(binding.imageViewCompany)
            binding.linearLayoutCustomerProfile.removeView(binding.tvCompany)*/

        }

        if (!customer.fullAddress.equals("\n\n  \n")) {

            val gmmIntentUri = Uri.parse("geo:0,0?q=${customer.fullAddress}")

            val intent = Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
            intent.setPackage("com.google.android.apps.maps")
            binding.tvFullAddress.setOnClickListener {
                startActivity(intent)
            }

            binding.tvFullAddress.setTextColor(context?.let { ContextCompat.getColor(it, R.color.colorCustomerAddress) }!!)
        }
        customerGsonAsAString = gson.toJson(customer)
        binding.customer = customer

        if (::activityCustomerProfile.isInitialized) {
            activityCustomerProfile.binding.name = customer.name
            activityCustomerProfile.binding.imageSmall = customer.imageSmall
            activityCustomerProfile.binding.executePendingBindings()
        }

        binding.executePendingBindings()
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }*/

    private var listener: CustomerProfileFragment.OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    interface OnFragmentInteractionListener {
        fun onBackPressFragm()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()

        val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setNavigationOnClickListener {
            listener?.onBackPressFragm()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment CustomerProfileFragment.
         */

        @JvmStatic
        fun newInstance(param1: String) =
                CustomerProfileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_customer_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item != null) {
            when (item.itemId) {
                R.id.action_customer_profile_edit -> {
                    val customerProfileEditFragment = CustomerProfileEditFragment.newInstance(customerGsonAsAString)
                    fragmentManager!!.beginTransaction()
                            .replace(R.id.container, customerProfileEditFragment, CustomerProfileActivity.CUSTOMER_PROFILE_EDIT_FRAG_TAG)
                            .addToBackStack(null)
                            .commit()
                }

                R.id.action_customer_profile_refresh -> {
                    readCustomer(customer.id)
                }

                R.id.action_customer_profile_share -> {
                    Toast.makeText(context, "Shared", Toast.LENGTH_SHORT).show()
                }

                R.id.action_customer_profile_import -> {
                    Toast.makeText(context, "Imported", Toast.LENGTH_SHORT).show()
                }

                else -> {

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
