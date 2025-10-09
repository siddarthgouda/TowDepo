package com.example.towdepo.utils


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.towdepo.di.AppContainer
import com.example.towdepo.security.TokenManager
import com.example.towdepo.viewmodels.AuthViewModel
import com.example.towdepo.viewmodels.WishlistViewModel


class AuthViewModelFactory(
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WishlistViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            return WishlistViewModel(appContainer.wishlistRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}