package com.example.towdepo.data

import com.google.gson.annotations.SerializedName

data class WishlistItem(
    @SerializedName("_id") val id: WishlistId? = null,
    val title: String,
    val product: ApiProduct?,
    val mrp: Double,
    val discount: String,
    val brand: String = "N/A",
    val image: String? = null,
    val user: User,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class WishlistId(
    @SerializedName("\$oid") val oid: String
)

data class WishlistResponse(
    val results: List<WishlistItem>,
    val page: Int,
    val limit: String,
    val totalPages: Int?,
    val totalResults: Int
){
    // Safe getter for limit
    val limitInt: Int
        get() = try {
            limit.takeIf { it.isNotBlank() }?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            0
        }
}

data class AddToWishlistRequest(
    val title: String,
    val product: String,
    val mrp: Double,
    val discount: String?,
    val brand: String = "N/A",
    val image: String? = null
)

data class WishlistItemResponse(
    val id: String,
    val title: String,
    val product: String,
    val mrp: Double,
    val discount: String,
    val brand: String,
    val image: String
)