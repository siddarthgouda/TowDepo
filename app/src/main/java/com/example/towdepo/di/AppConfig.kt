package com.example.towdepo.di


object AppConfig {
    // Replace with your actual AWS server IP
    private const val AWS_SERVER_IP = "13.233.45.67"
    private const val SERVER_PORT = "3501"


    // Base URLs
    const val DEV_BASE_URL = "http://10.0.2.2:3501/v1/" // For emulator
    const val PROD_BASE_URL = "http://$AWS_SERVER_IP:$SERVER_PORT/v1/" // For AWS server

    // Image Base URLs
    const val DEV_IMAGE_BASE_URL = "http://10.0.2.2:3501/uploads/product/"
    const val PROD_IMAGE_BASE_URL = "http://$AWS_SERVER_IP:$SERVER_PORT/uploads/product/"

    // Switch between environments
    const val USE_PRODUCTION = false // Set to true for AWS server

    // Get the appropriate base URL
    fun getBaseUrl(): String {
        return if (USE_PRODUCTION) PROD_BASE_URL else DEV_BASE_URL
    }

    // Get the appropriate image base URL
    fun getImageBaseUrl(): String {
        return if (USE_PRODUCTION) PROD_IMAGE_BASE_URL else DEV_IMAGE_BASE_URL
    }

    // Helper to log which environment we're using
    fun getEnvironmentInfo(): String {
        return if (USE_PRODUCTION) {
            "Production - AWS Server: $PROD_BASE_URL"
        } else {
            "Development - Local Server: $DEV_BASE_URL"
        }
    }
}