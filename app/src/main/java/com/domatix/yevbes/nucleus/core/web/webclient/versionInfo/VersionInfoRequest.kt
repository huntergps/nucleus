package com.domatix.yevbes.nucleus.core.web.webclient.versionInfo

import com.domatix.yevbes.nucleus.core.entities.webclient.versionInfo.VersionInfo
import com.domatix.yevbes.nucleus.core.entities.webclient.versionInfo.VersionInfoReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VersionInfoRequest {

    @POST("/web/webclient/version_info")
    fun versionInfo(
            @Body versionInfoReqBody: VersionInfoReqBody
    ): Observable<Response<VersionInfo>>
}