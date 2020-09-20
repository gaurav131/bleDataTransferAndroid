package com.niltechcorp.bluetoothble.home

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomePageViewModelFactory (
    val context: Context,
    val bluetoothAdapter: BluetoothAdapter,
    val handler: Handler
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePageViewModel::class.java)) {
            return HomePageViewModel(context, bluetoothAdapter, handler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}