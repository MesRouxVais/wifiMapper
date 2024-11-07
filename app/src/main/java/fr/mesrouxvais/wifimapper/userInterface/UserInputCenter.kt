package fr.mesrouxvais.wifimapper.userInterface

import android.content.Context
import fr.mesrouxvais.wifimapper.R
import java.util.Date
import java.util.Locale
import android.graphics.Color
import fr.mesrouxvais.wifimapper.MainActivity
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
            }
            command.equals("turnOffBl", ignoreCase = true) -> {
            }
            command.equals("getBlDs", ignoreCase = true) -> {
            }
            command.startsWith("conByNameBL", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a device name (connectByName DeviceName)", Color.YELLOW)
                    return
                }
                val deviceName = parts[1].trim()
                Terminal.getInstance().displayOnTerminal("[+]:Trying to connect to device: $deviceName", Color.GREEN)
            }
            command.equals("disBL", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[r]:Trying to disconnect to device", Color.GREEN)

            }
            command.startsWith("sendbl", ignoreCase = true) -> {
                val parts = command.split(" ", limit = 2)
                if (parts.size < 2) {
                    Terminal.getInstance().displayOnTerminal("[!]:Please specify a message name (sendbl message)", Color.YELLOW)
                    return
                }
                val message = parts[1].trim()
                Terminal.getInstance().displayOnTerminal("[+]:Trying to send message to device: $message", Color.GREEN)

            }
            command.equals("startBlListener", ignoreCase = true) -> {
                Terminal.getInstance().displayOnTerminal("[+]:Trying to send message to start a bluetooth listener", Color.GREEN)

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