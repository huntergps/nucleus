package com.domatix.yevbes.nucleus.core.entities.webclient.versionInfo

import com.domatix.yevbes.nucleus.core.Odoo
import com.google.gson.JsonArray
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VersionInfoResult(

        @field:Expose
        @SerializedName("server_serie")
        val serverSerie: String = "",


        @Expose
        @SerializedName("server_version_info")
        val serverVersionInfo: JsonArray = JsonArray(),

        @Expose
        @SerializedName("server_version")
        val serverVersion: String = "",

        @Expose
        @SerializedName("protocol_version")
        val protocolVersion: Int = 0
) {
    val serverVersionType: String
        get() = if (serverVersionInfo.size() > 0) serverVersionInfo[3].asString else ""

    val isServerVersionEnterprise: Boolean
        get() = if (serverVersionInfo.size() > 0) serverVersionInfo[5].asString.contains("e", true) else false

    val serverVersionIsSupported: Boolean
        get() = Odoo.supportedOdooVersions.any { serverVersion.startsWith(it) }
}