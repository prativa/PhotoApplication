package com.example.photoapplication.ui.theme.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhotoViewModel : ViewModel() {
    private var _itemNumber = MutableStateFlow(0)
    var itemNumber = _itemNumber.asStateFlow()


    private var _imageUris = MutableStateFlow( arrayListOf<Uri>())
    var imageUris = _imageUris.asStateFlow()

    fun populateImageUris(imageUris : ArrayList<Uri>) = viewModelScope.launch {
        _imageUris.update { imageUris }
    }

    fun updateItemNumber(number : Int) = viewModelScope.launch {
        _itemNumber.update { number }
    }
}