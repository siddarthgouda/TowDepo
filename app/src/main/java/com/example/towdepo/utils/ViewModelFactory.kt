package com.example.towdepo.utils


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.towdepo.api.RetrofitInstance
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.viewmodels.ProductViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            val repository = ProductRepository(RetrofitInstance.productApi)
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}