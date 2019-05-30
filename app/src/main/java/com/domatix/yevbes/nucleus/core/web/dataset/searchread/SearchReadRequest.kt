package com.domatix.yevbes.nucleus.core.web.dataset.searchread

import com.domatix.yevbes.nucleus.core.entities.dataset.searchread.SearchRead
import com.domatix.yevbes.nucleus.core.entities.dataset.searchread.SearchReadReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SearchReadRequest {

    @POST("/web/dataset/search_read")
    fun searchRead(
            @Body searchReadReqBody: SearchReadReqBody
    ): Observable<Response<SearchRead>>
}
