package fr.mesrouxvais.wifimapper

import android.bluetooth.BluetoothDevice




class LeDeviceListAdapter {
    private val devices = mutableListOf<BluetoothDevice>()

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
        }
    }

    fun clear() {
        devices.clear()
    }

    fun getBluetoothDevices(): List<BluetoothDevice> {
        return devices.toList()
    }


}

