package com.example.towdepo.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.towdepo.repository.AddressRepository
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.viewmodels.CheckoutViewModel

class CheckoutViewModelFactory(
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(addressRepository,cartRepository = cartRepository, userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


