package fr.mesrouxvais.wifimapper // Remplacez par le nom de votre package

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

import androidx.appcompat.app.AppCompatActivity
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import fr.mesrouxvais.wifimapper.userInterface.UserInputCenter

class MainActivity : AppCompatActivity() {
    private lateinit var commandInput: EditText

    private lateinit var turnON: Button
    private lateinit var turnOff: Button
    private lateinit var getDevicesList: Button
    private var REQUEST_ENABLE_BLUETOOTH = 2
    private lateinit var bDevices: Set<BluetoothDevice>
    private lateinit var adapter: BluetoothAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        commandInput = findViewById(R.id.commandInput)


        Terminal.initialize(findViewById(R.id.terminalOutput), findViewById(R.id.scrollView))
        Terminal.getInstance().displayOnTerminal("[-]:Welcome to the terminal", Color.WHITE)


        UserInputCenter.initialize(this)
        PermissionCenter.initialize(this,this)
        BluetoothCenter.initialize(this)



        val executeButton: Button = findViewById(R.id.executeButton)
        executeButton.setOnClickListener {
            val command = commandInput.text.toString()
            if (command.isNotBlank()) {
                Terminal.getInstance().displayOnTerminal("[+]:$command", Color.CYAN)
                UserInputCenter.getInstance().processCommand(command)
                commandInput.text.clear() // Efface le champ de saisie
            }
        }

    }

}
