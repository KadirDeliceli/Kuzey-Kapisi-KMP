package com.kuzeykapisi.app.data.model

data class Konum(val enlem: Double, val boylam: Double)

/** Sinop merkez — konum alınamadığında (izin reddi, hata, zaman aşımı) düşülen nokta. */
val VARSAYILAN_KONUM = Konum(42.026334999089315, 35.145131076654025)

/**
 * Kullanıcının güncel konumunu almayı dener; izin yoksa, hata olursa ya da
 * ~5 saniyede sonuç gelmezse [VARSAYILAN_KONUM] döner — asla hata fırlatmaz.
 */
expect suspend fun guncelKonumAl(): Konum
