package com.niltechcorp.bluetoothble.home

import android.app.PendingIntent.getActivity
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.niltechcorp.bluetoothble.R
import com.niltechcorp.bluetoothble.databinding.HomePageFragmentBinding
import java.util.*
import kotlin.collections.ArrayList

class HomePageViewModel(val context: Context, val bluetoothAdapter: BluetoothAdapter,
                        val handler: Handler) : ViewModel() {

    private val SCAN_PERIOD: Long = 10000
    val scanStatus = MutableLiveData(false)
    var device = MutableLiveData<Any>("Ble Device Not Found")
    var mScanning: Boolean = false
    var bleDevice: BleDevice? = null
    var gatt: BluetoothGatt? = null
    val services: MutableLiveData<MutableList<BluetoothGatt>>
    /**
     * Activity for scanning and displaying available BLE devices.
     */
    var leScanCallback: LeScanCallBack
    init {
        leScanCallback = LeScanCallBack(device)
        services = MutableLiveData(ArrayList())
    }

    fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    scanStatus.value = false
                    bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
                    if (device.value !is ScanResult){
                        device.value = "Our Device not found"
                    }
                }, SCAN_PERIOD)
                scanStatus.value = true
                mScanning = true
                bluetoothAdapter.bluetoothLeScanner.startScan(leScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
                scanStatus.value = true
            }
        }
    }

    fun connect(){

        println("ye part run ho rha")
        bleDevice = BleDevice((device.value as ScanResult).device)
        val gatcall = gatCallBack(services)
        gatt = (device.value as ScanResult).device.connectGatt(context, false, gatcall)
        println(gatt!!.connect())
        gatt!!.discoverServices()
        println("yaha tak ho gya")

    }

    fun read(){
        gatt!!.discoverServices()
        println("ye to test hai ${services.value.toString()}")
    }




    class LeScanCallBack(val device: MutableLiveData<Any>): ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result!!.device.name == "ESP32"){
                device.value = result
            }
        }
    }


    class gatCallBack(val services: MutableLiveData<MutableList<BluetoothGatt>>):BluetoothGattCallback(){

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("Successfull")
                gatt!!.discoverServices()
            }
        }

        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }
    }



}
