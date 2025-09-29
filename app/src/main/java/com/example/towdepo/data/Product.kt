package com.example.towdepo.data

// Updated data classes to match the actual API response
data class ProductResponse(
    val results: List<ApiProduct>,
    val page: Int,
    val limit: String,
    val totalPages: Int?,
    val totalResults: Int
)

data class ApiProduct(
    val id: String,
    val inStock: Boolean,
    val category: ApiCategory,
    val title: String,
    val mrp: Double,
    val brand: String?,
    val discount: String,
    val images: List<ApiImage>,
    val variant: List<ApiVariant>,
    val SKU: String,
    val productInfo: List<Any>,
    val productSpec: List<Any>,
    val created_on: String
) {
    // Helper function to get full image URL (for backwards compatibility with your UI code)
    fun getFirstImageUrl(): String? {
        return images.firstOrNull()?.src?.let { imageSrc ->
            "YOUR_BASE_URL/$imageSrc" // Replace with your actual image base URL
        }
    }

    // Get the first variant's price (most common use case)
    fun getCurrentPrice(): Double {
        return variant.firstOrNull()?.price ?: mrp
    }

    // Get total stock quantity from all variants
    fun getStockQuantity(): Int {
        return variant.sumOf { it.quantity }
    }

    // Get first variant's images
    fun getVariantImages(): List<String> {
        return variant.firstOrNull()?.images ?: emptyList()
    }
}

data class ApiCategory(
    val id: String,
    val name: String
)

data class ApiImage(
    val id: String,
    val src: String
) {
    // Helper property to generate full URL
    val url: String
        get() = "YOUR_BASE_URL/$src" // Replace with your actual image base URL
}

data class ApiVariant(
    val id: String,
    val sku: String,
    val quantity: Int,
    val price: Double,
    val images: List<String>,
    val attributes: List<ApiAttribute>
) {
    // Helper to get attribute value by name
    fun getAttribute(name: String): String? {
        return attributes.find { it.name == name }?.value
    }
}

data class ApiAttribute(
    val id: String,
    val name: String,
    val value: String
)