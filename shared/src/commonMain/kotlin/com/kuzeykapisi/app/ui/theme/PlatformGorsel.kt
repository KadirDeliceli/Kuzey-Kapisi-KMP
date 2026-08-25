package com.kuzeykapisi.app.ui.theme

/**
 * Fener ışığı (glow) mikro-etkileşimi YALNIZCA web'de uygulanır. Mobilde
 * (Android/iOS) yerine hafif bir basma geri bildirimi kullanılır.
 */
expect val fenerHalesiDestekli: Boolean

/**
 * Kullanıcı "hareketi azalt" tercihini açtıysa true — geçiş animasyonlarının
 * süresi kısaltılır/atlanır. Tespit edilemeyen platformlarda false döner.
 */
expect val hareketAzaltilsin: Boolean
