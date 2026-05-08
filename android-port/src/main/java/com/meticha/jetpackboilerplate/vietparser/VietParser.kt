package com.meticha.jetpackboilerplate.vietparser

import java.text.Normalizer
import java.util.Locale

object VietParser {
    val AM_DAU_DOC = linkedMapOf(
        "" to "",
        "b" to "bờ",
        "c" to "cờ",
        "ch" to "chờ",
        "d" to "dờ",
        "đ" to "đờ",
        "g" to "gờ",
        "gh" to "gờ",
        "gi" to "dờ",
        "h" to "hờ",
        "k" to "cờ",
        "kh" to "khờ",
        "l" to "lờ",
        "m" to "mờ",
        "n" to "nờ",
        "ng" to "ngờ",
        "ngh" to "ngờ",
        "nh" to "nhờ",
        "p" to "pờ",
        "ph" to "phờ",
        "qu" to "quờ",
        "r" to "rờ",
        "s" to "sờ",
        "t" to "tờ",
        "th" to "thờ",
        "tr" to "trờ",
        "v" to "vờ",
        "x" to "xờ",
    )

    val THANH_DAU = linkedMapOf(
        '\u0300' to "huyền",
        '\u0301' to "sắc",
        '\u0303' to "ngã",
        '\u0309' to "hỏi",
        '\u0323' to "nặng",
    )

    private val THU_TU_AM_DAU = AM_DAU_DOC.keys
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.length }

    private val THU_TU_AM_CUOI = listOf("ch", "nh", "ng", "c", "m", "n", "p", "t", "i", "y", "u", "o")
    private val NGUYEN_AM = setOf('a', 'ă', 'â', 'e', 'ê', 'i', 'o', 'ô', 'ơ', 'u', 'ư', 'y')
    private val AM_DAU_KHO = setOf("gi", "qu", "gh", "ngh")
    private val AM_DAU_GHEP = setOf("ch", "gh", "kh", "ng", "ngh", "nh", "ph", "th", "tr")
    private val DAC_BIET_GI_QU = setOf("gi", "qu")
    private val AM_DEM_THEO_U = setOf('a', 'ă', 'â', 'e', 'ê', 'y')
    private val LOCALE_VI = Locale("vi", "VN")

    private data class CauHinhNgoaiLe(
        val am_dau: String,
        val doc_am_dau: String,
        val van: String,
        val am_dem: String,
        val am_chinh: String,
        val am_cuoi: String,
        val thanh: String,
    )

    private data class ThanhPhanAmTiet(
        val am_dau: String,
        val doc_am_dau: String,
        val van: String,
        val am_dem: String,
        val am_chinh: String,
        val am_cuoi: String,
        val thanh: String,
        val ngoai_le: Boolean,
        val source: String,
    )

    private val NGOAI_LE = mapOf(
        "oắt" to CauHinhNgoaiLe("", "", "oăt", "o", "ă", "t", "sắc"),
        "uỳnh" to CauHinhNgoaiLe("", "", "uynh", "u", "y", "nh", "huyền"),
    )

    fun boThanh(tu: String): String {
        val builder = StringBuilder()
        val decomposed = Normalizer.normalize(tu, Normalizer.Form.NFD)
        for (char in decomposed) {
            if (isCombiningMark(char)) {
                continue
            }
            when (char) {
                'đ' -> builder.append('d')
                'Đ' -> builder.append('D')
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    fun phanTichAmTiet(tu: String): AmTiet {
        val tuChuan = chuanHoaTu(tu)
        val amTietKhongDau = boThanh(tuChuan)

        val thanhPhan = NGOAI_LE[tuChuan]?.let {
            ThanhPhanAmTiet(
                am_dau = it.am_dau,
                doc_am_dau = it.doc_am_dau,
                van = it.van,
                am_dem = it.am_dem,
                am_chinh = it.am_chinh,
                am_cuoi = it.am_cuoi,
                thanh = it.thanh,
                ngoai_le = true,
                source = "ngoai_le",
            )
        } ?: run {
            val (amTietTachVan, thanhPhanTich) = tachThanhKhoiAmTiet(tuChuan)
            val (amDauPhanTich, vanPhanTich) = if (amTietTachVan == "gi") {
                "g" to "i"
            } else {
                tachAmDau(amTietTachVan)
            }
            val docAmDauPhanTich = AM_DAU_DOC[amDauPhanTich]
                ?: throw IllegalArgumentException("Am dau chua ho tro: $amDauPhanTich")
            val (amDemPhanTich, amChinhPhanTich, amCuoiPhanTich) = phanTichVan(vanPhanTich)
            ThanhPhanAmTiet(
                am_dau = amDauPhanTich,
                doc_am_dau = docAmDauPhanTich,
                van = vanPhanTich,
                am_dem = amDemPhanTich,
                am_chinh = amChinhPhanTich,
                am_cuoi = amCuoiPhanTich,
                thanh = thanhPhanTich,
                ngoai_le = false,
                source = "sgk",
            )
        }

        val nhomBaiHoc = taoNhomBaiHoc(thanhPhan.am_dau, thanhPhan.am_dem, thanhPhan.am_cuoi)
        val ketQuaTam = AmTiet(
            tu = tuChuan,
            am_dau = thanhPhan.am_dau,
            doc_am_dau = thanhPhan.doc_am_dau,
            van = thanhPhan.van,
            am_dem = thanhPhan.am_dem,
            am_chinh = thanhPhan.am_chinh,
            am_cuoi = thanhPhan.am_cuoi,
            thanh = thanhPhan.thanh,
            am_tiet_khong_dau = amTietKhongDau,
            danh_van = "",
            do_kho = "",
            nhom_bai_hoc = nhomBaiHoc,
            ngoai_le = thanhPhan.ngoai_le,
            cache_hit = false,
            source = thanhPhan.source,
        )
        val doKho = tinhDoKho(ketQuaTam)
        val coDoKho = ketQuaTam.copy(do_kho = doKho)
        return coDoKho.copy(danh_van = ghepDanhVan(coDoKho))
    }

    fun ghepDanhVan(amTiet: AmTiet): String {
        return if (amTiet.doc_am_dau.isNotEmpty()) {
            "${amTiet.doc_am_dau} - ${amTiet.van} - ${amTiet.am_tiet_khong_dau} - ${amTiet.thanh} - ${amTiet.tu}"
        } else {
            "${amTiet.van} - ${amTiet.thanh} - ${amTiet.tu}"
        }
    }

    fun tinhDoKho(amTiet: AmTiet): String {
        val vanPhuc = amTiet.am_dem.isNotEmpty() && amTiet.am_cuoi.isNotEmpty()
        if (amTiet.ngoai_le) {
            return "kho"
        }
        if (amTiet.am_dau in AM_DAU_KHO || vanPhuc) {
            return "kho"
        }
        if (amTiet.thanh in setOf("sắc", "hỏi", "ngã", "nặng") ||
            amTiet.am_cuoi.isNotEmpty() ||
            amTiet.am_dem.isNotEmpty()
        ) {
            return "trung_binh"
        }
        return "de"
    }

    internal fun chuanHoaTu(tu: String): String {
        val tuChuan = Normalizer.normalize(tu.trim().lowercase(LOCALE_VI), Normalizer.Form.NFC)
        require(tuChuan.isNotEmpty() && !tuChuan.contains(' ')) { "Tu khong hop le: $tu" }
        return tuChuan
    }

    private fun tachThanhKhoiAmTiet(tu: String): Pair<String, String> {
        var thanh = "ngang"
        val builder = StringBuilder()
        val decomposed = Normalizer.normalize(tu, Normalizer.Form.NFD)
        for (char in decomposed) {
            if (isCombiningMark(char)) {
                val tone = THANH_DAU[char]
                if (tone != null) {
                    thanh = tone
                } else {
                    builder.append(char)
                }
            } else {
                builder.append(char)
            }
        }
        val khongDauThanh = Normalizer.normalize(builder.toString(), Normalizer.Form.NFC)
        return khongDauThanh to thanh
    }

    private fun tachAmDau(amTietKhongDauThanh: String): Pair<String, String> {
        for (amDau in THU_TU_AM_DAU) {
            if (amTietKhongDauThanh.startsWith(amDau)) {
                return amDau to amTietKhongDauThanh.removePrefix(amDau)
            }
        }
        return "" to amTietKhongDauThanh
    }

    private fun coNguyenAm(chuoi: String): Boolean = chuoi.any { it in NGUYEN_AM }

    private fun tachAmCuoi(van: String): Pair<String, String> {
        for (amCuoi in THU_TU_AM_CUOI) {
            if (van.endsWith(amCuoi) && van.length > amCuoi.length) {
                val thanVan = van.dropLast(amCuoi.length)
                if (coNguyenAm(thanVan)) {
                    return thanVan to amCuoi
                }
            }
        }
        return van to ""
    }

    private fun tachAmDemVaAmChinh(thanVan: String): Triple<String, String, String> {
        if (thanVan.length >= 2 && thanVan[0] == 'o' && thanVan[1] in NGUYEN_AM) {
            return Triple("o", thanVan.substring(1), "")
        }
        if (thanVan.length >= 2 && thanVan[0] == 'u' && thanVan[1] in AM_DEM_THEO_U) {
            return Triple("u", thanVan.substring(1), "")
        }
        return Triple("", thanVan, "")
    }

    private fun phanTichVan(van: String): Triple<String, String, String> {
        val (thanVan, amCuoi) = tachAmCuoi(van)
        val (amDem, amChinh, _) = tachAmDemVaAmChinh(thanVan)
        require(amChinh.isNotEmpty() && coNguyenAm(amChinh)) { "Khong phan tich duoc van: '$van'" }
        return Triple(amDem, amChinh, amCuoi)
    }

    private fun taoNhomBaiHoc(amDau: String, amDem: String, amCuoi: String): List<String> {
        val nhom = mutableListOf<String>()
        if (amDau.isEmpty()) {
            nhom += "khong_am_dau"
        } else if (amDau.length == 1) {
            nhom += "am_dau_don"
        } else if (amDau in AM_DAU_GHEP) {
            nhom += "am_dau_ghep"
        }
        if (amDau in DAC_BIET_GI_QU) {
            nhom += "dac_biet_gi_qu"
        }
        if (amDem.isNotEmpty()) {
            nhom += "van_co_am_dem"
        }
        if (amCuoi.isNotEmpty()) {
            nhom += "van_co_am_cuoi"
        }
        if (amDem.isNotEmpty() && amCuoi.isNotEmpty()) {
            nhom += "van_phuc"
        }
        return nhom
    }

    private fun isCombiningMark(char: Char): Boolean {
        return when (Character.getType(char).toInt()) {
            Character.NON_SPACING_MARK.toInt(),
            Character.COMBINING_SPACING_MARK.toInt(),
            Character.ENCLOSING_MARK.toInt() -> true
            else -> false
        }
    }
}
