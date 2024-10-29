package fr.mesrouxvais.wifimapper.userInterface

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ScrollView
import android.widget.TextView

class Terminal private constructor(private val terminalOutput: TextView, private val scrollView: ScrollView){
    private val outputBuilder = SpannableStringBuilder()

    companion object {
        private var instance: Terminal? = null

        // Méthode pour créer l'instance du singleton
        fun initialize(terminalOutput: TextView, scrollView: ScrollView) {
            if (instance == null) {
                instance = Terminal(terminalOutput, scrollView)
            } else {
                throw IllegalStateException("Terminal has already been initialized.")
            }
        }

        fun getInstance(): Terminal {
            return instance ?: throw IllegalStateException("Terminal not initialized.")
        }
    }

    fun displayOnTerminal(message: String, color: Int) {
        if (message.isNotBlank()) {
            // Créer un SpannableStringBuilder pour accumuler les messages
            val spannableMessage = SpannableStringBuilder("$message\n").apply {
                setSpan(ForegroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // Ajouter le message formaté au TextView
            outputBuilder.append(spannableMessage)
            terminalOutput.text = outputBuilder // Mettre à jour le texte

            // Défilement vers le bas
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN) // Défilement vers le bas
            }
        }
    }

    fun clearTerminal(){
        outputBuilder.clear()
    }
}