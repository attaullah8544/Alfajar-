package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NominatimResult(
    @param:Json(name = "place_id") val placeId: Long? = null,
    @param:Json(name = "lat") val lat: String,
    @param:Json(name = "lon") val lon: String,
    @param:Json(name = "display_name") val displayName: String,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "type") val type: String? = null,
    @param:Json(name = "address") val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    @param:Json(name = "city") val city: String? = null,
    @param:Json(name = "town") val town: String? = null,
    @param:Json(name = "village") val village: String? = null,
    @param:Json(name = "municipality") val municipality: String? = null,
    @param:Json(name = "state") val state: String? = null,
    @param:Json(name = "country") val country: String? = null,
    @param:Json(name = "country_code") val countryCode: String? = null
) {
    val bestCityName: String?
        get() = city ?: town ?: village ?: municipality
}

interface GeocodingService {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("accept-language") acceptLanguage: String = "en",
        @Header("User-Agent") userAgent: String = "AlFajrWatchApp/1.0 (Android Retro Prayer Watch)"
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") acceptLanguage: String = "en",
        @Header("User-Agent") userAgent: String = "AlFajrWatchApp/1.0 (Android Retro Prayer Watch)"
    ): NominatimResult
}
