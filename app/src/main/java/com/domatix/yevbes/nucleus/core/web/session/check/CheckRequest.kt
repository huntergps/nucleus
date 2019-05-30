package com.domatix.yevbes.nucleus.core.web.session.check

import com.domatix.yevbes.nucleus.core.entities.session.check.Check
import com.domatix.yevbes.nucleus.core.entities.session.check.CheckReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckRequest {

    @POST("/web/session/check")
    fun check(
            @Body checkReqBody: CheckReqBody
    ): Observable<Response<Check>>
}
