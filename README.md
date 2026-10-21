# Elektronik Sensus Pelepah

Aplikasi Android offline untuk pemeriksaan kategori tunas pelepah dan sanitasi, usulan tunasan, data pemeriksaan, dokumentasi kamera, dan ekspor PDF.

## Build APK otomatis melalui GitHub Actions

1. Buat repository baru di GitHub, misalnya `elektronik-sensus-pelepah`.
2. Unggah seluruh isi folder proyek ini ke root repository. Folder `.github/workflows` harus ikut terunggah.
3. Buka tab **Actions** di repository.
4. Pilih workflow **Build APK Android**.
5. Tekan **Run workflow**, pilih branch `main`, lalu tekan **Run workflow**.
6. Tunggu proses build selesai, biasanya sekitar 3 sampai 10 menit pada build pertama.
7. Buka hasil workflow yang berstatus hijau.
8. Pada bagian **Artifacts**, unduh `Elektronik-Sensus-Pelepah-APK`.
9. Ekstrak berkas ZIP artifact. Di dalamnya terdapat `Elektronik-Sensus-Pelepah.apk`.

Workflow juga berjalan otomatis setiap ada perubahan yang dikirim ke branch `main`.

## Struktur penting

- `.github/workflows/build-apk.yml`: otomasi pembuatan APK di GitHub.
- `app/src/main/assets/index.html`: aplikasi HTML offline.
- `app/src/main/java/id/co/elektroniksensus/pelepah/MainActivity.java`: WebView, kamera, dan penyimpanan PDF.
- `app/build.gradle`: konfigurasi Android.

## Instalasi APK di HP

1. Salin `Elektronik-Sensus-Pelepah.apk` ke HP Android.
2. Buka file APK.
3. Jika diminta, izinkan **Pasang aplikasi tidak dikenal** untuk aplikasi pembuka file.
4. Tekan **Instal**.
5. Saat aplikasi pertama dibuka, berikan izin kamera.

## Catatan rilis

APK dari workflow ini adalah **debug APK**, cocok untuk pengujian dan pemakaian internal. Untuk publikasi resmi atau Google Play, buat keystore dan konfigurasi release signing. Jangan menyimpan kata sandi atau file keystore langsung di repository publik.
