package com.domatix.yevbes.nucleus.core.web.route

import com.domatix.yevbes.nucleus.core.entities.route.Route
import com.domatix.yevbes.nucleus.core.entities.route.RouteReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RouteRequest {

    @POST("/{path1}/{path2}")
    fun route(
            @Path("path1") path1: String,
            @Path("path2") path2: String,
            @Body routeReqBody: RouteReqBody
    ): Observable<Response<Route>>
}