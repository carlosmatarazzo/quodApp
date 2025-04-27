package br.com.fiap.quodapp.screens

import kotlinx.serialization.Serializable

@Serializable
data class BiometriaResponse(
    val status: String,
    val tipoBiometria: String? = null,
    val id: String? = null,
    val dataCaptura: String? = null,
    val dispositivo: Dispositivo? = null,
    val imagemBase64: String? = null
)