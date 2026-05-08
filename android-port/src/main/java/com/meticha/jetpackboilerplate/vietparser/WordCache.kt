package com.meticha.jetpackboilerplate.vietparser

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object WordCache {
    private const val ASSET_FILE_NAME = "sgk_lop1.jsonl"

    private val executor = Executors.newSingleThreadExecutor()
    private val cache = ConcurrentHashMap<String, AmTiet>()

    @Volatile
    private var daTai = false

    fun load(context: Context) {
        executor.execute {
            context.assets.open(ASSET_FILE_NAME).use { input ->
                BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                    loadFromJsonLines(reader.lineSequence())
                }
            }
        }
    }

    fun layRecord(tu: String): AmTiet {
        val tuChuan = VietParser.chuanHoaTu(tu)
        val cached = cache[tuChuan]
        return if (cached != null) {
            cached.copy(cache_hit = true)
        } else {
            VietParser.phanTichAmTiet(tuChuan).copy(cache_hit = false)
        }
    }

    internal fun loadFromJsonLines(lines: Sequence<String>) {
        val parsed = LinkedHashMap<String, AmTiet>()
        for (line in lines) {
            if (line.isBlank()) {
                continue
            }
            val amTiet = parseJsonLine(line)
            parsed[amTiet.tu] = amTiet.copy(cache_hit = false)
        }
        cache.clear()
        cache.putAll(parsed)
        daTai = true
    }

    internal fun resetForTest() {
        cache.clear()
        daTai = false
    }

    internal fun sizeForTest(): Int = cache.size

    internal fun isLoadedForTest(): Boolean = daTai

    private fun parseJsonLine(line: String): AmTiet {
        val root = JSONObject(line)
        val linguistics = root.getJSONObject("linguistics")
        val pedagogy = root.getJSONObject("pedagogy")
        val metadata = root.getJSONObject("metadata")

        val nhom = mutableListOf<String>()
        val nhomJson = pedagogy.getJSONArray("nhom_bai_hoc")
        for (index in 0 until nhomJson.length()) {
            nhom += nhomJson.getString(index)
        }

        return AmTiet(
            tu = metadata.getString("word"),
            am_dau = linguistics.getString("am_dau"),
            doc_am_dau = linguistics.getString("doc_am_dau"),
            van = linguistics.getString("van"),
            am_dem = linguistics.getString("am_dem"),
            am_chinh = linguistics.getString("am_chinh"),
            am_cuoi = linguistics.getString("am_cuoi"),
            thanh = linguistics.getString("thanh"),
            am_tiet_khong_dau = linguistics.getString("am_tiet_khong_dau"),
            danh_van = pedagogy.getString("danh_van"),
            do_kho = pedagogy.getString("do_kho"),
            nhom_bai_hoc = nhom,
            ngoai_le = linguistics.getBoolean("ngoai_le"),
            cache_hit = false,
            source = metadata.getString("source"),
        )
    }
}
