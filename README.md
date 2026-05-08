# DatasetVietnamese

Bo cong cu tao dataset danh van tieng Viet chuan SGK cho app/game day doc tre em.

Muc tieu cua repo nay la:
- phan tich am tiet tieng Viet thanh cac truong ngon ngu hoc co cau truc
- sinh chuoi danh van de tre em co the nghe/doc theo kieu SGK
- xuat dataset dang JSONL de dung cho app, game, TTS, hoac pipeline AI sau nay

## Repo nay co gi

- [generate_dataset.py](/home/quocanh/DatasetVietnamese/generate_dataset.py:1): script chinh
  - `phan_tich_am_tiet(tu)`: phan tich 1 tu
  - `tao_dataset(danh_sach_tu)`: sinh list records
  - `xuat_json(records, output_path)`: ghi `.jsonl`
  - co kem `unittest`
- [validate_dataset.py](/home/quocanh/DatasetVietnamese/validate_dataset.py:1): validator cho file JSONL da sinh
- [schema.json](/home/quocanh/DatasetVietnamese/schema.json:1): JSON Schema cua moi record
- [sample_output.jsonl](/home/quocanh/DatasetVietnamese/sample_output.jsonl:1): 20 record mau
- [sgk_lop1.jsonl](/home/quocanh/DatasetVietnamese/sgk_lop1.jsonl:1): dataset da sinh tu `danh_sach_tu.txt`
- [danh_sach_tu.txt](/home/quocanh/DatasetVietnamese/danh_sach_tu.txt:1): danh sach tu dau vao hien tai
- [review_output.jsonl](/home/quocanh/DatasetVietnamese/review_output.jsonl:1): file review trung gian

## Quy tac danh van dang dung

Cau truc co ban:

`[am_dau] + [van] + [thanh] -> [tu hoan chinh]`

Template `danh_van`:
- Neu co am dau: `doc_am_dau - van - am_tiet_khong_dau - thanh - tu`
- Neu khong co am dau: `van - thanh - tu`

Vi du:
- `hoc` -> `hờ - oc - hoc - nặng - học`
- `ban` -> `bờ - an - ban - huyền - bàn`
- `em` -> `em - ngang - em`
- `qua` -> `quờ - a - qua - hỏi - quả`
- `cat` -> `cờ - ăt - cat - sắc - cắt`

Luu y quan trong:
- `am_tiet_khong_dau` la dang ASCII hoan toan
  - `quả -> qua`
  - `nghiêng -> nghieng`
  - `đất -> dat`
- `gi` va `qu` co quy uoc rieng theo SGK
  - `giá -> dờ - a - gia - sắc - giá`
  - `quả -> quờ - a - qua - hỏi - quả`
- truong hop tu `gi` dung mot minh duoc xu ly theo huong SGK:
  - `am_dau = "g"`
  - `van = "i"`
  - `danh_van = "gờ - i - gi - ngang - gi"`

## Cau truc record

Moi dong trong dataset la 1 JSON object gom 3 nhom:

- `linguistics`
  - `am_dau`, `doc_am_dau`, `van`
  - `am_dem`, `am_chinh`, `am_cuoi`
  - `thanh`
  - `am_tiet_khong_dau`
  - `ngoai_le`
- `pedagogy`
  - `danh_van`
  - `do_kho`
  - `nhom_bai_hoc`
- `metadata`
  - `id`
  - `word`
  - `version`
  - `source`

Schema day du nam o [schema.json](/home/quocanh/DatasetVietnamese/schema.json:1).

## Cach chay

Yeu cau:
- `python3`
- khong can cai them thu vien ngoai

### 1. Chay test

```bash
python3 -m unittest generate_dataset -v
```

### 2. Sinh file mau mac dinh

Lenh nay se sinh lai:
- `sample_output.jsonl`
- `schema.json`

```bash
python3 generate_dataset.py
```

### 3. Sinh dataset tu file danh sach tu

Neu co file `danh_sach_tu.txt`, chay:

```bash
python3 - <<'PY'
from generate_dataset import tao_dataset, xuat_json

with open("danh_sach_tu.txt", encoding="utf-8") as f:
    tu_list = [line.strip() for line in f if line.strip()]

records = tao_dataset(tu_list)
xuat_json(records, "sgk_lop1.jsonl")
print(len(records))
PY
```

### 4. Validate file da sinh

```bash
python3 validate_dataset.py sgk_lop1.jsonl
```

Validator hien tai kiem tra:
- `metadata.id` khong bi trung
- `am_tiet_khong_dau` dung dang ASCII va khop voi tu goc bo dau
- `van == am_dem + am_chinh + am_cuoi`
- `danh_van` khop template

Ngoai ra validator con in `tracking_notes` cho mot so nhom can theo doi khi scale lon, hien tai la cac truong hop `uô / ươ / iê`.

## Tinh trang hien tai

- `sample_output.jsonl`: 20 record mau
- `sgk_lop1.jsonl`: 671 record tu `danh_sach_tu.txt`
- validator dang bao:
  - `errors=0`
  - co mot so `tracking_notes` cho nhom `uô / ươ / iê`

## Gioi han hien tai

- Parser dang la rule-based, khong dung tu dien hay thu vien ngu am ben ngoai.
- Co bang `EXCEPTION_WORDS` nho cho cac truong hop dac biet.
- Mot so nhom van doi nhu `uô`, `ươ`, `iê` dang duoc danh dau de theo doi them theo quy uoc SGK khi mo rong len 100k+ tu.

## Tai lieu thiet ke

- [Design spec](/home/quocanh/DatasetVietnamese/docs/superpowers/specs/2026-05-08-vietnamese-spelling-dataset-design.md:1)
- [Implementation plan](/home/quocanh/DatasetVietnamese/docs/superpowers/plans/2026-05-08-vietnamese-spelling-dataset.md:1)
