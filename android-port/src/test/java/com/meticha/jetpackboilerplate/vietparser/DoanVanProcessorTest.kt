package com.meticha.jetpackboilerplate.vietparser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoanVanProcessorTest {
    private val sampleLines = sequenceOf(
        """{"linguistics":{"am_dau":"b","doc_am_dau":"bờ","van":"an","am_dem":"","am_chinh":"a","am_cuoi":"n","thanh":"nặng","am_tiet_khong_dau":"ban","ngoai_le":false},"pedagogy":{"danh_van":"bờ - an - ban - nặng - bạn","do_kho":"trung_binh","nhom_bai_hoc":["am_dau_don","van_co_am_cuoi"]},"metadata":{"id":"1","word":"bạn","version":"1.0.0","source":"sgk"}}""",
        """{"linguistics":{"am_dau":"qu","doc_am_dau":"quờ","van":"a","am_dem":"","am_chinh":"a","am_cuoi":"","thanh":"hỏi","am_tiet_khong_dau":"qua","ngoai_le":false},"pedagogy":{"danh_van":"quờ - a - qua - hỏi - quả","do_kho":"kho","nhom_bai_hoc":["dac_biet_gi_qu"]},"metadata":{"id":"2","word":"quả","version":"1.0.0","source":"sgk"}}""",
    )

    @Before
    fun setUp() {
        WordCache.resetForTest()
        WordCache.loadFromJsonLines(sampleLines)
    }

    @Test
    fun tachAmTietBoDauCau() {
        val words = DoanVanProcessor.tachAmTiet("Bạn, quả! Oanh?")
        assertEquals(listOf("bạn", "quả", "oanh"), words)
    }

    @Test
    fun tachAmTietBoSo() {
        val words = DoanVanProcessor.tachAmTiet("Bài 12: bạn có 2 quả.")
        assertEquals(listOf("bài", "bạn", "có", "quả"), words)
    }

    @Test
    fun xuLyDoanVanCoTuNgoaiCache() {
        val items = DoanVanProcessor.xuLyDoanVan("Bạn và nghiêng, quả.")
        assertEquals(4, items.size)
        assertEquals(0, items[0].first)
        assertTrue(items[0].second.cache_hit)
        assertEquals("bạn", items[0].second.tu)
        assertEquals(2, items[2].first)
        assertFalse(items[2].second.cache_hit)
        assertEquals("nghiêng", items[2].second.tu)
        assertEquals("quả", items[3].second.tu)
    }
}
