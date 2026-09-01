package com.example.personaldocumentproject.data.local.converters

object DocumentConverters {
    private const val SEPARATOR = "\u001F"

    fun fromStringList(values: List<String>): String = values.joinToString(SEPARATOR)
    fun toStringList(value: String): List<String> =
        value.takeIf(String::isNotEmpty)?.split(SEPARATOR).orEmpty()

    fun fromFloatList(values: List<Float>): String = values.joinToString(",")
    fun toFloatList(value: String): List<Float> =
        value.takeIf(String::isNotEmpty)?.split(',')?.mapNotNull(String::toFloatOrNull).orEmpty()
}
