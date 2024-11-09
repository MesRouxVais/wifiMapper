package fr.mesrouxvais.wifimapper.userInterface

import android.content.Context
import fr.mesrouxvais.wifimapper.R
import java.util.Date
import java.util.Locale
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import fr.mesrouxvais.wifimapper.BluetoothCenter
import java.text.SimpleDateFormat
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.S)
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
            command.equals("help", ignoreCase = true) -> {
                val allCommands = context.getString(R.string.all_commands)
                Terminal.getInstance().displayOnTerminal("[r]: $allCommands", Color.CYAN)
            }
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


            command.equals("askBlPerms", ignoreCase = true) -> {
                BluetoothCenter.checkAndRequestBluetoothPermissions(true)
            }
            command.equals("turnOnBl", ignoreCase = true) -> {
                BluetoothCenter.enableBluetooth()
            }
            command.equals("turnOffBl", ignoreCase = true) -> {
            }
            command.equals("getBlDs", ignoreCase = true) -> {
                for (device in BluetoothCenter.scanLeDevice()){
                    val name = device.name
                    Terminal.getInstance().displayOnTerminal("[i]:\t $name", Color.CYAN)
                }
            }


            command.startsWith("conByNameBL", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a device name ", Color.YELLOW)
                    return
                }
                val deviceName = parts[1].trim()
                Terminal.getInstance().displayOnTerminal("[+]:Trying to connect to device: $deviceName", Color.GREEN)
                BluetoothCenter.connect(deviceName)
            }
            command.equals("disBL", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying to disconnect to device", Color.GREEN)
                BluetoothCenter.close()

            }
            command.startsWith("reading", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a UUID (reading UUID)", Color.YELLOW)
                    return
                }
                Terminal.getInstance().displayOnTerminal("[+]:Trying read", Color.GREEN)

                BluetoothCenter.readCharacteristic(parts[1].trim())

            }
            command.startsWith("write", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a UUID ", Color.YELLOW)
                    return
                }
                Terminal.getInstance().displayOnTerminal("[+]:Trying write", Color.GREEN)

                BluetoothCenter.writeCharacteristic(parts[1].trim());

            }
            command.equals("status", ignoreCase = true) -> {
                BluetoothCenter.getStatus()

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