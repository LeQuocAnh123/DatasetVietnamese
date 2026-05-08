import json
import tempfile
import unittest
import unicodedata
import uuid
from pathlib import Path

VERSION = "1.0.0"
SCHEMA_VERSION = "2020-12"
ONSET_READINGS = {
    "": "",
    "b": "bờ",
    "c": "cờ",
    "ch": "chờ",
    "d": "dờ",
    "đ": "đờ",
    "g": "gờ",
    "gh": "gờ",
    "gi": "dờ",
    "h": "hờ",
    "k": "cờ",
    "kh": "khờ",
    "l": "lờ",
    "m": "mờ",
    "n": "nờ",
    "ng": "ngờ",
    "ngh": "ngờ",
    "nh": "nhờ",
    "p": "pờ",
    "ph": "phờ",
    "qu": "quờ",
    "r": "rờ",
    "s": "sờ",
    "t": "tờ",
    "th": "thờ",
    "tr": "trờ",
    "v": "vờ",
    "x": "xờ",
}
ORDERED_ONSETS = sorted((onset for onset in ONSET_READINGS if onset), key=len, reverse=True)
ORDERED_FINALS = ["ch", "nh", "ng", "c", "m", "n", "p", "t", "i", "y", "u", "o"]
TONE_MARKS = {
    "\u0300": "huyền",
    "\u0301": "sắc",
    "\u0303": "ngã",
    "\u0309": "hỏi",
    "\u0323": "nặng",
}
VOWELS = set("aăâeêioôơuưy")
DIFFICULT_ONSETS = {"gi", "qu", "gh", "ngh"}
COMPLEX_ONSETS = {"ch", "gh", "kh", "ng", "ngh", "nh", "ph", "th", "tr"}
SPECIAL_GI_QU = {"gi", "qu"}
MEDIAL_U_FOLLOWERS = {"a", "ă", "â", "e", "ê", "y"}
EXCEPTION_WORDS = {
    "oắt": {
        "am_dau": "",
        "doc_am_dau": "",
        "van": "oăt",
        "am_dem": "o",
        "am_chinh": "ă",
        "am_cuoi": "t",
        "thanh": "sắc",
    },
    "uỳnh": {
        "am_dau": "",
        "doc_am_dau": "",
        "van": "uynh",
        "am_dem": "u",
        "am_chinh": "y",
        "am_cuoi": "nh",
        "thanh": "huyền",
    },
}
SAMPLE_WORDS = [
    "học",
    "bàn",
    "em",
    "quả",
    "cắt",
    "hoa",
    "ghen",
    "nghiêng",
    "giá",
    "giỏi",
    "quang",
    "toán",
    "bạn",
    "oanh",
    "bé",
    "thỏ",
    "ngã",
    "đất",
    "oắt",
    "uỳnh",
]


def chuan_hoa_tu(tu):
    tu_chuan = unicodedata.normalize("NFC", tu.strip().lower())
    if not tu_chuan or " " in tu_chuan:
        raise ValueError(f"Tu khong hop le: {tu!r}")
    return tu_chuan


def tach_thanh_khoi_am_tiet(tu):
    thanh = "ngang"
    ky_tu = []
    for char in unicodedata.normalize("NFD", tu):
        if unicodedata.combining(char):
            if char in TONE_MARKS:
                thanh = TONE_MARKS[char]
            else:
                ky_tu.append(char)
        else:
            ky_tu.append(char)
    return unicodedata.normalize("NFC", "".join(ky_tu)), thanh


def bo_dau_ascii(tu):
    ky_tu = []
    for char in unicodedata.normalize("NFD", tu):
        if unicodedata.category(char).startswith("M"):
            continue
        if char == "đ":
            ky_tu.append("d")
        elif char == "Đ":
            ky_tu.append("D")
        else:
            ky_tu.append(char)
    return "".join(ky_tu)


def tach_am_dau(am_tiet_khong_dau):
    for am_dau in ORDERED_ONSETS:
        if am_tiet_khong_dau.startswith(am_dau):
            return am_dau, am_tiet_khong_dau[len(am_dau) :]
    return "", am_tiet_khong_dau


def co_nguyen_am(chuoi):
    return any(ky_tu in VOWELS for ky_tu in chuoi)


def tach_am_cuoi(van):
    for am_cuoi in ORDERED_FINALS:
        if van.endswith(am_cuoi) and len(van) > len(am_cuoi) and co_nguyen_am(van[: -len(am_cuoi)]):
            return van[: -len(am_cuoi)], am_cuoi
    return van, ""


def tach_am_dem_va_am_chinh(than_van):
    if len(than_van) >= 2 and than_van[0] == "o" and than_van[1] in VOWELS:
        return "o", than_van[1:]
    if len(than_van) >= 2 and than_van[0] == "u" and than_van[1] in MEDIAL_U_FOLLOWERS:
        return "u", than_van[1:]
    return "", than_van


def phan_tich_van(van):
    than_van, am_cuoi = tach_am_cuoi(van)
    am_dem, am_chinh = tach_am_dem_va_am_chinh(than_van)
    if not am_chinh or not co_nguyen_am(am_chinh):
        raise ValueError(f"Khong phan tich duoc van: {van!r}")
    return am_dem, am_chinh, am_cuoi


def tao_nhom_bai_hoc(am_dau, am_dem, am_cuoi):
    nhom = []
    if am_dau == "":
        nhom.append("khong_am_dau")
    elif len(am_dau) == 1:
        nhom.append("am_dau_don")
    elif am_dau in COMPLEX_ONSETS:
        nhom.append("am_dau_ghep")
    if am_dau in SPECIAL_GI_QU:
        nhom.append("dac_biet_gi_qu")
    if am_dem:
        nhom.append("van_co_am_dem")
    if am_cuoi:
        nhom.append("van_co_am_cuoi")
    if am_dem and am_cuoi:
        nhom.append("van_phuc")
    return nhom


def tinh_do_kho(ngoai_le, am_dau, am_dem, am_cuoi, thanh):
    van_phuc = bool(am_dem and am_cuoi)
    if ngoai_le:
        return "kho"
    if am_dau in DIFFICULT_ONSETS or van_phuc:
        return "kho"
    if thanh in {"sắc", "hỏi", "ngã", "nặng"} or am_cuoi or am_dem:
        return "trung_binh"
    return "de"


def tao_danh_van(doc_am_dau, van, am_tiet_khong_dau, thanh, tu_day_du):
    if doc_am_dau:
        return f"{doc_am_dau} - {van} - {am_tiet_khong_dau} - {thanh} - {tu_day_du}"
    return f"{van} - {thanh} - {tu_day_du}"


def phan_tich_am_tiet(tu):
    tu_chuan = chuan_hoa_tu(tu)
    am_tiet_khong_dau = bo_dau_ascii(tu_chuan)
    if tu_chuan in EXCEPTION_WORDS:
        co_so = dict(EXCEPTION_WORDS[tu_chuan])
        ngoai_le = True
        source = "ngoai_le"
    else:
        am_tiet_tach_van, thanh = tach_thanh_khoi_am_tiet(tu_chuan)
        if am_tiet_tach_van == "gi":
            am_dau, van = "g", "i"
        else:
            am_dau, van = tach_am_dau(am_tiet_tach_van)
        doc_am_dau = ONSET_READINGS.get(am_dau)
        if doc_am_dau is None:
            raise ValueError(f"Am dau chua ho tro: {am_dau!r}")
        am_dem, am_chinh, am_cuoi = phan_tich_van(van)
        co_so = {
            "am_dau": am_dau,
            "doc_am_dau": doc_am_dau,
            "van": van,
            "am_dem": am_dem,
            "am_chinh": am_chinh,
            "am_cuoi": am_cuoi,
            "thanh": thanh,
        }
        ngoai_le = False
        source = "sgk"
    co_so["am_tiet_khong_dau"] = am_tiet_khong_dau

    nhom_bai_hoc = tao_nhom_bai_hoc(co_so["am_dau"], co_so["am_dem"], co_so["am_cuoi"])
    do_kho = tinh_do_kho(
        ngoai_le,
        co_so["am_dau"],
        co_so["am_dem"],
        co_so["am_cuoi"],
        co_so["thanh"],
    )
    danh_van = tao_danh_van(
        co_so["doc_am_dau"],
        co_so["van"],
        co_so["am_tiet_khong_dau"],
        co_so["thanh"],
        tu_chuan,
    )
    return {
        "tu": tu_chuan,
        **co_so,
        "ngoai_le": ngoai_le,
        "source": source,
        "nhom_bai_hoc": nhom_bai_hoc,
        "do_kho": do_kho,
        "danh_van": danh_van,
    }


def tao_id():
    return str(uuid.uuid4())


def tao_dataset(danh_sach_tu):
    records = []
    for tu in danh_sach_tu:
        phan_tich = phan_tich_am_tiet(tu)
        records.append(
            {
                "linguistics": {
                    "am_dau": phan_tich["am_dau"],
                    "doc_am_dau": phan_tich["doc_am_dau"],
                    "van": phan_tich["van"],
                    "am_dem": phan_tich["am_dem"],
                    "am_chinh": phan_tich["am_chinh"],
                    "am_cuoi": phan_tich["am_cuoi"],
                    "thanh": phan_tich["thanh"],
                    "am_tiet_khong_dau": phan_tich["am_tiet_khong_dau"],
                    "ngoai_le": phan_tich["ngoai_le"],
                },
                "pedagogy": {
                    "danh_van": phan_tich["danh_van"],
                    "do_kho": phan_tich["do_kho"],
                    "nhom_bai_hoc": phan_tich["nhom_bai_hoc"],
                },
                "metadata": {
                    "id": tao_id(),
                    "word": phan_tich["tu"],
                    "version": VERSION,
                    "source": phan_tich["source"],
                },
            }
        )
    return records


def xuat_json(records, output_path):
    output = Path(output_path)
    with output.open("w", encoding="utf-8") as file:
        for record in records:
            file.write(json.dumps(record, ensure_ascii=False) + "\n")


def validate_records(records):
    errors = []
    seen_ids = {}
    for index, record in enumerate(records, start=1):
        word = record["metadata"]["word"]
        record_id = record["metadata"]["id"]
        linguistics = record["linguistics"]
        pedagogy = record["pedagogy"]

        if record_id in seen_ids:
            errors.append(
                {
                    "word": word,
                    "field": "metadata.id",
                    "current": record_id,
                    "expected": f"unique id; duplicated with record {seen_ids[record_id]}",
                }
            )
        else:
            seen_ids[record_id] = index

        expected_ascii = bo_dau_ascii(word)
        current_ascii = linguistics["am_tiet_khong_dau"]
        if current_ascii != expected_ascii or not current_ascii.isascii():
            errors.append(
                {
                    "word": word,
                    "field": "linguistics.am_tiet_khong_dau",
                    "current": current_ascii,
                    "expected": expected_ascii,
                }
            )

        expected_van = linguistics["am_dem"] + linguistics["am_chinh"] + linguistics["am_cuoi"]
        if linguistics["van"] != expected_van:
            errors.append(
                {
                    "word": word,
                    "field": "linguistics.van",
                    "current": linguistics["van"],
                    "expected": expected_van,
                }
            )

        if linguistics["doc_am_dau"]:
            expected_danh_van = (
                f"{linguistics['doc_am_dau']} - {linguistics['van']} - "
                f"{current_ascii} - {linguistics['thanh']} - {word}"
            )
        else:
            expected_danh_van = f"{linguistics['van']} - {linguistics['thanh']} - {word}"
        if pedagogy["danh_van"] != expected_danh_van:
            errors.append(
                {
                    "word": word,
                    "field": "pedagogy.danh_van",
                    "current": pedagogy["danh_van"],
                    "expected": expected_danh_van,
                }
            )
    return errors


def tao_schema():
    return {
        "$schema": f"https://json-schema.org/draft/{SCHEMA_VERSION}/schema",
        "title": "Vietnamese Spelling Dataset Record",
        "type": "object",
        "additionalProperties": False,
        "required": ["linguistics", "pedagogy", "metadata"],
        "properties": {
            "linguistics": {
                "type": "object",
                "additionalProperties": False,
                "required": [
                    "am_dau",
                    "doc_am_dau",
                    "van",
                    "am_dem",
                    "am_chinh",
                    "am_cuoi",
                    "thanh",
                    "am_tiet_khong_dau",
                    "ngoai_le",
                ],
                "properties": {
                    "am_dau": {"type": "string"},
                    "doc_am_dau": {"type": "string"},
                    "van": {"type": "string"},
                    "am_dem": {"type": "string"},
                    "am_chinh": {"type": "string"},
                    "am_cuoi": {"type": "string"},
                    "thanh": {"type": "string", "enum": ["ngang", "huyền", "sắc", "hỏi", "ngã", "nặng"]},
                    "am_tiet_khong_dau": {"type": "string"},
                    "ngoai_le": {"type": "boolean"},
                },
            },
            "pedagogy": {
                "type": "object",
                "additionalProperties": False,
                "required": ["danh_van", "do_kho", "nhom_bai_hoc"],
                "properties": {
                    "danh_van": {"type": "string"},
                    "do_kho": {"type": "string", "enum": ["de", "trung_binh", "kho"]},
                    "nhom_bai_hoc": {
                        "type": "array",
                        "items": {
                            "type": "string",
                            "enum": [
                                "khong_am_dau",
                                "am_dau_don",
                                "am_dau_ghep",
                                "dac_biet_gi_qu",
                                "van_co_am_dem",
                                "van_co_am_cuoi",
                                "van_phuc",
                            ],
                        },
                    },
                },
            },
            "metadata": {
                "type": "object",
                "additionalProperties": False,
                "required": ["id", "word", "version", "source"],
                "properties": {
                    "id": {"type": "string"},
                    "word": {"type": "string"},
                    "version": {"type": "string"},
                    "source": {"type": "string", "enum": ["sgk", "ngoai_le", "vay_muon"]},
                },
            },
        },
    }


def xuat_schema(output_path):
    Path(output_path).write_text(
        json.dumps(tao_schema(), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def main():
    root = Path(__file__).resolve().parent
    records = tao_dataset(SAMPLE_WORDS)
    xuat_json(records, root / "sample_output.jsonl")
    xuat_schema(root / "schema.json")


class TestPhanTichAmTiet(unittest.TestCase):
    def test_hoc(self):
        ket_qua = phan_tich_am_tiet("học")
        self.assertEqual(ket_qua["am_dau"], "h")
        self.assertEqual(ket_qua["doc_am_dau"], "hờ")
        self.assertEqual(ket_qua["van"], "oc")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "o")
        self.assertEqual(ket_qua["am_cuoi"], "c")
        self.assertEqual(ket_qua["thanh"], "nặng")
        self.assertEqual(ket_qua["danh_van"], "hờ - oc - hoc - nặng - học")

    def test_ban(self):
        ket_qua = phan_tich_am_tiet("bàn")
        self.assertEqual(ket_qua["am_dau"], "b")
        self.assertEqual(ket_qua["van"], "an")
        self.assertEqual(ket_qua["thanh"], "huyền")
        self.assertEqual(ket_qua["danh_van"], "bờ - an - ban - huyền - bàn")

    def test_em_khong_co_am_dau(self):
        ket_qua = phan_tich_am_tiet("em")
        self.assertEqual(ket_qua["am_dau"], "")
        self.assertEqual(ket_qua["van"], "em")
        self.assertEqual(ket_qua["doc_am_dau"], "")
        self.assertEqual(ket_qua["nhom_bai_hoc"], ["khong_am_dau", "van_co_am_cuoi"])
        self.assertEqual(ket_qua["danh_van"], "em - ngang - em")

    def test_qua(self):
        ket_qua = phan_tich_am_tiet("quả")
        self.assertEqual(ket_qua["am_dau"], "qu")
        self.assertEqual(ket_qua["doc_am_dau"], "quờ")
        self.assertEqual(ket_qua["van"], "a")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "a")
        self.assertEqual(ket_qua["am_cuoi"], "")
        self.assertEqual(ket_qua["thanh"], "hỏi")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "qua")
        self.assertEqual(ket_qua["nhom_bai_hoc"], ["dac_biet_gi_qu"])
        self.assertEqual(ket_qua["do_kho"], "kho")
        self.assertEqual(ket_qua["danh_van"], "quờ - a - qua - hỏi - quả")

    def test_cat(self):
        ket_qua = phan_tich_am_tiet("cắt")
        self.assertEqual(ket_qua["am_dau"], "c")
        self.assertEqual(ket_qua["van"], "ăt")
        self.assertEqual(ket_qua["am_chinh"], "ă")
        self.assertEqual(ket_qua["am_cuoi"], "t")
        self.assertEqual(ket_qua["thanh"], "sắc")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "cat")
        self.assertEqual(ket_qua["danh_van"], "cờ - ăt - cat - sắc - cắt")

    def test_hoa_co_am_dem(self):
        ket_qua = phan_tich_am_tiet("hoa")
        self.assertEqual(ket_qua["am_dau"], "h")
        self.assertEqual(ket_qua["van"], "oa")
        self.assertEqual(ket_qua["am_dem"], "o")
        self.assertEqual(ket_qua["am_chinh"], "a")
        self.assertEqual(ket_qua["am_cuoi"], "")
        self.assertIn("van_co_am_dem", ket_qua["nhom_bai_hoc"])
        self.assertEqual(ket_qua["do_kho"], "trung_binh")

    def test_nghieng(self):
        ket_qua = phan_tich_am_tiet("nghiêng")
        self.assertEqual(ket_qua["am_dau"], "ngh")
        self.assertEqual(ket_qua["doc_am_dau"], "ngờ")
        self.assertEqual(ket_qua["van"], "iêng")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "iê")
        self.assertEqual(ket_qua["am_cuoi"], "ng")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "nghieng")
        self.assertIn("am_dau_ghep", ket_qua["nhom_bai_hoc"])
        self.assertIn("van_co_am_cuoi", ket_qua["nhom_bai_hoc"])
        self.assertEqual(ket_qua["do_kho"], "kho")
        self.assertEqual(ket_qua["danh_van"], "ngờ - iêng - nghieng - ngang - nghiêng")

    def test_gia_dac_biet_gi(self):
        ket_qua = phan_tich_am_tiet("giá")
        self.assertEqual(ket_qua["am_dau"], "gi")
        self.assertEqual(ket_qua["doc_am_dau"], "dờ")
        self.assertEqual(ket_qua["van"], "a")
        self.assertEqual(ket_qua["thanh"], "sắc")
        self.assertEqual(ket_qua["danh_van"], "dờ - a - gia - sắc - giá")

    def test_gi_dung_mot_minh(self):
        ket_qua = phan_tich_am_tiet("gi")
        self.assertEqual(ket_qua["am_dau"], "g")
        self.assertEqual(ket_qua["doc_am_dau"], "gờ")
        self.assertEqual(ket_qua["van"], "i")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "i")
        self.assertEqual(ket_qua["am_cuoi"], "")
        self.assertEqual(ket_qua["thanh"], "ngang")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "gi")
        self.assertEqual(ket_qua["danh_van"], "gờ - i - gi - ngang - gi")

    def test_ghen(self):
        ket_qua = phan_tich_am_tiet("ghen")
        self.assertEqual(ket_qua["am_dau"], "gh")
        self.assertEqual(ket_qua["doc_am_dau"], "gờ")
        self.assertEqual(ket_qua["van"], "en")
        self.assertEqual(ket_qua["do_kho"], "kho")

    def test_toan_van_phuc(self):
        ket_qua = phan_tich_am_tiet("toán")
        self.assertEqual(ket_qua["am_dau"], "t")
        self.assertEqual(ket_qua["van"], "oan")
        self.assertEqual(ket_qua["am_dem"], "o")
        self.assertEqual(ket_qua["am_chinh"], "a")
        self.assertEqual(ket_qua["am_cuoi"], "n")
        self.assertEqual(ket_qua["do_kho"], "kho")
        self.assertEqual(
            ket_qua["nhom_bai_hoc"],
            ["am_dau_don", "van_co_am_dem", "van_co_am_cuoi", "van_phuc"],
        )

    def test_oanh_khong_am_dau(self):
        ket_qua = phan_tich_am_tiet("oanh")
        self.assertEqual(ket_qua["am_dau"], "")
        self.assertEqual(ket_qua["van"], "oanh")
        self.assertEqual(ket_qua["am_dem"], "o")
        self.assertEqual(ket_qua["am_chinh"], "a")
        self.assertEqual(ket_qua["am_cuoi"], "nh")
        self.assertEqual(
            ket_qua["nhom_bai_hoc"],
            ["khong_am_dau", "van_co_am_dem", "van_co_am_cuoi", "van_phuc"],
        )
        self.assertEqual(ket_qua["danh_van"], "oanh - ngang - oanh")

    def test_do_kho_cascade_de(self):
        ket_qua = phan_tich_am_tiet("ve")
        self.assertEqual(ket_qua["do_kho"], "de")

    def test_do_kho_cascade_trung_binh(self):
        ket_qua = phan_tich_am_tiet("bạn")
        self.assertEqual(ket_qua["do_kho"], "trung_binh")

    def test_tho(self):
        ket_qua = phan_tich_am_tiet("thỏ")
        self.assertEqual(ket_qua["am_dau"], "th")
        self.assertEqual(ket_qua["van"], "o")
        self.assertEqual(ket_qua["thanh"], "hỏi")
        self.assertEqual(ket_qua["danh_van"], "thờ - o - tho - hỏi - thỏ")

    def test_dat(self):
        ket_qua = phan_tich_am_tiet("đất")
        self.assertEqual(ket_qua["am_dau"], "đ")
        self.assertEqual(ket_qua["van"], "ât")
        self.assertEqual(ket_qua["am_chinh"], "â")
        self.assertEqual(ket_qua["am_cuoi"], "t")
        self.assertEqual(ket_qua["thanh"], "sắc")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "dat")

    def test_ngoai_le_oat(self):
        ket_qua = phan_tich_am_tiet("oắt")
        self.assertTrue(ket_qua["ngoai_le"])
        self.assertEqual(ket_qua["source"], "ngoai_le")
        self.assertEqual(ket_qua["do_kho"], "kho")
        self.assertEqual(ket_qua["am_tiet_khong_dau"], "oat")

    def test_ngoai_le_uynh(self):
        ket_qua = phan_tich_am_tiet("uỳnh")
        self.assertTrue(ket_qua["ngoai_le"])
        self.assertEqual(ket_qua["source"], "ngoai_le")
        self.assertEqual(ket_qua["danh_van"], "uynh - huyền - uỳnh")

    def test_uong_nguyen_am_doi(self):
        ket_qua = phan_tich_am_tiet("uống")
        self.assertEqual(ket_qua["van"], "uông")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "uô")
        self.assertEqual(ket_qua["am_cuoi"], "ng")

    def test_duoc_nguyen_am_doi(self):
        ket_qua = phan_tich_am_tiet("được")
        self.assertEqual(ket_qua["van"], "ươc")
        self.assertEqual(ket_qua["am_dem"], "")
        self.assertEqual(ket_qua["am_chinh"], "ươ")
        self.assertEqual(ket_qua["am_cuoi"], "c")


class TestDatasetOutput(unittest.TestCase):
    def test_tao_dataset(self):
        records = tao_dataset(["học", "bàn"])
        self.assertEqual(len(records), 2)
        self.assertEqual(records[0]["metadata"]["word"], "học")
        self.assertEqual(records[0]["linguistics"]["am_dau"], "h")
        self.assertEqual(records[0]["pedagogy"]["danh_van"], "hờ - oc - hoc - nặng - học")
        self.assertEqual(records[0]["metadata"]["source"], "sgk")

    def test_xuat_json(self):
        records = tao_dataset(["học", "quả"])
        with tempfile.TemporaryDirectory() as tmp_dir:
            output_path = Path(tmp_dir) / "output.jsonl"
            xuat_json(records, output_path)
            lines = output_path.read_text(encoding="utf-8").splitlines()

        self.assertEqual(len(lines), 2)
        dong_1 = json.loads(lines[0])
        dong_2 = json.loads(lines[1])
        self.assertEqual(dong_1["metadata"]["word"], "học")
        self.assertEqual(dong_2["linguistics"]["am_dau"], "qu")

    def test_record_structure(self):
        record = tao_dataset(["giá"])[0]
        self.assertEqual(set(record.keys()), {"linguistics", "pedagogy", "metadata"})
        self.assertEqual(record["metadata"]["source"], "sgk")
        self.assertEqual(record["linguistics"]["ngoai_le"], False)
        self.assertEqual(record["pedagogy"]["nhom_bai_hoc"], ["dac_biet_gi_qu"])

    def test_duplicate_words_have_unique_ids(self):
        records = tao_dataset(["đèn", "đèn"])
        ids = [record["metadata"]["id"] for record in records]
        self.assertEqual(len(ids), 2)
        self.assertEqual(len(set(ids)), 2)


class TestValidator(unittest.TestCase):
    def test_validate_records_reports_duplicate_ids(self):
        records = tao_dataset(["đèn", "đèn"])
        records[1]["metadata"]["id"] = records[0]["metadata"]["id"]
        errors = validate_records(records)
        self.assertTrue(any(error["field"] == "metadata.id" for error in errors))


if __name__ == "__main__":
    main()
