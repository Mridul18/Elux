package com.assignment.models

import kotlinx.serialization.Serializable

@Serializable
enum class Country(val VAT: Double) {
    SWEDEN(25.0),
    GERMANY(19.0),
    FRANCE(20.0),
    ;

    companion object {
        fun fromStringOrThrow(countryString: String?): Country {
            if (countryString.isNullOrBlank()) {
                throw IllegalArgumentException("Country string cannot be null or blank")
            }
            return try {
                valueOf(countryString.uppercase())
            } catch (e: IllegalArgumentException) {
                val supportedCountries = entries.joinToString { it.name }
                throw IllegalArgumentException("Invalid country: $countryString. Supported countries: $supportedCountries")
            }
        }

        fun getSupportedCountries(): String {
            return entries.joinToString { it.name }
        }
    }
}
