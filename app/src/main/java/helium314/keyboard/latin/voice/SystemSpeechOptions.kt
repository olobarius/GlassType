// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.voice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.LocaleList
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import helium314.keyboard.latin.RichInputMethodManager
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.getSecondaryLocales
import helium314.keyboard.latin.utils.prefs
import java.util.Locale

/** Options for the built-in voice input that uses the Android SpeechRecognizer. */
object SystemSpeechOptions {
    private const val TAG = "SystemSpeechOptions"

    /** recognition service: [SERVICE_DEFAULT] or a flattened ComponentName */
    const val PREF_VOICE_SYSTEM_SERVICE = "voice_system_service"
    const val SERVICE_DEFAULT = "default"

    /** installed recognition services as label to flattened component name */
    fun installedServices(context: Context): List<Pair<String, String>> {
        val pm = context.packageManager
        return pm.queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), 0)
            .mapNotNull { info ->
                val service = info.serviceInfo ?: return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: service.packageName
                label to ComponentName(service.packageName, service.name).flattenToString()
            }
            .sortedBy { it.first.lowercase() }
    }

    /** label of the system default recognition service, null if unknown */
    fun defaultServiceLabel(context: Context): String? {
        val flat = android.provider.Settings.Secure.getString(context.contentResolver, "voice_recognition_service")
            ?: return null
        return installedServices(context).firstOrNull { it.second == flat }?.first
    }

    /** recognizer of the chosen service, falls back to the system default if it is no longer installed */
    fun createRecognizer(context: Context): SpeechRecognizer {
        val chosen = context.prefs().getString(PREF_VOICE_SYSTEM_SERVICE, SERVICE_DEFAULT) ?: SERVICE_DEFAULT
        if (chosen != SERVICE_DEFAULT) {
            val component = ComponentName.unflattenFromString(chosen)
            if (component != null && installedServices(context).any { it.second == chosen }) {
                try {
                    return SpeechRecognizer.createSpeechRecognizer(context, component)
                } catch (e: Exception) {
                    Log.w(TAG, "could not use recognition service $chosen: ${e.message}")
                }
            }
        }
        return SpeechRecognizer.createSpeechRecognizer(context)
    }

    /**
     * Recognizers work best with language and region ("de-DE" instead of "de"). The region is taken from
     * the system languages if one matches, otherwise from a short list of common defaults.
     */
    fun withRegion(languageTag: String): String {
        val locale = Locale.forLanguageTag(languageTag)
        if (locale.country.isNotEmpty() || locale.language.isEmpty()) return languageTag
        val system = LocaleList.getDefault()
        for (i in 0 until system.size()) {
            val l = system[i]
            if (l.language == locale.language && l.country.isNotEmpty()) return "${l.language}-${l.country}"
        }
        val region = DEFAULT_REGIONS[locale.language] ?: return languageTag
        return "${locale.language}-$region"
    }

    /** all languages of the current keyboard (main and additional languages), with region */
    fun keyboardLanguages(): List<String> = try {
        val subtype = RichInputMethodManager.getInstance().currentSubtype
        val main = subtype.locale
        val secondary = getSecondaryLocales(subtype.rawSubtype.extraValue)
        (listOf(main) + secondary).map { withRegion(it.toLanguageTag()) }.distinct()
    } catch (e: Exception) {
        emptyList()
    }

    private val SPOKEN_PUNCTUATION = mapOf(
        "punkt" to ".", "komma" to ",", "fragezeichen" to "?", "ausrufezeichen" to "!", "doppelpunkt" to ":",
        "period" to ".", "comma" to ",", "question mark" to "?", "exclamation mark" to "!", "colon" to ":",
    )

    /**
     * After a pause the recognizer starts a new segment, and a spoken punctuation mark at its start
     * ("Punkt", "Komma") is written as a word. Turns it into the punctuation mark.
     */
    fun leadingSpokenPunctuation(text: String): String {
        val lower = text.lowercase()
        for ((word, mark) in SPOKEN_PUNCTUATION) {
            if (lower == word) return mark
            if (lower.startsWith("$word ")) return mark + text.substring(word.length)
        }
        return text
    }

    private val DEFAULT_REGIONS = mapOf(
        "de" to "DE", "en" to "US", "fr" to "FR", "es" to "ES", "it" to "IT", "nl" to "NL", "pt" to "BR",
        "pl" to "PL", "sv" to "SE", "da" to "DK", "nb" to "NO", "fi" to "FI", "cs" to "CZ", "tr" to "TR",
        "ru" to "RU", "uk" to "UA", "ja" to "JP", "ko" to "KR", "zh" to "CN",
    )
}
