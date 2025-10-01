// Product.kt
package com.example.towdepo.data
// Product.kt


data class ProductApiResponse(
    val results: List<ApiProduct>,
    val page: Int,
    val limit: String?,
    val totalPages: Int?,
    val totalResults: Int
)

data class ApiProduct(
    val id: String, // Changed from _id to match your backend response
    val title: String,
    val mrp: Double,
    val category: Category,
    val inStock: Boolean,
    val SKU: String,
    val createdAt: String? = null,
    val created_on: String,
    val description: String? = null,
    val discount: String? = null,
    val brand: Brand? = null,
    val images: List<Image> = emptyList(),
    val variant: List<Variant> = emptyList(), // Added variant field
    val productInfo: List<ProductInfo> = emptyList(), // Added productInfo field
    val productSpec: List<ProductSpec> = emptyList() // Added productSpec field
) {
    fun getAllImageUrls(): List<String> {
        return images.mapNotNull { it.src } // Changed from url to src
    }

    fun getStockQuantity(): Int {
        return if (inStock) 1 else 0
    }

    val discountedPrice: Double
        get() = mrp - (mrp * (discount?.toDoubleOrNull() ?: 0.0) / 100)
}

data class Category(
    val id: String, // Changed from _id to id
    val name: String
)

data class Brand(
    val id: String, // Changed from _id to id
    val name: String
)

data class Image(
    val id: String, // Changed from _id to id
    val src: String // Changed from url to src (matches your backend)
)

data class Variant(
    val id: String,
    val sku: String,
    val quantity: Int,
    val price: Double,
    val images: List<String>,
    val attributes: List<Attribute>
)

data class Attribute(
    val id: String,
    val name: String,
    val value: String
)

data class ProductInfo(
    val id: String,
    // Add fields based on your ProductInfo model
)

data class ProductSpec(
    val id: String,
    // Add fields based on your ProductSpec model
)