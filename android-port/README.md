# android-port

Scaffold Kotlin de copy vao Android project that.

## Cau truc

- `src/main/java/com/meticha/jetpackboilerplate/vietparser/AmTiet.kt`
- `src/main/java/com/meticha/jetpackboilerplate/vietparser/VietParser.kt`
- `src/main/java/com/meticha/jetpackboilerplate/vietparser/WordCache.kt`
- `src/main/java/com/meticha/jetpackboilerplate/vietparser/DoanVanProcessor.kt`
- `src/main/assets/sgk_lop1.jsonl`
- `src/test/java/com/meticha/jetpackboilerplate/vietparser/*.kt`

## Muc dich tung file

- `AmTiet.kt`: data class trung tam cho parser va cache
- `VietParser.kt`: port 1:1 logic parser tu Python
- `WordCache.kt`: load JSONL tu assets va tra record tu cache hoac fallback parser
- `DoanVanProcessor.kt`: tach tu trong doan van va map sang `AmTiet`

## Luu y khi copy vao Android project

- `WordCache.load(context)` dang dung `android.content.Context`, nen file nay la Android-only.
- Test trong scaffold nay la JUnit source-level; muon chay that can dat vao module Android/Kotlin co cau hinh test.
- `sgk_lop1.jsonl` can nam o `app/src/main/assets/sgk_lop1.jsonl` trong project that.
- Scaffold nay da sync theo config app hien tai:
  - `compileSdk = 36`
  - `minSdk = 31`
  - `Java/Kotlin target = 11`
- App dung Jetpack Compose. Parser khong phu thuoc Compose, nhung thong tin nay quan trong neu sau nay build UI karaoke/highlight tren Compose.

## 5 tu can doi chieu cuoi

- `bạn -> bờ - an - ban - nặng - bạn`
- `nghiêng -> ngờ - iêng - nghieng - ngang - nghiêng`
- `gi -> gờ - i - gi - ngang - gi`
- `quả -> quờ - a - qua - hỏi - quả`
- `oanh -> oanh - ngang - oanh`
