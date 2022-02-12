package ca.doublenom.pixnails

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlin.coroutines.suspendCoroutine

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var sChannels: Spinner
    private lateinit var webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        sChannels = findViewById(R.id.spinner)
        webview = findViewById(R.id.webview)

        webview.webViewClient = CustomClient(sChannels)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://www.twitch.tv/login")

        sChannels.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val channel = resources.getStringArray(R.array.channels_name)[p2].lowercase()
                Log.d("Channel", "$channel")
                webview.loadUrl("https://www.twitch.tv/popout/$channel/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Nothing
            }
        }
    }

    class CustomClient(val spinner: Spinner) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("WVC", "url: $url")
            if (url != null && (url == "https://www.twitch.tv" || url.contains("?"))) {
                view?.loadUrl("https://www.twitch.tv/popout/doublenom/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
                spinner.visibility = View.VISIBLE
            }
        }
    }

}