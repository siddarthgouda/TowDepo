package com.example.towdepo.repository

import com.example.towdepo.data.ApiProduct
import com.example.towdepo.api.ProductApiService

class ProductRepository(private val productApi: ProductApiService) {

    suspend fun getAllProducts(): List<ApiProduct> {
        return try {
            println("🔄 [DEBUG] Calling getAllProducts()")
            val response = productApi.getAllProducts()
            println("✅ [DEBUG] Success! Got ${response.results.size} products")
            println("✅ [DEBUG] Total results: ${response.totalResults}, Page: ${response.page}")
            response.results
        } catch (e: Exception) {
            println("❌ [DEBUG] API Call Failed: ${e.message}")
            println("❌ [DEBUG] Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw Exception("Failed to fetch products: ${e.message}")
        }
    }

    suspend fun getProductById(id: String): ApiProduct {
        return try {
            println("🔄 [DEBUG] Fetching product with ID: $id")
            productApi.getProductById(id)
        } catch (e: Exception) {
            println("❌ [DEBUG] Failed to fetch product: ${e.message}")
            throw Exception("Failed to fetch product: ${e.message}")
        }
    }
}