package ca.doublenom.pixnails

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var sChannels: Spinner
    private lateinit var webview: WebView
    private lateinit var client: ViewGroup
    private lateinit var httpClient: HTTPClient
    private lateinit var shells: Shells
    private lateinit var promo: Promo
    private lateinit var toolbar: ConstraintLayout

    private lateinit var tbClientSwitch: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        httpClient = HTTPClient.getInstance(applicationContext)

        toolbar = findViewById(R.id.toolbar)
        sChannels = findViewById(R.id.spinner)
        webview = findViewById(R.id.webview)
        client = findViewById(R.id.include_client)
        tbClientSwitch = findViewById(R.id.client_switch)
        tbClientSwitch.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                webview.visibility = View.GONE
                client.visibility = View.VISIBLE
                shells.refresh()
                promo.refresh()
            } else {
                webview.visibility = View.VISIBLE
                client.visibility = View.GONE
            }
        }

        shells = Shells(this)
        promo = Promo(this)

        val swc = ServiceWorkerController.getInstance()
        swc.setServiceWorkerClient(object : ServiceWorkerClient() {
            override fun shouldInterceptRequest(request: WebResourceRequest?): WebResourceResponse? {
                if (request != null && request.url.path == "/generations") {
                    val auth = request.requestHeaders["authorization"]
                    if (auth != null) {
                        httpClient.headers["authorization"] = auth
                        Handler(Looper.getMainLooper()).post {
                            tbClientSwitch.isChecked = true
                        }
                    }
                }
                return super.shouldInterceptRequest(request)
            }
        })

        webview.webViewClient = CustomClient(toolbar)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://www.twitch.tv/login")

        sChannels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val channel = resources.getStringArray(R.array.channels_name)[p2].lowercase()
                Log.d("Channel", "$channel")
                tbClientSwitch.isChecked = false
                webview.loadUrl("https://www.twitch.tv/popout/$channel/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Nothing
            }
        }


    }

    class CustomClient(val toolbar: ConstraintLayout) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("WVC", "url: $url")
            if (url != null && (url == "https://www.twitch.tv" || url.contains("?"))) {
                view?.loadUrl("https://www.twitch.tv/popout/doublenom/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
                toolbar.visibility = View.VISIBLE
            }
        }
    }
}