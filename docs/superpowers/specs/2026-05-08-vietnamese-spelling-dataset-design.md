# Vietnamese Spelling Dataset Design

Date: 2026-05-08
Topic: Dataset danh van tieng Viet chuan SGK

## Muc tieu

Tao bo cong cu sinh dataset danh van tieng Viet cho app/game day doc tre em, huong toi quy mo 100.000+ tu, voi logic phan tich am tiet nhat quan, de kiem tra, va co the mo rong cho bai toan AI/TTS sau nay.

## Pham vi cua dot nay

Dot nay tao 3 tep:

- `generate_dataset.py`
- `schema.json`
- `sample_output.jsonl`

Script can:

- Phan tich mot tu thanh cac truong ngon ngu hoc va su pham
- Sinh JSON records tu danh sach tu
- Xuat JSONL UTF-8
- Chay bo `unittest` voi tap tu mau da dang

## Nguyen tac du lieu

### Chon mo hinh phan tich

Du lieu luu dong thoi:

- `van` theo dang hien thi de tre em hoc van
- `am_dem`, `am_chinh`, `am_cuoi` theo muc tieu phan tich sau hon

Vi du:

```json
{
  "am_dau": "qu",
  "doc_am_dau": "quờ",
  "van": "a",
  "am_dem": "",
  "am_chinh": "a",
  "am_cuoi": "",
  "thanh": "hỏi"
}
```

Luu y: trong implementation, `doc_am_dau` luon dung gia tri doc chuan SGK co dau tieng Viet.

### Giu chinh ta goc

- `am_dau` luu theo chu viet goc, khong chuyen ve phu am tuong duong.
- `doc_am_dau` luu cach doc SGK de sinh chuoi danh van.
- `gi` van luu la `gi`, doc thanh `dờ`.
- `qu` van luu la `qu`, doc thanh `quờ`.
- `gh`, `ngh` duoc giu nguyen o `am_dau`.

### Dinh nghia `am_tiet_khong_dau`

`am_tiet_khong_dau` la toan bo am tiet sau khi bo dau thanh, nhung giu nguyen chinh ta goc cua am tiet. Khong tach rieng am dau, khong chuyen doi sang bieu dien am vi.

Vi du:

- `ban` -> `ban`
- `bạn` -> `ban`
- `quả` -> `qua`
- `nghiêng` -> `nghieng`

## Quy tac danh van

### Cong thuc chot

Neu co am dau:

`doc_am_dau + " - " + van + " - " + am_tiet_khong_dau + " - " + thanh + " - " + tu_day_du`

Neu khong co am dau:

`van + " - " + thanh + " - " + tu_day_du`

### Vi du

- `bạn` -> `bờ - an - ban - nặng - bạn`
- `oanh` -> `oanh - ngang - oanh`
- `nghiêng` -> `ngờ - iêng - nghieng - ngang - nghiêng`

## Thiet ke logic tach am tiet

### Luong xu ly

1. Chuan hoa Unicode NFC va chuyen ve chu thuong.
2. Kiem tra bang ngoai le neu tu nam trong danh sach override.
3. Xac dinh thanh bang cach bo dau thanh khoi nguyen am.
4. Xac dinh `am_dau` theo nguyen tac uu tien chuoi dai nhat:
   - uu tien cac cum `ngh`, `gh`, `ch`, `kh`, `ng`, `nh`, `ph`, `qu`, `th`, `tr`, `gi`
   - sau do moi den am dau 1 ky tu
5. Tach phan con lai thanh `van`.
6. Phan tich `van` thanh `am_dem`, `am_chinh`, `am_cuoi`.
7. Tao `am_tiet_khong_dau`.
8. Sinh `danh_van`, `do_kho`, `nhom_bai_hoc`, va metadata.

### Edge cases can ho tro

- Tu khong co am dau: `em`, `oanh`
- Am dem: `hoa`, `toan`, `oanh`
- `gh`, `ngh`: `ghen`, `nghiêng`
- `gi`: `giá`, `giỏi`
- `qu`: `quả`, `quang`

## Schema record

Moi record co 3 nhom chinh:

### `linguistics`

- `am_dau`: string
- `doc_am_dau`: string
- `van`: string
- `am_dem`: string
- `am_chinh`: string
- `am_cuoi`: string
- `thanh`: enum `ngang | huyền | sắc | hỏi | ngã | nặng`
- `am_tiet_khong_dau`: string
- `ngoai_le`: boolean

### `pedagogy`

- `danh_van`: string
- `do_kho`: enum `de | trung_binh | kho`
- `nhom_bai_hoc`: list string

Gia tri hop le cho `nhom_bai_hoc`:

- `khong_am_dau`
- `am_dau_don`
- `am_dau_ghep`
- `dac_biet_gi_qu`
- `van_co_am_dem`
- `van_co_am_cuoi`
- `van_phuc`

### `metadata`

- `id`: string
- `word`: string
- `version`: string
- `source`: enum `sgk | ngoai_le | vay_muon`

## Suy dien `do_kho`

Ap dung theo rule cascade, dung thu tu sau:

1. `ngoai_le == true` -> `kho`
2. `am_dau` thuoc `{gi, qu, gh, ngh}` hoac `van_phuc == true` -> `kho`
3. `thanh` thuoc `{sắc, hỏi, ngã, nặng}` hoac co `am_cuoi` hoac co `am_dem` -> `trung_binh`
4. Nguoc lai -> `de`

Khong cong diem, khong OR song song xep hang. Ket qua phai deterministic theo thu tu tren.

## Suy dien `nhom_bai_hoc`

`nhom_bai_hoc` la danh sach nhan, khong phai scalar. Gan tat ca nhan phu hop:

- `khong_am_dau` khi `am_dau == ""`
- `am_dau_don` khi `am_dau` la am dau 1 ky tu
- `am_dau_ghep` khi `am_dau` thuoc `{ch, gh, kh, ng, ngh, nh, ph, th, tr}`
- `dac_biet_gi_qu` khi `am_dau` thuoc `{gi, qu}`
- `van_co_am_dem` khi `am_dem != ""`
- `van_co_am_cuoi` khi `am_cuoi != ""`
- `van_phuc` khi `am_dem != ""` va `am_cuoi != ""`

## Co che ngoai le

Implementation se co bang ngoai le nho trong script de override ket qua phan tich cho mot so tu hiem hoac tu vay muon. Khi co override:

- `linguistics.ngoai_le = true`
- `metadata.source = "ngoai_le"` neu tu do duoc cap bang override noi bo

Neu khong trung override:

- `linguistics.ngoai_le = false`
- `metadata.source = "sgk"` mac dinh, tru khi dau vao sau nay quy dinh rieng `vay_muon`

## Kiem thu

Script se co bo `unittest` toi thieu 20 ca, bao gom:

- Nhom khong am dau
- Nhom am dem
- Nhom `gh` / `ngh`
- Nhom `gi` / `qu`
- Nhom du 6 thanh
- Nhom ngoai le, it nhat 2 test
- Nhom kiem tra string `danh_van` chinh xac, it nhat 3 test

## Dau ra mau

`sample_output.jsonl` se duoc sinh tu chinh `generate_dataset.py` de dam bao dong nhat giua logic va du lieu mau.
