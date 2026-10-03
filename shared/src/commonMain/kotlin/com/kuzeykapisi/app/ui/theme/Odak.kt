package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/**
 * Klavye odağı göstergesi: şeklin 3dp dışında 2dp [TasBeyazi] halka (koyu
 * zeminde 11:1 ve üstü). Yalnızca çizimdir, yerleşimi değiştirmez, bu yüzden
 * odak gelince içerik kaymaz. Halka şeklin dışına taştığı için `clip`'ten ÖNCE
 * uygulanmalıdır. Açık zeminde (ör. sözleşme ekranı) [renk] koyu bir tonla
 * değiştirilir.
 */
fun Modifier.klavyeOdakHalkasi(odakli: Boolean, sekil: Shape, renk: Color = TasBeyazi): Modifier = drawWithContent {
    drawContent()
    if (odakli) {
        val kalinlik = 2.dp.toPx()
        val pay = 3.dp.toPx() + kalinlik / 2f
        val halka = sekil.createOutline(
            Size(size.width + pay * 2f, size.height + pay * 2f),
            layoutDirection,
            this,
        )
        translate(left = -pay, top = -pay) {
            drawOutline(outline = halka, color = renk, style = Stroke(width = kalinlik))
        }
    }
}
