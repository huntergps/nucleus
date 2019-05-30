package com.domatix.yevbes.nucleus.utils

interface ConstantManager {
    // SHARED PREFS CONTACT PROFILE
    companion object {

        // Language
        const val EN = "en"
        const val ES = "es"
        const val LOCALE_LANGUAGE = "LOCALE_LANGUAGE"

        const val IS_EDITED = "IS_EDITED"

        // SharedPreferences
        const val START_TIME: String = "START_TIME"
        const val END_TIME: String = "END_TIME"

        // WorksheetDataAdapter
        const val WORKSHEET_DATA_ADAPTER_TAG = "WorksheetAdapter"
        const val VIEW_TYPE_ITEM = 0

        const val WORK_ORDER = "WORK_ORDER"

        // WorkheetDataAdapter tabs
        const val FRAGMENT_PIECES = 0
        const val FRAGMENT_OPERATIONS = 1
        const val FRAGMENT_TIME_TRACKING = 2
        const val FRAGMENT_NOTES = 3
        const val FRAGMENT_SIGNATURE = 4
        const val TYPE = "TYPE"

        // GENERAL ADAPTER ACTIVITY
        const val GENERAL_ADAPTER_ACTIVITY: String = "GENERAL_ADAPTER_ACTIVITY"
        const val PIECES_PRODUCT: Int = 0
        const val PIECES_LOCATION_ORIGIN: Int = 1
        const val PIECES_LOCATION_DESTINATION: Int = 2
        const val LOT_SERIAL_NUMBER: Int = 4
        //        const val PIECES_PRODUCTS_PARTS
        const val PIECES_TITLE: String = "PIECES_TITLE"

        const val CUSTOMER_TYPE: Int = 0
        const val SHIPPING_ADDRESS_TYPE: Int = 1
        const val CATEGORY_TYPE: Int = 3
        const val RESPONSIBLE_TYPE: Int = 2
    }
}