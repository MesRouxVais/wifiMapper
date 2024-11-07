package fr.mesrouxvais.wifimapper

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import fr.mesrouxvais.wifimapper.userInterface.UserInputCenter
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var commandInput: EditText
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        commandInput = findViewById(R.id.commandInput)


        Terminal.initialize(findViewById(R.id.terminalOutput), findViewById(R.id.scrollView))
        Terminal.getInstance().displayOnTerminal("[-]:Welcome to the terminal", Color.WHITE)

        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        Terminal.getInstance().displayOnTerminal("[-]:android version :$sdkVersion ($release)", Color.WHITE)


        UserInputCenter.initialize(this)
        PermissionCenter.initialize(this,this)



        val executeButton: Button = findViewById(R.id.executeButton)
        executeButton.setOnClickListener {
            val command = commandInput.text.toString()
            if (command.isNotBlank()) {
                Terminal.getInstance().displayOnTerminal("[+]:$command", Color.CYAN)
                UserInputCenter.getInstance().processCommand(command)
                commandInput.text.clear() // Efface le champ de saisie
            }
        }
        checkAndRequestBluetoothPermissions()
        enableBluetooth()

        scanLeDevice()
    }


    fun checkAndRequestBluetoothPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST_CODE)
        } else {
            Terminal.getInstance().displayOnTerminal("[r]: get all permissions", Color.GREEN)
        }
    }

    // Fonction de rappel pour traiter le résultat de la demande de permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Toutes les permissions sont accordées
                enableBluetooth()
            } else {
                // Les permissions sont refusées
                Terminal.getInstance().displayOnTerminal("[!]: permissions denied", Color.RED)

            }
        }
    }

    fun enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Terminal.getInstance().displayOnTerminal("[!]: permissions denied", Color.RED)
                return
            }
            Terminal.getInstance().displayOnTerminal("[i]: enabling bl", Color.WHITE)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 2)
        }
    }


    //test ble


    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private fun scanLeDevice() {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    // Device scan callback.
    private val devices = mutableListOf<BluetoothDevice>()

    private val leScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Terminal.getInstance().displayOnTerminal("[r]:scanning", Color.GREEN)
            if (!devices.contains(result.device)) {
                devices.add(result.device)
                Terminal.getInstance().displayOnTerminal("[r]: device added :" + result.device.name, Color.GREEN)
            }
        }
    }

}
