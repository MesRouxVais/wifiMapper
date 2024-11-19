package fr.mesrouxvais.wifimapper

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.S)
object BluetoothCenter {
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1

    private var mainActivity: WeakReference<MainActivity>? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter

    fun initialize(mainActivity: MainActivity) {
        this.mainActivity = WeakReference(mainActivity)
        Terminal.getInstance().displayOnTerminal("[i]:Bluetooth Center is initialized.", Color.WHITE)
    }

    fun enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    mainActivity?.get() ?: return,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Terminal.getInstance().displayOnTerminal("[!]: permissions denied", Color.RED)
                return
            }
            Terminal.getInstance().displayOnTerminal("[i]: enabling bl", Color.WHITE)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (mainActivity?.get() ?: return).startActivityForResult(enableBtIntent, 2)
        }
    }

    //-------------------------------------------------------------Permissions
    fun checkAndRequestBluetoothPermissions(doRequest: Boolean): Boolean {

        val safeMainActivity = (mainActivity?.get() ?: return false)

        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(safeMainActivity, permission) != PackageManager.PERMISSION_GRANTED
        }


        if (missingPermissions.isNotEmpty()) {
            Terminal.getInstance().displayOnTerminal("[!]: permissions denied", Color.RED)
            if(doRequest) {
                Terminal.getInstance().displayOnTerminal("[w]: attempt to send a request", Color.YELLOW)
                ActivityCompat.requestPermissions(
                    safeMainActivity,
                    missingPermissions.toTypedArray(),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            }
            return false


        } else {
            Terminal.getInstance().displayOnTerminal("[-]: get all permissions", Color.WHITE)
            return true
        }

    }

    fun onBluetoothRequestPermissionsResult(grantResults: IntArray){
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Terminal.getInstance().displayOnTerminal("[!]: permissions granted", Color.RED)
        } else {
            // Les permissions sont refusées
            Terminal.getInstance().displayOnTerminal("[!]: permissions denied", Color.RED)

        }
    }


    //-------------------------------------------------------------SCAN

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 5000

    fun scanLeDevice(): List<BluetoothDevice>  {
        if(!checkAndRequestBluetoothPermissions(false) || !::bluetoothAdapter.isInitialized){
            Terminal.getInstance().displayOnTerminal("[!]: permissions denied or bluetooth adapter not initialized", Color.RED)
            return emptyList()
        }
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                Terminal.getInstance().displayOnTerminal("[w]: scan finish", Color.YELLOW)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }

        return LeDeviceListAdapter.getBluetoothDevices()
    }

    // Device scan callback.

    private val leScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            LeDeviceListAdapter.addDevice(result.device)
        }
    }


    //-------------------------------------------------------------CONNECT

    private var bluetoothGatt: BluetoothGatt? = null


    fun connect(name: String): Boolean {


        try {
            val device = LeDeviceListAdapter.findDeviceByName(name) ?: throw IllegalArgumentException("Device not found")

            // Timeout handler
            handler.postDelayed({
                if (connectionState == BluetoothProfile.STATE_CONNECTED) {

                    val success = bluetoothGatt?.discoverServices()
                    Terminal.getInstance().displayOnTerminal("[w]: bluetoothGatt?.discoverServices() : $success", Color.YELLOW)
                    if(success == true){
                        val count = bluetoothGattCallback.getSupportedGattServices()?.size
                        Terminal.getInstance().displayOnTerminal("[w]: services count : $count", Color.YELLOW)
                        if(bluetoothGattCallback.getSupportedGattServices() !=null){
                            for(services in bluetoothGattCallback.getSupportedGattServices()!!){
                                if (services != null) {
                                    Terminal.getInstance().displayOnTerminal("services " + getShortUuid(services.uuid) +" of type " + services.type,  Color.WHITE)
                                    for(characteristic in services.characteristics){
                                        Terminal.getInstance().displayOnTerminal("\t characteristics " + getShortUuid(characteristic.uuid)+" of type " + getCharacteristicType(characteristic.properties),  Color.WHITE)
                                        characteristicsMap.put(getShortUuid(characteristic.uuid), characteristic)


                                    }
                                }
                            }
                        }



                    }
                }else{
                    Terminal.getInstance().displayOnTerminal("[!]: Connection timeout", Color.RED)
                }
            }, 5000) // 5 secondes timeout

            bluetoothGatt = device.connectGatt(
                mainActivity?.get() ?: return false,
                false,  // autoConnect à false pour l'ESP32
                bluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE  // Forcer le mode BLE
            )
            return true
        } catch (e: Exception) {
            Terminal.getInstance().displayOnTerminal("[!]: Connection error: ${e.message}", Color.RED)
            return false
        }

    }

    fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }
    private var connectionState = BluetoothProfile.STATE_DISCONNECTED

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("info bl","[-]: Successfully connected to the GATT Server");
                connectionState = BluetoothProfile.STATE_CONNECTED

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Terminal.getInstance().displayOnTerminal("[-]: disconnected from the GATT Server", Color.WHITE)
                connectionState = BluetoothProfile.STATE_DISCONNECTED
            }
        }

        private var services: List<BluetoothGattService> = emptyList()

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.e("bl", "onServicesDiscovered");
            super.onServicesDiscovered(gatt, status)
            if (gatt != null) {
                services = gatt.services
            }
        }

        fun getSupportedGattServices(): List<BluetoothGattService?>? {
            return services
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Terminal.getInstance().displayOnTerminal(" receved ${value.toString(Charset.defaultCharset())} ", Color.WHITE)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Terminal.getInstance().displayOnTerminal("finish writing ", Color.WHITE)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Terminal.getInstance().displayOnTerminal("new notification " + String(characteristic.value, Charsets.UTF_8), Color.WHITE)
        }

    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startReceivingUpdates() {
        val characteristic = characteristicsMap["0xfef4"]
        if (characteristic != null) {
            bluetoothGatt?.setCharacteristicNotification(characteristic, true)

            val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val desc = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            bluetoothGatt?.writeDescriptor(desc)
        }
    }

    val characteristicsMap = HashMap<String, BluetoothGattCharacteristic>()
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readCharacteristic(shortUUID: String) {

        val characteristic = characteristicsMap[shortUUID]

        if (characteristic != null) {
            val success = bluetoothGatt?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success")
        }
    }

    fun writeCharacteristic(shortUUID: String) {
        val characteristic = characteristicsMap[shortUUID]
        if (characteristic != null) {
            Terminal.getInstance().displayOnTerminal("write on ${characteristic.uuid} ", Color.WHITE)
        }
        if (characteristic != null) {
            // First write the new value to our local copy of the characteristic
            characteristic.value = "Tom".toByteArray()

            //...Then send the updated characteristic to the device
            val success = bluetoothGatt?.writeCharacteristic(characteristic)

            Log.v("bluetooth", "Write status: $success")
        }
    }

    fun startReceivingCharacteristicUpdates(shortUUID: String) {

        val characteristic = characteristicsMap[shortUUID]
        if (characteristic != null) {
            bluetoothGatt?.setCharacteristicNotification(characteristic, true)

            val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val desc = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            bluetoothGatt?.writeDescriptor(desc)
        }
    }




    fun getStatus(){
        var statuts = connectionState == BluetoothProfile.STATE_CONNECTED
        Terminal.getInstance().displayOnTerminal("[-]: connected ? $statuts", Color.WHITE)
    }


//util

    private fun getShortUuid(uuid: UUID): String {
        // Extraire les 16 premiers bits de l'UUID (les 4 premiers caractères hexadécimaux)
        val shortUuidHex = uuid.toString().substring(4, 8)  // Premier segment de 4 caractères hexadécimaux

        // Convertir en entier et afficher sous forme hexadécimale
        return "0x$shortUuidHex"
    }

    private fun getCharacteristicType(type: Int): String{
        if (type == BluetoothGattCharacteristic.PROPERTY_READ) {
            return "READ"
        }
        if (type == BluetoothGattCharacteristic.PROPERTY_WRITE) {
            return "WRITE"
        }
        if (type == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
            return "NOTIFY"
        }

        return "UNKNOWN"
    }

}