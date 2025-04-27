package br.com.fiap.quodapp.screens.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun getCurrentUtcDateTimeIso(): String {
    return DateTimeFormatter.ISO_INSTANT
        .withZone(ZoneOffset.UTC)
        .format(Instant.now())
}