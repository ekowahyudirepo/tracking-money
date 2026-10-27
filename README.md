# KeuanganKu

Aplikasi Android sederhana untuk mencatat pemasukan dan pengeluaran. Data disimpan offline di HP (Room/SQLite).

## Fitur
- Tambah transaksi pemasukan atau pengeluaran (nominal, kategori, catatan)
- Ringkasan saldo, pemasukan, dan pengeluaran bulan berjalan
- Riwayat transaksi dan hapus transaksi
- Format Rupiah, tema terang/gelap mengikuti sistem

## Teknologi
Kotlin, Jetpack Compose (Material 3), Room, minSdk 24.

## Cara mendapatkan APK
1. Push proyek ini ke branch `main` di GitHub.
2. Buka tab **Actions**, pilih workflow **Build APK**, tunggu sampai selesai.
3. Unduh artifact `KeuanganKu-debug-apk`, ekstrak, lalu pasang `app-debug.apk` di HP
   (izinkan "Install dari sumber tidak dikenal").

Alternatif: buka folder ini di Android Studio, lalu Build > Build APK(s).
