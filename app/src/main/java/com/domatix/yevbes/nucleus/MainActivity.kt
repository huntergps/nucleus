package com.domatix.yevbes.nucleus

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.domatix.yevbes.nucleus.about.AboutActivity
import com.domatix.yevbes.nucleus.activities.ActivitiesFragment
import com.domatix.yevbes.nucleus.core.authenticator.ProfileActivity
import com.domatix.yevbes.nucleus.core.preferences.SettingsActivity
import com.domatix.yevbes.nucleus.core.utils.NavHeaderViewHolder
import com.domatix.yevbes.nucleus.core.utils.android.ktx.postEx
import com.domatix.yevbes.nucleus.customer.CustomerFragment
import com.domatix.yevbes.nucleus.databinding.ActivityMainBinding
import com.domatix.yevbes.nucleus.sales.fragments.SalesFragment
import com.domatix.yevbes.nucleus.sga.view.ui.DashboardInventoryFragment
import com.domatix.yevbes.nucleus.sga.view.ui.InventorySettingsFragment
import com.domatix.yevbes.nucleus.sga.view.ui.StoreFragment
import com.domatix.yevbes.nucleus.sga.view.ui.TransfersFragment

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val REQUEST_CODE = 0
        private const val ACTION_CONTACT = 1
        private const val ACTION_ACTIVITIES = 2
        private const val ACTION_SALES = 3
        private const val ACTION_SGA = 4
        private const val ACTION_DASHBOARD_INVENTORY = 5
        const val ACTION_TRANSFERS_INVENTORY = 6
        private const val ACTION_INVENTORY_SETTINGS = 8
    }

    private var isInventoryExpanded = false
    lateinit var app: App private set
    lateinit var binding: ActivityMainBinding private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navHeader: NavHeaderViewHolder
    private var doubleBackToExitPressedOnce = false
    private var currentDrawerItemID: Int = 0
    private var drawerClickStatus: Boolean = false

    /*private val customerFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Customer)
    }

    private val supplierFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Supplier)
    }

    private val companyFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Company)
    }*/

    private val contactsFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Contacts)
    }

    private val activitiesFragment: ActivitiesFragment by lazy {
        ActivitiesFragment.newInstance(ActivitiesFragment.Companion.ActivityType.Activity)
    }

    private val salesFragment: SalesFragment by lazy {
        SalesFragment.newInstance(SalesFragment.Companion.SalesType.SaleOrder)
    }

    private val storeFragment: StoreFragment by lazy {
        StoreFragment()
    }

    private val settingsInventoryFragment: InventorySettingsFragment by lazy {
        InventorySettingsFragment()
    }

    private val dashboardInventoryFragment: DashboardInventoryFragment by lazy {
        DashboardInventoryFragment()
    }

    private val transfersFragment: TransfersFragment by lazy {
        TransfersFragment()
    }

//    private val aboutFragment: AboutFragment by lazy {
//        AboutFragment()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.tb)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.tb.setNavigationOnClickListener {
            binding.dl.openDrawer(GravityCompat.START)
        }
        setTitle(R.string.app_name)

        drawerToggle = ActionBarDrawerToggle(
                this, binding.dl, binding.tb,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()


        val view = binding.nv.getHeaderView(0)
        if (view != null) {
            navHeader = NavHeaderViewHolder(view)
            val user = getActiveOdooUser()
            if (user != null) {
                navHeader.setUser(user)
            }
        }

        drawerClickStatus = false

        navHeader.menuToggle.setOnClickListener {
            val menu = binding.nv.menu
            if (drawerClickStatus) {
                //menu.setGroupVisible(R.id.nav_menu_1, true)
                //menu.setGroupVisible(R.id.nav_menu_2, true)
                //menu.setGroupVisible(R.id.nav_menu_3, true)
                //menu.setGroupVisible(R.id.nav_menu_4, false)

                menu.setGroupVisible(R.id.nav_menu_5, true)
                menu.findItem(R.id.nav_menu_6).isVisible = true
                menu.setGroupVisible(R.id.nav_general_group, true)

                if (isInventoryExpanded) {
                    menu.findItem(R.id.nav_inventory_selectable).isVisible = false
                    menu.findItem(R.id.nav_menu_inventory).isVisible = true
                }
                menu.setGroupVisible(R.id.nav_menu_7, false)
                navHeader.menuToggleImage.setImageResource(R.drawable.ic_arrow_drop_down_white_24dp)
            } else {
                //menu.setGroupVisible(R.id.nav_menu_1, false)
                //menu.setGroupVisible(R.id.nav_menu_2, false)
                //menu.setGroupVisible(R.id.nav_menu_3, false)
                //menu.setGroupVisible(R.id.nav_menu_4, true)
//                menu.findItem(R.id.nav_menu_6).isVisible = false
//                menu.findItem(R.id.nav_menu_inventory).isVisible = false
                menu.setGroupVisible(R.id.nav_menu_5, false)
                menu.findItem(R.id.nav_menu_6).isVisible = false
                menu.findItem(R.id.nav_menu_inventory).isVisible = false
                menu.setGroupVisible(R.id.nav_general_group, false)
                menu.setGroupVisible(R.id.nav_menu_7, true)
                navHeader.menuToggleImage.setImageResource(R.drawable.ic_arrow_drop_up_white_24dp)
            }
            drawerClickStatus = !drawerClickStatus
        }

        binding.nv.setNavigationItemSelectedListener { item ->
            if (item.itemId != R.id.nav_inventory_selectable)
                binding.dl.postEx { closeDrawer(GravityCompat.START) }

            when (item.itemId) {
                R.id.nav_contacts -> {
                    if (currentDrawerItemID != ACTION_CONTACT) {
                        loadFragment(ACTION_CONTACT)
                        itemsGroupInventory(false)
                    }
                    true
                }

                R.id.nav_activities -> {
                    if (currentDrawerItemID != ACTION_ACTIVITIES) {
                        loadFragment(ACTION_ACTIVITIES)
                        itemsGroupInventory(false)
                    }
                    //startActivity(Intent(this, ActivitiesActivity::class.java))
                    true
                }


                R.id.nav_sales -> {
                    if (currentDrawerItemID != ACTION_SALES) {
                        loadFragment(ACTION_SALES)
                        itemsGroupInventory(false)
                    }
                    //startActivity(Intent(this, ActivitiesActivity::class.java))
                    true
                }

//                R.id.nav_help -> {
//                    if (currentDrawerItemID != ACTION_ABOUT) {
//                        loadFragment(ACTION_ABOUT)
//                        itemsGroupInventory(false)
//                    }
//                    true
//                }

                R.id.nav_inventory_selectable -> {
                    if (currentDrawerItemID != ACTION_DASHBOARD_INVENTORY) {
                        loadFragment(ACTION_DASHBOARD_INVENTORY)
                        itemsGroupInventory(true)
                        Handler().postDelayed({
                            binding.dl.postEx { closeDrawer(GravityCompat.START) }
                        }, 2000)
                    }
                    true
                }

                R.id.nav_dashboard_inventory -> {
                    if (currentDrawerItemID != ACTION_DASHBOARD_INVENTORY) {
                        loadFragment(ACTION_DASHBOARD_INVENTORY)
                        itemsGroupInventory(true)
                    }
                    true
                }

                R.id.nav_inventory_transference -> {
                    if (currentDrawerItemID != ACTION_TRANSFERS_INVENTORY) {
                        loadFragment(ACTION_TRANSFERS_INVENTORY)
                        itemsGroupInventory(true)
                    }
                    true
                }

                R.id.nav_inventory_tools -> {
                    if (currentDrawerItemID != ACTION_INVENTORY_SETTINGS) {
                        loadFragment(ACTION_INVENTORY_SETTINGS)
                        itemsGroupInventory(true)
                    }
                    true
                }

                R.id.nav_sga -> {
                    if (currentDrawerItemID != ACTION_SGA) {
                        loadFragment(ACTION_SGA)
                        itemsGroupInventory(true)
                    }
                    true
                }

                R.id.nav_help -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    if (getActiveOdooUser() != null) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    } else {
                        showMessage(message = getString(R.string.error_active_user))
                    }
                    true
                }

                R.id.nav_settings -> {

                    startActivityForResult(Intent(this, SettingsActivity::class.java),
                            REQUEST_CODE)
                    true
                }

                else -> {
                    true
                }
            }
        }

        if (savedInstanceState == null) {
            loadFragment(ACTION_SALES)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        binding.nv.menu.clear()
                        binding.nv.inflateMenu(R.menu.menu_main_nav)
                        loadFragment(ACTION_CONTACT)
                        binding.nv.menu.findItem(R.id.nav_contacts).isChecked = true
//                        val id = data?.extras?.getInt("MENU_ITEM_ID")
//                        if (id != null) {
//                            when (id) {
//                                R.id.nav_dashboard_inventory -> {
//                                    itemsGroupInventory(true)
//                                }
//                                R.id.nav_sga -> {
//                                    itemsGroupInventory(true)
//                                }
//                                R.id.nav_inventory_tools -> {
//                                    itemsGroupInventory(true)
//                                }
//                                R.id.nav_inventory_transference -> {
//                                    itemsGroupInventory(true)
//                                }
//                                else -> {
//                                    itemsGroupInventory(false)
//                                }
//                            }
//                            binding.nv.menu.findItem(id).isChecked = true
//                        }
                    }
                }
            }
        }
    }

    private fun itemsGroupInventory(check: Boolean) {
        val menu = binding.nv.menu
        if (check) {
            isInventoryExpanded = true
            menu.findItem(R.id.nav_menu_inventory).isVisible = true
            menu.findItem(R.id.nav_inventory_selectable).isVisible = false
        } else {
            isInventoryExpanded = false
            menu.findItem(R.id.nav_menu_inventory).isVisible = false
            menu.findItem(R.id.nav_inventory_selectable).isVisible = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun loadFragment(currentDrawerItemID: Int) {
        clearBackStack()
        this.currentDrawerItemID = currentDrawerItemID
        when (currentDrawerItemID) {
            ACTION_CONTACT -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, contactsFragment)
                        .commit()
            }

            ACTION_ACTIVITIES -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, activitiesFragment, getString(R.string.action_activities))
                        .commit()
            }

            ACTION_SALES -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, salesFragment, getString(R.string.action_sales))
                        .commit()
            }

            ACTION_SGA -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, storeFragment, getString(R.string.action_store))
                        .commit()
            }
//            ACTION_ABOUT -> {
//                supportFragmentManager
//                        .beginTransaction()
//                        .replace(R.id.clMain, aboutFragment, getString(R.string.action_help))
//                        .commit()
//            }
            ACTION_DASHBOARD_INVENTORY -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, dashboardInventoryFragment, getString(R.string.action_dashboard_inventory))
                        .commit()
            }

            ACTION_INVENTORY_SETTINGS -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, settingsInventoryFragment, getString(R.string.action_inventory_tools))
                        .commit()
            }

            ACTION_TRANSFERS_INVENTORY -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, transfersFragment, getString(R.string.action_inventory_transference))
                        .commit()
            }
        }
    }

    fun replaceFragment(fragment: Fragment, currentItemID: Int) {
        currentDrawerItemID = currentItemID

        clearBackStack()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.clMain, fragment, getString(R.string.action_inventory_transference))
                .commit()

        when (currentItemID) {
            ACTION_TRANSFERS_INVENTORY -> {
                itemsGroupInventory(true)
            }
        }
    }

    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        for (i in 0 until fragmentManager.backStackEntryCount) {
            fragmentManager.popBackStackImmediate()
        }
    }


/*override fun onBackPressed() {


    if (doubleBackToExitPressedOnce) {
        super.onBackPressed()
        return
    }

    this.doubleBackToExitPressedOnce = true
    Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

    Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
}*/
}
