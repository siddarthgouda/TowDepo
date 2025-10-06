package com.example.towdepo.data

import com.google.gson.annotations.SerializedName

// Cart-specific data classes only
data class CartApiResponse(
    @SerializedName("results")
    val results: List<CartItem>,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: String,

    @SerializedName("totalPages")
    val totalPages: Int?,

    @SerializedName("totalResults")
    val totalResults: Int?
)

data class CartItem(
    @SerializedName("_id")
    val mongoId: MongoId? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("product")
    val product: CartProduct, // Simplified product for cart

    @SerializedName("mrp")
    val mrp: Double,

    @SerializedName("discount")
    val discount: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("user")
    val user: CartUser,

    @SerializedName("__v")
    val version: Int = 0
) {
    val safeId: String
        get() = mongoId?.oid ?: id ?: ""

    val quantity: Int
        get() = count

    val productImage: String?
        get() = product.images.firstOrNull()?.src
}

data class MongoId(
    @SerializedName("\$oid")
    val oid: String
)

data class CartUser(
    @SerializedName("_id")
    val mongoId: MongoId? = null,

    @SerializedName("id")
    val id: String? = null
)

// Simplified product for cart (don't reuse your main Product class)
data class CartProduct(
    @SerializedName("_id")
    val mongoId: MongoId? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("images")
    val images: List<CartProductImage> = emptyList()
)

data class CartProductImage(
    @SerializedName("src")
    val src: String = ""
)

// Request classes for cart operations only
data class AddToCartRequest(
    val product: CartProductRequest,
    val quantity: Int
)

data class CartProductRequest(
    val id: String,
    val title: String,
    val mrp: Double,
    val discount: String?,
    val brand: String?
)

data class UpdateCartRequest(
    val count: Int
)