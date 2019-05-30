package com.domatix.yevbes.nucleus.customer

import android.graphics.Bitmap
import com.domatix.yevbes.nucleus.company.entities.Company
import com.domatix.yevbes.nucleus.country.entities.Country

interface PersistDataListener {
    fun onEditPerfilGetBMP(): Bitmap?
    fun onEditPerfilGetCountry(): Country
    fun onEditPerfilGetCompany(): Company
}