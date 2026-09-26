package com.kuzeykapisi.app.data.remote

/**
 * Admin uçlarından dönen 4xx hataları. [detay], backend'in "detail" alanındaki
 * tek satırlık açıklamasıdır; yoksa ya da tek bir metin değilse null. İstisna
 * mesajı bilerek boş bırakılır: arayüze giden metni Metinler.adminHataMesaji seçer.
 */
class AdminApiHatasi(val httpKodu: Int, val detay: String?) : Exception("HTTP $httpKodu")
