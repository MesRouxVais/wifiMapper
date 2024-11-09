package fr.mesrouxvais.wifimapper

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import fr.mesrouxvais.wifimapper.userInterface.Terminal


object LeDeviceListAdapter {
    private val devices = mutableListOf<BluetoothDevice>()

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            Terminal.getInstance().displayOnTerminal("[r]: device added :" + device.name, Color.GREEN)
        }
    }

    fun clear() {
        devices.clear()
    }

    fun getBluetoothDevices(): List<BluetoothDevice> {
        return devices.toList()
    }

    fun findDeviceByName(deviceName: String): BluetoothDevice? {
        if (deviceName.isBlank()) {
            throw IllegalArgumentException("Device name cannot be empty")
        }

        return devices.find { device ->
            device.name?.equals(deviceName, ignoreCase = true) == true
        }.also { device ->
            if (device != null) {
                Terminal.getInstance().displayOnTerminal("[+]: Found device: ${device.name}", Color.GREEN)
            } else {
                Terminal.getInstance().displayOnTerminal("[!]: No device found with name: $deviceName", Color.YELLOW)
            }
        }
    }


}

