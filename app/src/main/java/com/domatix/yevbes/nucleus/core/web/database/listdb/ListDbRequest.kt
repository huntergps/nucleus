package com.domatix.yevbes.nucleus.core.web.database.listdb

import com.domatix.yevbes.nucleus.core.entities.database.listdb.ListDb
import com.domatix.yevbes.nucleus.core.entities.database.listdb.ListDbReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ListDbRequest {

    @POST("/web/database/list")
    fun listDb(
            @Body listDbReqBody: ListDbReqBody
    ): Observable<Response<ListDb>>
}