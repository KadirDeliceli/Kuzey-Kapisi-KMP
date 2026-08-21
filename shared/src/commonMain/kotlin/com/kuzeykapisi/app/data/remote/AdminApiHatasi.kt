package com.kuzeykapisi.app.data.remote

/** Admin uçlarından dönen 4xx hatalarının backend'in "detail" mesajıyla sarmalanmış hali. */
class AdminApiHatasi(val httpKodu: Int, val detay: String) : Exception(detay)
