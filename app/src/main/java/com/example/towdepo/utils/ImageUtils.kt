package com.example.towdepo.utils


import com.example.towdepo.di.AppConfig

object ImageUtils {
    /**
     * Builds complete product image URL from relative path
     */
    fun getProductImageUrl(imageSrc: String): String {
        return AppConfig.getImageBaseUrl() + imageSrc
    }

    /**
     * Safe image URL builder - handles empty or null paths
     */
    fun getSafeProductImageUrl(imageSrc: String?): String? {
        return if (imageSrc.isNullOrEmpty()) {
            null
        } else {
            getProductImageUrl(imageSrc)
        }
    }

    /**
     * For placeholder images when no product image is available
     */
    fun getPlaceholderImageUrl(): String {
        return "https://via.placeholder.com/300x300/CCCCCC/969696?text=No+Image"
    }
}