package fr.mesrouxvais.wifimapper.userInterface

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ScrollView
import android.widget.TextView
import java.util.LinkedList

class Terminal private constructor(private val terminalOutput: TextView, private val scrollView: ScrollView) {
    // Utiliser une LinkedList comme buffer circulaire avec une taille maximale
    private val maxLines = 100
    private val messageBuffer = LinkedList<Pair<String, Int>>() // Pair<message, color>

    companion object {
        private var instance: Terminal? = null

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
        if (message.isBlank()) return

        // Ajouter le nouveau message au buffer
        messageBuffer.add(Pair(message, color))

        // Retirer les messages les plus anciens si nécessaire
        while (messageBuffer.size > maxLines) {
            messageBuffer.removeFirst()
        }

        // Reconstruire le texte avec seulement les dernières lignes
        val builder = SpannableStringBuilder()
        messageBuffer.forEach { (msg, clr) ->
            val start = builder.length
            builder.append("$msg\n")
            builder.setSpan(
                ForegroundColorSpan(clr),
                start,
                builder.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Mettre à jour le TextView sur le thread principal
        terminalOutput.post {
            terminalOutput.text = builder
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    fun clearTerminal() {
        messageBuffer.clear()
        terminalOutput.post {
            terminalOutput.text = ""
        }
    }
}