package com.niltechcorp.bluetoothble.home

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.niltechcorp.bluetoothble.R
import com.niltechcorp.bluetoothble.databinding.HomePageFragmentBinding

class HomePage : Fragment() {

    private lateinit var viewModel: HomePageViewModel
    lateinit var binding: HomePageFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_page_fragment, container, false)
        val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val handler =  Handler()
        val viewModelFactory = HomePageViewModelFactory(requireContext(), bluetoothAdapter, handler)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomePageViewModel::class.java)
        viewModel.scanLeDevice(true)
        binding.lifecycleOwner = this
        viewModel.device.observe(viewLifecycleOwner, Observer{
            it?.let {
                if (it is ScanResult){
                    binding.deviceData.text = "Device Found ${it.device.name}"
                    viewModel.connect()
                }
                else{
                    binding.deviceData.text = it.toString()
                }
            }
        })
        binding.read.setOnClickListener {
            viewModel.read()
        }
        return binding.root
    }

}

