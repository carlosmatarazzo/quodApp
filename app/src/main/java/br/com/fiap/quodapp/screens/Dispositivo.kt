package br.com.fiap.quodapp.screens

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dispositivo(
    val fabricante: String,
    val modelo: String,
    val sistemaOperacional: String,
    val dataDispositivo: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("ipOrigem") val ipOrigem: String
)
