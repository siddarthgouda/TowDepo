package com.example.towdepo.api

import com.example.towdepo.data.Address
import com.example.towdepo.data.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AddressService {

    @GET("/v1/address")
    suspend fun getAddresses(): ApiResponse<List<Address>>

    @POST("/v1/address")
    suspend fun createAddress(@Body address: Address): Address

    @GET("/v1/address/user/{userId}")
    suspend fun getAddressesByUserId(
        @Path("userId") userId: String
    ):  List<Address>

    @PATCH("/v1/address/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: String,
        @Body address: Address
    ): Address

    @DELETE("/v1/address/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: String): Response<Void>
}