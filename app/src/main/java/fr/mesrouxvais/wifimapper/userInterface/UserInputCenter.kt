package fr.mesrouxvais.wifimapper.userInterface

import android.content.Context
import fr.mesrouxvais.wifimapper.R
import java.util.Date
import java.util.Locale
import android.graphics.Color
import fr.mesrouxvais.wifimapper.BluetoothCenter
import fr.mesrouxvais.wifimapper.BluetoothCommunication
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat


class UserInputCenter private constructor(private val context: Context){

    companion object {
        private var instance: UserInputCenter? = null

        // Méthode pour créer l'instance du singleton
        fun initialize(context: Context) {
            if (instance == null) {
                instance = UserInputCenter(context)
                Terminal.getInstance().displayOnTerminal("[-]:User Input Center is initialized.", Color.WHITE)
            } else {
                throw IllegalStateException("UserInputCenter has already been initialized.")
            }
        }

        fun getInstance(): UserInputCenter {
            return instance ?: throw IllegalStateException("UserInputCenter not initialized.")
        }
    }


    fun processCommand(command: String) {
        when {
            command.equals("clear", ignoreCase = true) -> {
                Terminal.getInstance().clearTerminal()
                Terminal.getInstance().displayOnTerminal("[r]:Terminal cleared.", Color.GREEN)
            }
            command.equals("checkpoint", ignoreCase = true) -> {
                val timestamp = getCurrentTimestamp()
                Terminal.getInstance().displayOnTerminal("[r]:Checkpoint: $timestamp", Color.GREEN)
            }
            command.equals("mesrouxvais", ignoreCase = true) -> {
                val whoIsIt = context.getString(R.string.easter_egg)
                Terminal.getInstance().displayOnTerminal("[r]:$whoIsIt", Color.GREEN)
            }
            command.equals("turnOnBl", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying to turn the bluetooth on", Color.GREEN)
                BluetoothCenter.getInstance().enableBluetooth()
            }
            command.equals("turnOffBl", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying to turn the bluetooth off", Color.GREEN)
                BluetoothCenter.getInstance().disableBLuetooth()
            }
            command.equals("getBlDs", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying get the bluetooth devices list", Color.GREEN)
                for(item in BluetoothCenter.getInstance().getBluetoothDevices()){
                    var itemName = item.name
                    Terminal.getInstance().displayOnTerminal("[r]:---$itemName", Color.GREEN)
                }
            }
            command.startsWith("conByNameBL", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a device name (connectByName DeviceName)", Color.YELLOW)
                    return
                }
                val deviceName = parts[1].trim()
                Terminal.getInstance().displayOnTerminal("[+]:Trying to connect to device: $deviceName", Color.GREEN)

                val success = BluetoothCenter.getInstance().connectToDeviceByName(deviceName)
                if (!success) {
                    Terminal.getInstance().displayOnTerminal("[!]:Connection failed. Please check device name and try again.", Color.RED)
                }
            }
            command.equals("disBL", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying to disconnect to device", Color.GREEN)
                BluetoothCenter.getInstance().disconnect()

            }
            command.startsWith("sendbl", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a message name (sendbl message)", Color.YELLOW)
                    return
                }
                val message = parts[1].trim()
                Terminal.getInstance().displayOnTerminal("[+]:Trying to send message to device: $message", Color.GREEN)
                BluetoothCommunication.sendData(message.toByteArray(StandardCharsets.US_ASCII))

            }
            command.equals("startBlListener", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[+]:Trying to send message to start a bluetooth listener", Color.GREEN)
                BluetoothCommunication.startDataListener()

            }
            else -> {
                Terminal.getInstance().displayOnTerminal("[r*]:invalid entry\"$command\"", Color.RED)
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return formatter.format(Date())
    }
}