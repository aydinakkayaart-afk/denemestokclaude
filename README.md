# Stok Takip (Android + SQLite)

Basit stok uygulaması. Veriler cihazda SQLite veritabanında (`stok.db`) tutulur, internet gerekmez.

## Özellikler
- Ürün ekle / düzenle / sil (satıra dokun = düzenle, uzun bas = sil)
- Satırdaki **−** / **+** ile hızlı stok azaltma/artırma (0'ın altına inmez)
- Ürün adına göre arama
- Kritik stok seviyesi: adet bu seviyeye inince sayı kırmızı olur
- Üstte toplam ürün, adet ve stok değeri özeti

## APK nasıl alınır

### Yol 1 — Bilgisayar gerekmez (GitHub Actions)
1. GitHub'da yeni bir repo oluştur, bu klasörün içindekileri yükle (`.github` klasörü dahil).
2. Repo > **Actions** sekmesi > "APK Derle" çalışır (yoksa **Run workflow**).
3. Bittiğinde çalışmanın altındaki **stok-takip-apk** dosyasını indir, zipten `app-debug.apk` çıkar.
4. Telefona at, kur ("bilinmeyen kaynaklardan yüklemeye izin ver" gerekebilir).

### Yol 2 — Android Studio
1. Klasörü Android Studio ile aç, Gradle senkronu bitsin.
2. **Build > Build APK(s)**. Dosya: `app/build/outputs/apk/debug/app-debug.apk`

Gereksinimler: JDK 17, Android SDK 34. Ek kütüphane yok.
