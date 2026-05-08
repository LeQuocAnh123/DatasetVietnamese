package com.meticha.jetpackboilerplate.vietparser

import java.text.Normalizer
import java.util.Locale

object DoanVanProcessor {
    private val localeVi = Locale("vi", "VN")
    private val wordRegex = Regex("\\p{L}+")

    fun tachAmTiet(doanVan: String): List<String> {
        val lower = Normalizer.normalize(doanVan.lowercase(localeVi), Normalizer.Form.NFC)
        return wordRegex.findAll(lower).map { it.value }.toList()
    }

    fun xuLyDoanVan(doanVan: String): List<Pair<Int, AmTiet>> {
        val lower = Normalizer.normalize(doanVan.lowercase(localeVi), Normalizer.Form.NFC)
        return wordRegex.findAll(lower).mapIndexed { index, matchResult ->
            index to WordCache.layRecord(matchResult.value)
        }.toList()
    }
}
