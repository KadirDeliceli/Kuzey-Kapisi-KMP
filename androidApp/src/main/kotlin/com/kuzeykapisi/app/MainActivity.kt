package com.kuzeykapisi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kuzeykapisi.app.data.media.ImagePickerHolder
import com.kuzeykapisi.app.platform.AndroidContextHolder

class MainActivity : ComponentActivity() {
    private val resimSecLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> ImagePickerHolder.sonucGeldi(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContextHolder.appContext = applicationContext
        ImagePickerHolder.launcherAyarla(resimSecLauncher)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}