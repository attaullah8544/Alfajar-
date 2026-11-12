package com.example.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab

object LocationDefaultsHelper {

    /**
     * Recommends the most accurate standard calculation method for the given country code or name.
     */
    fun recommendCalculationMethod(countryCode: String?, countryName: String?): CalculationMethod {
        val code = countryCode?.lowercase()?.trim() ?: ""
        val name = countryName?.lowercase()?.trim() ?: ""

        return when {
            // Pakistan, Bangladesh, India, Afghanistan
            code in listOf("pk", "bd", "in", "af") ||
                    name.contains("pakistan") || name.contains("bangladesh") ||
                    name.contains("india") || name.contains("afghanistan") -> {
                CalculationMethod.KARACHI
            }

            // Saudi Arabia, Yemen, Oman, Bahrain
            code in listOf("sa", "ye", "om", "bh") ||
                    name.contains("saudi") || name.contains("yemen") ||
                    name.contains("oman") || name.contains("bahrain") -> {
                CalculationMethod.UMM_AL_QURA
            }

            // United Arab Emirates
            code == "ae" || name.contains("emirates") || name.contains("uae") || name.contains("dubai") -> {
                CalculationMethod.DUBAI
            }

            // Qatar
            code == "qa" || name.contains("qatar") -> {
                CalculationMethod.QATAR
            }

            // Kuwait
            code == "kw" || name.contains("kuwait") -> {
                CalculationMethod.KUWAIT
            }

            // Egypt, Syria, Lebanon, Jordan, Palestine, Sudan, Libya, Iraq
            code in listOf("eg", "sy", "lb", "jo", "ps", "sd", "ly", "iq") ||
                    name.contains("egypt") || name.contains("syria") ||
                    name.contains("lebanon") || name.contains("jordan") ||
                    name.contains("palestine") || name.contains("sudan") ||
                    name.contains("libya") || name.contains("iraq") -> {
                CalculationMethod.EGYPTIAN
            }

            // North America (USA, Canada, Mexico)
            code in listOf("us", "ca", "mx") ||
                    name.contains("united states") || name.contains("usa") ||
                    name.contains("canada") || name.contains("mexico") -> {
                CalculationMethod.NORTH_AMERICA
            }

            // Singapore, Malaysia, Indonesia, Brunei, Thailand, Philippines
            code in listOf("sg", "my", "id", "bn", "th", "ph") ||
                    name.contains("singapore") || name.contains("malaysia") ||
                    name.contains("indonesia") || name.contains("brunei") -> {
                CalculationMethod.SINGAPORE
            }

            // Europe, Turkey, Central Asia, Russia, and rest of the world
            else -> {
                CalculationMethod.MUSLIM_WORLD_LEAGUE
            }
        }
    }

    /**
     * Determines whether the region traditionally follows the Hanafi or Shafi'i/Standard Asr time.
     * Hanafi is prominent in South Asia (Pakistan, Bangladesh, India, Afghanistan) and parts of Central Asia/Turkey.
     */
    fun recommendMadhab(countryCode: String?, countryName: String?): Madhab {
        val code = countryCode?.lowercase()?.trim() ?: ""
        val name = countryName?.lowercase()?.trim() ?: ""

        return if (code in listOf("pk", "bd", "in", "af", "tr") ||
            name.contains("pakistan") || name.contains("bangladesh") ||
            name.contains("india") || name.contains("afghanistan") ||
            name.contains("turkey")
        ) {
            Madhab.HANAFI
        } else {
            Madhab.SHAFI
        }
    }
}
