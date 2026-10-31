# KeuanganKu

Aplikasi Android untuk mencatat pemasukan dan pengeluaran. Data tersimpan offline di HP (Room/SQLite).

## Fitur (v1.1)
- Navigasi bawah: Beranda, Statistik, Pengaturan
- Tambah, ubah, dan hapus transaksi (nominal dengan pemisah ribuan, kategori beremoji, tanggal, catatan)
- Pindah antar bulan, pencarian, dan filter pemasukan/pengeluaran
- Riwayat dikelompokkan per hari beserta total harian
- Ringkasan saldo, pemasukan, pengeluaran, dan anggaran bulanan dengan progress bar
- Statistik: ringkasan bulan, grafik 6 bulan terakhir, pengeluaran per kategori
- Ekspor semua data ke CSV dan bagikan ringkasan bulanan
- Tema terang/gelap/ikut sistem, warna dinamis (Android 12+)

## Versi web (PWA)
Ada juga versi web yang bisa dipasang di iPhone dan Android: [tracking-money-pwa](https://github.com/ekowahyudirepo/tracking-money-pwa).
Versi web punya fitur tambahan yang **belum ada di aplikasi Android ini**:
- Login dan sinkronisasi antar perangkat (Supabase)
- Batas pengeluaran harian

Data bisa dipindahkan antar kedua versi lewat ekspor/impor CSV. Tangkapan layar versi web ada di README repo tersebut.

## Teknologi
Kotlin, Jetpack Compose (Material 3), Room, minSdk 24.

## Cara mendapatkan APK
1. Push perubahan ke branch `main`.
2. Buka tab **Actions**, pilih run **Build APK** terbaru, tunggu sampai hijau.
3. Unduh artifact `KeuanganKu-debug-apk`, ekstrak, lalu pasang `app-debug.apk` di HP.

## Catatan penandatanganan
`app/keuanganku-debug.jks` adalah keystore debug tetap (password `android`) supaya APK baru bisa
menimpa versi lama tanpa uninstall. Jangan dipakai untuk rilis Play Store.
