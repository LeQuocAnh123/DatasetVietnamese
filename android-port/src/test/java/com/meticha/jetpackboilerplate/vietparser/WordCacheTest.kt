package com.meticha.jetpackboilerplate.vietparser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordCacheTest {
    private val sampleLines = sequenceOf(
        """{"linguistics":{"am_dau":"b","doc_am_dau":"bờ","van":"an","am_dem":"","am_chinh":"a","am_cuoi":"n","thanh":"nặng","am_tiet_khong_dau":"ban","ngoai_le":false},"pedagogy":{"danh_van":"bờ - an - ban - nặng - bạn","do_kho":"trung_binh","nhom_bai_hoc":["am_dau_don","van_co_am_cuoi"]},"metadata":{"id":"1","word":"bạn","version":"1.0.0","source":"sgk"}}""",
        """{"linguistics":{"am_dau":"qu","doc_am_dau":"quờ","van":"a","am_dem":"","am_chinh":"a","am_cuoi":"","thanh":"hỏi","am_tiet_khong_dau":"qua","ngoai_le":false},"pedagogy":{"danh_van":"quờ - a - qua - hỏi - quả","do_kho":"kho","nhom_bai_hoc":["dac_biet_gi_qu"]},"metadata":{"id":"2","word":"quả","version":"1.0.0","source":"sgk"}}""",
    )

    @Before
    fun setUp() {
        WordCache.resetForTest()
    }

    @Test
    fun loadFileIntoCache() {
        WordCache.loadFromJsonLines(sampleLines)
        assertEquals(2, WordCache.sizeForTest())
    }

    @Test
    fun cacheHitWhenWordExists() {
        WordCache.loadFromJsonLines(sampleLines)
        val record = WordCache.layRecord("bạn")
        assertTrue(record.cache_hit)
        assertEquals("bờ - an - ban - nặng - bạn", record.danh_van)
    }

    @Test
    fun fallbackParserWhenWordMissing() {
        WordCache.loadFromJsonLines(sampleLines)
        val record = WordCache.layRecord("nghiêng")
        assertFalse(record.cache_hit)
        assertEquals("ngờ - iêng - nghieng - ngang - nghiêng", record.danh_van)
    }
}
