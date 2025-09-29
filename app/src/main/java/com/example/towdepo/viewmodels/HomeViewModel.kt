package com.example.towdepo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.ApiProduct
import com.example.towdepo.repository.ProductRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<ApiProduct>>()
    val products: LiveData<List<ApiProduct>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadProducts() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val productsList = productRepository.getAllProducts()
                _products.value = productsList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}