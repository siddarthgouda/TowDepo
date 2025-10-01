package com.example.towdepo.repository

import com.example.towdepo.api.ProductApiService

import com.example.towdepo.data.ApiProduct
import com.example.towdepo.di.AppContainer

class ProductRepository(
    private val apiService: ProductApiService = AppContainer.productApiService
) {

    suspend fun getAllProducts(): List<ApiProduct> {
        return try {
            val response = apiService.getAllProducts()
            response.results // Extract products from the results field
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductById(id: String): ApiProduct? {
        return try {
            apiService.getProductById(id)
        } catch (e: Exception) {
            null
        }
    }
}