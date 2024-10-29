package fr.mesrouxvais.wifimapper

import android.bluetooth.BluetoothSocket
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

object BluetoothCommunication {

    private const val BUFFER_SIZE = 1024
    private val uiHandler = Handler(Looper.getMainLooper())

    /**
     * Envoie des données sur le socket Bluetooth actif
     * @param data Les données à envoyer sous forme de ByteArray
     * @return true si l'envoi a réussi, false sinon
     */
    fun sendData(data: ByteArray): Boolean {
        val socket = BluetoothCenter.getInstance().bluetoothSocket ?: return false

        return try {
            val outputStream: OutputStream = socket.outputStream
            outputStream.write(data)
            outputStream.flush()
            true
        } catch (e: IOException) {
            uiHandler.post {
                Terminal.getInstance().displayOnTerminal("[-]:Error sending data: ${e.message}", Color.RED)
            }
            false
        }
    }

    /**
     * Reçoit des données du socket Bluetooth actif
     * @return Les données reçues sous forme de ByteArray, ou null en cas d'erreur
     */
    fun receiveData(): ByteArray? {
        val socket = BluetoothCenter.getInstance().bluetoothSocket ?: return null

        return try {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(BUFFER_SIZE)
            val bytesRead = inputStream.read(buffer)
            buffer.copyOfRange(0, bytesRead)
        } catch (e: IOException) {
            uiHandler.post {//important pour android de gérer l'ui dans le tread principal
                Terminal.getInstance().displayOnTerminal("[-]:Error receiving data: ${e.message}", Color.RED)
            }
            null
        }
    }

    /**
     * Lit les données entrantes dans un thread séparé et les affiche dans le terminal
     */
    fun startDataListener() {
        thread {
            while (true) {
                val data = receiveData() ?: continue
                val message = String(data, StandardCharsets.UTF_8)
                uiHandler.post {
                    Terminal.getInstance().displayOnTerminal("[<]:$message", Color.CYAN)
                }
            }
        }
    }
}