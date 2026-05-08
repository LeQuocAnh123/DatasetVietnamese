package com.meticha.jetpackboilerplate.vietparser

data class AmTiet(
    val tu: String,
    val am_dau: String,
    val doc_am_dau: String,
    val van: String,
    val am_dem: String,
    val am_chinh: String,
    val am_cuoi: String,
    val thanh: String,
    val am_tiet_khong_dau: String,
    val danh_van: String,
    val do_kho: String,
    val nhom_bai_hoc: List<String>,
    val ngoai_le: Boolean,
    val cache_hit: Boolean,
    val source: String = "sgk",
)
