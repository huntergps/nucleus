package com.domatix.yevbes.nucleus.core.web.dataset.load

import com.domatix.yevbes.nucleus.core.entities.dataset.load.Load
import com.domatix.yevbes.nucleus.core.entities.dataset.load.LoadReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoadRequest {

    @POST("/web/dataset/load")
    fun load(
            @Body loadReqBody: LoadReqBody
    ): Observable<Response<Load>>
}