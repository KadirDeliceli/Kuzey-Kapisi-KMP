package com.kuzeykapisi.app

import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.remote.HataTuru
import com.kuzeykapisi.app.data.remote.hataTuru

/**
 * Kullanıcıya gösterilen TÜM hata ve durum mesajlarının tek kaynağı.
 *
 * Kurallar:
 * - Hitap her yerde "siz"dir ("deneyin", "seçebilirsiniz"; asla "dene", "seçebilirsin").
 * - Ne olduğu, sonra ne yapılacağı söylenir; özür yok, teknik ayrıntı yok.
 * - İstisna metni (e.message) ve backend'in ham yanıtı buraya ASLA karışmaz:
 *   hata, türüne göre ([HataTuru]) sabit bir metne çevrilir.
 * - Buton metinleri cümle düzenindedir: yalnızca ilk harf büyük ("Tekrar dene",
 *   "Ayarları aç"); özel adlar kendi yazımını korur ("Google Maps'te aç").
 */
object Metinler {

    // --- Türe göre genel hata mesajları --------------------------------------

    const val HATA_AG = "Sunucuya ulaşılamıyor. İnternet bağlantınızı kontrol edip tekrar deneyin."
    const val HATA_YETKISIZ = "Bu işlem için yetkiniz yok. Lütfen yeniden giriş yapın."
    const val HATA_BULUNAMADI = "Aradığınız içerik bulunamadı. Kaldırılmış olabilir."
    const val HATA_DOGRULAMA = "Gönderilen bilgiler kabul edilmedi. Alanları kontrol edip tekrar deneyin."
    const val HATA_SUNUCU = "Sunucuda geçici bir sorun var. Birkaç dakika sonra tekrar deneyin."
    const val HATA_BILINMEYEN = "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."

    fun hataMesaji(tur: HataTuru): String = when (tur) {
        HataTuru.AG -> HATA_AG
        HataTuru.YETKISIZ -> HATA_YETKISIZ
        HataTuru.BULUNAMADI -> HATA_BULUNAMADI
        HataTuru.DOGRULAMA -> HATA_DOGRULAMA
        HataTuru.SUNUCU -> HATA_SUNUCU
        HataTuru.BILINMEYEN -> HATA_BILINMEYEN
    }

    fun hataMesaji(e: Throwable): String = hataMesaji(e.hataTuru())

    /**
     * Admin uçları: doğrulama hatalarında (400/409...) backend'in kendi Türkçe
     * açıklaması ("Bu kod zaten var." gibi) alanı düzeltmek için gereklidir,
     * o gösterilir. Diğer her durumda türe göre sabit metin.
     */
    fun adminHataMesaji(e: AdminApiHatasi): String {
        val detay = e.detay
        return if (e.hataTuru() == HataTuru.DOGRULAMA && detay != null) detay else hataMesaji(e)
    }

    // --- Yönetici girişi ------------------------------------------------------

    const val GIRIS_BILGI_EKSIK = "Kullanıcı adı ve şifre gerekli."
    const val GIRIS_BILGI_HATALI = "Kullanıcı adı veya şifre hatalı."
    const val GIRIS_SUNUCUYA_ULASILAMIYOR = "Sunucuya ulaşılamıyor, tekrar deneyin."

    // --- Yönetici oturumu -----------------------------------------------------

    const val ADMIN_OTURUM_NOTU = "Oturumunuz 15 dakika işlem yapılmazsa kendiliğinden kapanır."
    const val ADMIN_OTURUM_SURESI_DOLDU =
        "15 dakika işlem yapılmadığı için oturumunuz kapatıldı. Devam etmek için yeniden giriş yapın."
    const val ADMIN_OTURUM_GECERSIZ = "Oturumunuz sona erdi. Devam etmek için yeniden giriş yapın."

    /** YALNIZCA gerçek 401 "şifre hatalı" demektir; ağ/sunucu hatası kimlik bilgisi hatası gibi gösterilmez. */
    fun girisHataMesaji(e: Throwable): String = when {
        e is AdminApiHatasi && e.httpKodu == 401 -> GIRIS_BILGI_HATALI
        e.hataTuru() == HataTuru.AG -> GIRIS_SUNUCUYA_ULASILAMIYOR
        else -> hataMesaji(e)
    }

    // --- Sohbet ---------------------------------------------------------------

    const val SOHBET_YENILENDI = "Bağlantı yenilendi — sohbet geçmişi sıfırlandı."
    const val SOHBET_SES_ANLASILAMADI = "Sizi anlayamadım, lütfen tekrar deneyin."

    // --- Ses kaydı (mikrofon) -------------------------------------------------

    const val MIKROFON_IZNI_YOK = "Mikrofon izni verilmedi."
    const val MIKROFON_IZNI_KAPALI = "Mikrofon izni kapalı. Ayarlardan açabilirsiniz."
    const val MIKROFON_TARAYICIDA_ENGELLI =
        "Mikrofon erişimi engellenmiş. Tarayıcının adres çubuğundaki kilit/site bilgisi " +
            "simgesine tıklayıp mikrofon iznini 'İzin Ver' yapın, sonra sayfayı yenileyin."
    const val SES_KAYDI_BASLATILAMADI = "Ses kaydı başlatılamadı."
    const val SES_KAYDEDILEMEDI = "Ses kaydedilemedi."
    const val SES_KAYDI_DESTEKLENMIYOR = "Bu platformda ses kaydı desteklenmiyor."

    // --- Seslendirme (metin okuma) --------------------------------------------

    const val SESLENDIRME_TARAYICIDA_YOK =
        "Bu tarayıcı sesli okumayı desteklemiyor. Metni okuyarak devam edebilirsiniz."
    const val SESLENDIRME_GECICI_HATA = "Sesli okuma şu an başlatılamadı. Lütfen tekrar deneyin."
    const val SESLENDIRME_PLATFORMDA_YOK = "Bu platformda seslendirme desteklenmiyor."
    const val SESLENDIRME_TURKCE_YOK = "Bu cihazda Türkçe seslendirme desteklenmiyor."
    const val SES_MOTORU_BASLATILAMADI = "Ses motoru başlatılamadı."
    const val ANLATIM_OKUNAMADI = "Anlatım okunamadı."

    // --- Anlatım --------------------------------------------------------------

    const val ANLATIM_YUKLENEMEDI = "Anlatım yüklenemedi."
    const val ANLATIM_YOK = "Bu içerik için sesli anlatım bulunmuyor."
    const val ANLATIM_YOK_DURAK = "Bu durak için sesli anlatım bulunmuyor."
    const val ANLATIM_YOK_ACIKLAMA = "Doğrudan sohbet ekranına geçebilirsiniz."

    // --- Katalog / içerik listesi ---------------------------------------------

    const val ICERIK_YUKLENEMEDI_BASLIK = "İçerik yüklenemedi."
    const val BASLIKTA_ICERIK_YOK = "Bu başlıkta henüz içerik yok."
    const val BASLIKTA_ICERIK_YOK_ACIKLAMA = "Başka bir başlık seçmek için geri dönün."

    // --- Akıllı rota ----------------------------------------------------------

    const val ROTA_KATEGORILER_YUKLENEMEDI = "Kategoriler yüklenemedi, lütfen tekrar deneyin."
    const val ROTA_KATEGORILER_YUKLENIYOR = "Kategoriler yükleniyor…"
    const val ROTALAR_YUKLENEMEDI = "Rotalar yüklenemedi, lütfen tekrar deneyin."
    const val ROTALAR_HAZIRLANIYOR = "Rotalar hazırlanıyor…"
    const val ROTA_ONERILENLER_YUKLENEMIYOR = "Önerilen turlar şu an yüklenemiyor."
    const val ROTA_HARITASI_YUKLENEMIYOR = "Rota haritası şu an yüklenemiyor."
    const val ROTA_DURAK_BULUNAMADI =
        "Bu tercihlere uyan bir durak bulamadık. Süreyi ya da ilgi alanlarını değiştirip tekrar deneyin."

    fun rotaEnFazlaTur(sinir: Int) = "En fazla $sinir kategori seçebilirsiniz."

    // --- Yönetim paneli -------------------------------------------------------

    const val LISTE_YUKLENEMEDI = "Liste yüklenemedi, lütfen tekrar deneyin."
    const val ICERIK_YUKLENEMEDI = "İçerik yüklenemedi, lütfen tekrar deneyin."
    const val KATEGORIDE_ICERIK_YOK = "Bu kategoride henüz içerik yok."
    const val HENUZ_MEKAN_YOK = "Henüz mekan eklenmedi."
    const val GUNCELLENDI = "Güncellendi."

    fun eklendi(ad: String) = "Eklendi: $ad"
    fun kodZatenVar(detay: String) = "$detay Lütfen yukarıdaki 'Kod' alanını değiştirip tekrar deneyin."

    const val FORM_PERSONA_ZORUNLU_ALANLAR = "'Ad', 'Açılış Mesajı' ve 'Detaylı İçerik' alanları boş olamaz."
    const val FORM_GORSEL_GEREKLI = "Lütfen bir görsel seçin."
    const val FORM_MEKAN_ZORUNLU_ALANLAR = "'Ad' ve 'Açıklama' alanları boş olamaz."
    const val FORM_KOORDINAT_GECERSIZ = "'Enlem' ve 'Boylam' geçerli birer sayı olmalı."
    const val FORM_SURE_GECERSIZ = "'Ziyaret Süresi' sıfırdan büyük bir tam sayı olmalı."
}
