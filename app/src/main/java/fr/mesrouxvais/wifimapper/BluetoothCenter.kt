package fr.mesrouxvais.wifimapper

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import fr.mesrouxvais.wifimapper.userInterface.Terminal



