package com.meticha.jetpackboilerplate.vietparser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VietParserTest {
    @Test
    fun hoc() {
        val ketQua = VietParser.phanTichAmTiet("học")
        assertEquals("h", ketQua.am_dau)
        assertEquals("hờ", ketQua.doc_am_dau)
        assertEquals("oc", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("o", ketQua.am_chinh)
        assertEquals("c", ketQua.am_cuoi)
        assertEquals("nặng", ketQua.thanh)
        assertEquals("hờ - oc - hoc - nặng - học", ketQua.danh_van)
    }

    @Test
    fun ban() {
        val ketQua = VietParser.phanTichAmTiet("bàn")
        assertEquals("b", ketQua.am_dau)
        assertEquals("an", ketQua.van)
        assertEquals("huyền", ketQua.thanh)
        assertEquals("bờ - an - ban - huyền - bàn", ketQua.danh_van)
    }

    @Test
    fun emKhongCoAmDau() {
        val ketQua = VietParser.phanTichAmTiet("em")
        assertEquals("", ketQua.am_dau)
        assertEquals("em", ketQua.van)
        assertEquals("", ketQua.doc_am_dau)
        assertEquals(listOf("khong_am_dau", "van_co_am_cuoi"), ketQua.nhom_bai_hoc)
        assertEquals("em - ngang - em", ketQua.danh_van)
    }

    @Test
    fun qua() {
        val ketQua = VietParser.phanTichAmTiet("quả")
        assertEquals("qu", ketQua.am_dau)
        assertEquals("quờ", ketQua.doc_am_dau)
        assertEquals("a", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("a", ketQua.am_chinh)
        assertEquals("", ketQua.am_cuoi)
        assertEquals("hỏi", ketQua.thanh)
        assertEquals("qua", ketQua.am_tiet_khong_dau)
        assertEquals(listOf("dac_biet_gi_qu"), ketQua.nhom_bai_hoc)
        assertEquals("kho", ketQua.do_kho)
        assertEquals("quờ - a - qua - hỏi - quả", ketQua.danh_van)
    }

    @Test
    fun cat() {
        val ketQua = VietParser.phanTichAmTiet("cắt")
        assertEquals("c", ketQua.am_dau)
        assertEquals("ăt", ketQua.van)
        assertEquals("ă", ketQua.am_chinh)
        assertEquals("t", ketQua.am_cuoi)
        assertEquals("sắc", ketQua.thanh)
        assertEquals("cat", ketQua.am_tiet_khong_dau)
        assertEquals("cờ - ăt - cat - sắc - cắt", ketQua.danh_van)
    }

    @Test
    fun hoaCoAmDem() {
        val ketQua = VietParser.phanTichAmTiet("hoa")
        assertEquals("h", ketQua.am_dau)
        assertEquals("oa", ketQua.van)
        assertEquals("o", ketQua.am_dem)
        assertEquals("a", ketQua.am_chinh)
        assertEquals("", ketQua.am_cuoi)
        assertTrue(ketQua.nhom_bai_hoc.contains("van_co_am_dem"))
        assertEquals("trung_binh", ketQua.do_kho)
    }

    @Test
    fun nghieng() {
        val ketQua = VietParser.phanTichAmTiet("nghiêng")
        assertEquals("ngh", ketQua.am_dau)
        assertEquals("ngờ", ketQua.doc_am_dau)
        assertEquals("iêng", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("iê", ketQua.am_chinh)
        assertEquals("ng", ketQua.am_cuoi)
        assertEquals("nghieng", ketQua.am_tiet_khong_dau)
        assertTrue(ketQua.nhom_bai_hoc.contains("am_dau_ghep"))
        assertTrue(ketQua.nhom_bai_hoc.contains("van_co_am_cuoi"))
        assertEquals("kho", ketQua.do_kho)
        assertEquals("ngờ - iêng - nghieng - ngang - nghiêng", ketQua.danh_van)
    }

    @Test
    fun giaDacBietGi() {
        val ketQua = VietParser.phanTichAmTiet("giá")
        assertEquals("gi", ketQua.am_dau)
        assertEquals("dờ", ketQua.doc_am_dau)
        assertEquals("a", ketQua.van)
        assertEquals("sắc", ketQua.thanh)
        assertEquals("dờ - a - gia - sắc - giá", ketQua.danh_van)
    }

    @Test
    fun giDungMotMinh() {
        val ketQua = VietParser.phanTichAmTiet("gi")
        assertEquals("g", ketQua.am_dau)
        assertEquals("gờ", ketQua.doc_am_dau)
        assertEquals("i", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("i", ketQua.am_chinh)
        assertEquals("", ketQua.am_cuoi)
        assertEquals("ngang", ketQua.thanh)
        assertEquals("gi", ketQua.am_tiet_khong_dau)
        assertEquals("gờ - i - gi - ngang - gi", ketQua.danh_van)
    }

    @Test
    fun ghen() {
        val ketQua = VietParser.phanTichAmTiet("ghen")
        assertEquals("gh", ketQua.am_dau)
        assertEquals("gờ", ketQua.doc_am_dau)
        assertEquals("en", ketQua.van)
        assertEquals("kho", ketQua.do_kho)
    }

    @Test
    fun toanVanPhuc() {
        val ketQua = VietParser.phanTichAmTiet("toán")
        assertEquals("t", ketQua.am_dau)
        assertEquals("oan", ketQua.van)
        assertEquals("o", ketQua.am_dem)
        assertEquals("a", ketQua.am_chinh)
        assertEquals("n", ketQua.am_cuoi)
        assertEquals("kho", ketQua.do_kho)
        assertEquals(
            listOf("am_dau_don", "van_co_am_dem", "van_co_am_cuoi", "van_phuc"),
            ketQua.nhom_bai_hoc,
        )
    }

    @Test
    fun oanhKhongAmDau() {
        val ketQua = VietParser.phanTichAmTiet("oanh")
        assertEquals("", ketQua.am_dau)
        assertEquals("oanh", ketQua.van)
        assertEquals("o", ketQua.am_dem)
        assertEquals("a", ketQua.am_chinh)
        assertEquals("nh", ketQua.am_cuoi)
        assertEquals(
            listOf("khong_am_dau", "van_co_am_dem", "van_co_am_cuoi", "van_phuc"),
            ketQua.nhom_bai_hoc,
        )
        assertEquals("oanh - ngang - oanh", ketQua.danh_van)
    }

    @Test
    fun doKhoCascadeDe() {
        assertEquals("de", VietParser.phanTichAmTiet("ve").do_kho)
    }

    @Test
    fun doKhoCascadeTrungBinh() {
        assertEquals("trung_binh", VietParser.phanTichAmTiet("bạn").do_kho)
    }

    @Test
    fun tho() {
        val ketQua = VietParser.phanTichAmTiet("thỏ")
        assertEquals("th", ketQua.am_dau)
        assertEquals("o", ketQua.van)
        assertEquals("hỏi", ketQua.thanh)
        assertEquals("thờ - o - tho - hỏi - thỏ", ketQua.danh_van)
    }

    @Test
    fun dat() {
        val ketQua = VietParser.phanTichAmTiet("đất")
        assertEquals("đ", ketQua.am_dau)
        assertEquals("ât", ketQua.van)
        assertEquals("â", ketQua.am_chinh)
        assertEquals("t", ketQua.am_cuoi)
        assertEquals("sắc", ketQua.thanh)
        assertEquals("dat", ketQua.am_tiet_khong_dau)
    }

    @Test
    fun ngoaiLeOat() {
        val ketQua = VietParser.phanTichAmTiet("oắt")
        assertTrue(ketQua.ngoai_le)
        assertEquals("kho", ketQua.do_kho)
        assertEquals("oat", ketQua.am_tiet_khong_dau)
    }

    @Test
    fun ngoaiLeUynh() {
        val ketQua = VietParser.phanTichAmTiet("uỳnh")
        assertTrue(ketQua.ngoai_le)
        assertEquals("uynh - huyền - uỳnh", ketQua.danh_van)
    }

    @Test
    fun uongNguyenAmDoi() {
        val ketQua = VietParser.phanTichAmTiet("uống")
        assertEquals("uông", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("uô", ketQua.am_chinh)
        assertEquals("ng", ketQua.am_cuoi)
    }

    @Test
    fun duocNguyenAmDoi() {
        val ketQua = VietParser.phanTichAmTiet("được")
        assertEquals("ươc", ketQua.van)
        assertEquals("", ketQua.am_dem)
        assertEquals("ươ", ketQua.am_chinh)
        assertEquals("c", ketQua.am_cuoi)
    }

    @Test
    fun boThanhQua() {
        assertEquals("qua", VietParser.boThanh("quả"))
    }

    @Test
    fun boThanhNghieng() {
        assertEquals("nghieng", VietParser.boThanh("nghiêng"))
    }

    @Test
    fun boThanhDat() {
        assertEquals("dat", VietParser.boThanh("đất"))
    }

    @Test
    fun ghepDanhVanBan() {
        val amTiet = VietParser.phanTichAmTiet("bạn")
        assertEquals("bờ - an - ban - nặng - bạn", VietParser.ghepDanhVan(amTiet))
    }

    @Test
    fun ghepDanhVanOanh() {
        val amTiet = VietParser.phanTichAmTiet("oanh")
        assertEquals("oanh - ngang - oanh", VietParser.ghepDanhVan(amTiet))
    }
}
