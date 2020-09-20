package com.niltechcorp.bluetoothble

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.niltechcorp.bluetoothble.databinding.ActivityMain2Binding
import com.niltechcorp.bluetoothble.home.HomePageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


private var operatingAnim: Animation? = null
private var progressDialog: ProgressDialog? = null
class Main2Activity : AppCompatActivity() {
    private lateinit var binding:ActivityMain2Binding
    val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val handler =  Handler()
    private val SCAN_PERIOD: Long = 10000
    val scanStatus = MutableLiveData(false)
    var device = MutableLiveData<Any>("Ble Device Not Found")
    var mScanning: Boolean = false
    var gatt: BluetoothGatt? = null
    var bleDevice: BleDevice? = null
    val job = Job()
    lateinit var uiScope:CoroutineScope
    lateinit var leScanCallback: HomePageViewModel.LeScanCallBack
    lateinit var services: MutableLiveData<MutableList<BluetoothGatt>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        leScanCallback = HomePageViewModel.LeScanCallBack(device)
        services = MutableLiveData(ArrayList())
        scanLeDevice(true)
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        operatingAnim?.interpolator = LinearInterpolator()
        progressDialog = ProgressDialog(this)

        uiScope = CoroutineScope(Dispatchers.Main+job)
        device.observe(this, Observer {
            it?.let {
                if (it is ScanResult){
                    if (it.device.name == "ESP32"){
                        if (binding.deviceData.text.toString() != "Our Device Located"){
                            binding.deviceData.text = "Our Device Located\n"
                            try {
                                progressDialog?.show()
                                connect()
                            }
                            catch (e:Exception){
                                binding.deviceData.text = "connection failed"
                            }
                            scanLeDevice(false)
                        }
                    }
                }
            }
        })
        binding.read.setOnClickListener {
            try {
                read()
            }

            catch(e: Exception){
                try {
                    count = 0
                    connect()
                }
                catch (e: Exception){
                    binding.deviceData.text = "Device not able to connect"
                }}

        }


    }
    fun autoRefresh(){
            val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
            println("Auto Refresh Started")

            scheduler.scheduleAtFixedRate(Runnable {
                println("Auto Refresh Running")
                try {
                    count = 0
                    connect()
                }
                catch (e: Exception){
                    binding.deviceData.text = "Device not able to connect"
                }
            }, 5, 10, TimeUnit.SECONDS)
    }
    @android.support.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
    var count = 0

    fun connect(){

        println("ye part run ho rha")
        count ++
        if (count <2){
            if (bleDevice==null) {
                bleDevice = BleDevice(((device.value) as ScanResult).device)
            }
            binding.deviceData.text = ""
            BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
                override fun onStartConnect() {
                    println("Starting to connect")
                }


                override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                    progressDialog?.dismiss()
                    device.value = bleDevice
                    read()
                }

                override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                    progressDialog?.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "connection failed",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.deviceData.text = "Connection Failed"
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    bleDevice: BleDevice,
                    gatt: BluetoothGatt,
                    status: Int
                ) {
                    binding.deviceData.text = "Device Disconnected, trying to reconnect"
                    count = 0
                    connect()
                }
                })
        }
    }

    fun read(){
//        binding.deviceData.text = ""
        var bleDevice: BleDevice? = null
        if (device.value is ScanResult) {
            bleDevice = BleDevice((device.value as ScanResult).device)
        }
        if (device.value is BleDevice){
            bleDevice = device.value as BleDevice
        }
        gatt = BleManager.getInstance().getBluetoothGatt(bleDevice!!)
        println("ye to test hai ${gatt?.services}")
        val ourService = gatt?.services?.last()
        BleManager.getInstance().read(
            bleDevice,
            ourService!!.uuid.toString(),
            ourService.characteristics.last().uuid.toString(),
            object : BleReadCallback() {

                override fun onReadSuccess(data: ByteArray) {
                    val charset = Charsets.UTF_8
                    runOnUiThread(Runnable {
                        println("Prerunning")
                        val text_data = binding.deviceData.text.toString()
                        var data2 = data.toString(charset)
                        if (text_data.startsWith(data2)){
                            binding.deviceData.text = ""
                        }

                        if ("#" in data2) {
                            var printData = text_data + data2
                                .replace("#", "---------------------", false)
                            binding.deviceData.text = printData
                            println(printData)
                        }
                        else if("--" in data2){
                            var printData = text_data+data2
                                .replace("--", "by", false)
                            binding.deviceData.text = printData
                            println(printData)
                        }
                        else{
                            binding.deviceData.text = text_data+data2
                        }
                    })
                }

                override fun onReadFailure(exception: BleException) {
                    runOnUiThread(Runnable { println("Failure")})
                }
            })
    }


}
