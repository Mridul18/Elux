package com.assignment.models

import kotlinx.serialization.Serializable

@Serializable
enum class Country(val VAT: Double) {
    SWEDEN(25.0),
    GERMANY(19.0),
    FRANCE(20.0),
    ;

    companion object {
        fun fromString(countryString: String): Country {
            return try {
                valueOf(countryString.uppercase())
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid country: $countryString. Supported countries: ${getSupportedCountries()}")
            }
        }

        fun getSupportedCountries(): String {
            return entries.joinToString { it.name }
        }
    }
}
