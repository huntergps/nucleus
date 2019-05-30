package com.domatix.yevbes.nucleus.core.web.session.destroy

import com.domatix.yevbes.nucleus.core.entities.session.destroy.Destroy
import com.domatix.yevbes.nucleus.core.entities.session.destroy.DestroyReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DestroyRequest {

    @POST("/web/session/destroy")
    fun destroy(
            @Body destroyReqBody: DestroyReqBody
    ): Observable<Response<Destroy>>
}
