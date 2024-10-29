package fr.mesrouxvais.wifimapper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class BluetoothCenter private constructor(mainActivity:MainActivity){
    private lateinit var getDevicesList: Button
    private var REQUEST_ENABLE_BLUETOOTH = 2
    private lateinit var bDevices: Set<BluetoothDevice>
    private lateinit var adapter: BluetoothAdapter
    private var mainActivity= mainActivity
    private val UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var bluetoothSocket: BluetoothSocket? = null
    private var connectedDeviceName: String? = null
    private val CONNECT_TIMEOUT_MS = 5000L // 5 secondes de timeout

    companion object {
        private var instance: BluetoothCenter? = null

        fun initialize(mainActivity: MainActivity) {
            if (instance == null) {
                instance = BluetoothCenter(mainActivity)
                Terminal.getInstance().displayOnTerminal("[-]:Bluetooth Center is initialized.", Color.WHITE)
                getInstance().adapter = BluetoothAdapter.getDefaultAdapter()

                if(getInstance().adapter==null){
                    Terminal.getInstance().displayOnTerminal("[-]:Bluetooth is not supported", Color.RED)
                }
            } else {
                throw IllegalStateException("Bluetooth Center has already been initialized.")
            }
        }

        fun getInstance(): BluetoothCenter {
            return instance ?: throw IllegalStateException("Bluetooth Center not initialized.")
        }
    }

    fun enableBluetooth(){
        if(!adapter.isEnabled){
            var itent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            mainActivity.startActivityForResult(itent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    fun disableBLuetooth(){
        disconnect()
        adapter.disable()
    }

    fun getBluetoothDevices():Set<BluetoothDevice>{
        if(!adapter.isEnabled){
            Terminal.getInstance().displayOnTerminal("[-]:Bluetooth is not enabled", Color.RED)
            return emptySet()
        }
        bDevices = adapter.bondedDevices
        return bDevices
    }

    fun listPairedDevices() {
        val devices = getBluetoothDevices()
        if (devices.isEmpty()) {
            Terminal.getInstance().displayOnTerminal("[-]:No paired devices found", Color.YELLOW)
            return
        }

        Terminal.getInstance().displayOnTerminal("[+]:Paired devices:", Color.GREEN)
        devices.forEach { device ->
            val status = if (device.name == connectedDeviceName) " (CONNECTED)" else ""
            Terminal.getInstance().displayOnTerminal("   - Name: '${device.name}'${status} Address: ${device.address}", Color.WHITE)
        }
    }

    fun getConnectionStatus(): String {
        return if (bluetoothSocket?.isConnected == true && connectedDeviceName != null) {
            "Connected to: $connectedDeviceName"
        } else {
            "Not connected"
        }
    }

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    private fun connectWithTimeout(socket: BluetoothSocket): Boolean {
        var connected = false
        val connectThread = thread {
            try {
                socket.connect()
                connected = true
            } catch (e: IOException) {
                try {
                    socket.close()
                } catch (ce: IOException) {
                    Terminal.getInstance().displayOnTerminal("[-]:Error closing socket: ${ce.message}", Color.RED)
                }
            }
        }

        try {
            connectThread.join(CONNECT_TIMEOUT_MS)
            if (!connected) {
                connectThread.interrupt()
                try {
                    socket.close()
                } catch (e: IOException) {
                    Terminal.getInstance().displayOnTerminal("[-]:Error closing socket after timeout: ${e.message}", Color.RED)
                }
                Terminal.getInstance().displayOnTerminal("[-]:Connection timeout after ${CONNECT_TIMEOUT_MS/1000} seconds", Color.RED)
            }
        } catch (e: InterruptedException) {
            Terminal.getInstance().displayOnTerminal("[-]:Connection interrupted: ${e.message}", Color.RED)
            connectThread.interrupt()
            return false
        }

        return connected
    }

    fun connectToDeviceByName(deviceName: String): Boolean {
        try {
            if (!adapter.isEnabled) {
                Terminal.getInstance().displayOnTerminal("[-]:Bluetooth is not enabled", Color.RED)
                return false
            }

            Terminal.getInstance().displayOnTerminal("[+]:Searching for device: '$deviceName'", Color.GREEN)
            listPairedDevices()

            val pairedDevices = getBluetoothDevices()
            val device = pairedDevices.find { it.name?.contains(deviceName, ignoreCase = true) == true }

            if (device == null) {
                Terminal.getInstance().displayOnTerminal("[-]:Device containing '$deviceName' not found in paired devices", Color.RED)
                return false
            }

            bluetoothSocket?.close()

            Terminal.getInstance().displayOnTerminal("[+]:Attempting connection to '${device.name}'...", Color.GREEN)
            val socket = device.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT)

            if (connectWithTimeout(socket)) {
                bluetoothSocket = socket
                connectedDeviceName = device.name
                Terminal.getInstance().displayOnTerminal("[+]:Successfully connected to '${device.name}'", Color.GREEN)
                return true
            } else {
                Terminal.getInstance().displayOnTerminal("[-]:Could not connect to '${device.name}'. Device might be turned off or out of range", Color.RED)
                return false
            }

        } catch (e: Exception) {
            Terminal.getInstance().displayOnTerminal("[-]:Connection error: ${e.message}", Color.RED)
            bluetoothSocket?.close()
            bluetoothSocket = null
            connectedDeviceName = null
            return false
        }
    }

    fun disconnect() {
        try {
            if (connectedDeviceName != null) {
                Terminal.getInstance().displayOnTerminal("[+]:Disconnecting from '$connectedDeviceName'", Color.GREEN)
            }
            bluetoothSocket?.close()
            bluetoothSocket = null
            connectedDeviceName = null
            Terminal.getInstance().displayOnTerminal("[+]:Disconnected", Color.GREEN)
        } catch (e: IOException) {
            Terminal.getInstance().displayOnTerminal("[-]:Error during disconnection: ${e.message}", Color.RED)
        }
    }
}