package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val twitchLoginUrl = "https://www.twitch.tv/login"

    private lateinit var sChannels: Spinner
    private lateinit var webview: WebView
    private lateinit var client: ViewGroup
    private lateinit var httpClient: HTTPClient
    private lateinit var user: User
    private lateinit var promo: Promo
    private lateinit var boosters: Boosters
    private lateinit var toolbar: ConstraintLayout

    private lateinit var tbClientSwitch: ToggleButton

    @SuppressLint("SetJavaScriptEnabled")
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
                user.fetch()
                promo.refresh()
            } else {
                webview.visibility = View.VISIBLE
                client.visibility = View.GONE
            }
        }

        Generations.requestGenerations(this, object : Generations.Callback {
            override fun onGenerationLoaded() {
                loadCustomClient()
            }
        })

        OneTimeScheduleWorker.createNotificationChannel(this)

        val swc = ServiceWorkerController.getInstance()
        swc.setServiceWorkerClient(object : ServiceWorkerClient() {
            override fun shouldInterceptRequest(request: WebResourceRequest?): WebResourceResponse? {
                if (request != null && request.url.path == "/news") {
                    val auth = request.requestHeaders["authorization"]
                    if (auth != null) {
                        httpClient.headers["authorization"] = auth
                        loadCustomClient()
                    }
                }
                return super.shouldInterceptRequest(request)
            }
        })

        webview.webViewClient = CustomClient {
            if ((it == "https://www.twitch.tv" || it.contains("?"))) {
                webview.loadUrl("https://www.twitch.tv/popout/doublenom/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
                toolbar.visibility = View.VISIBLE
                loadCustomClient()
            }
        }
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(twitchLoginUrl)

        sChannels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val channel = resources.getStringArray(R.array.channels_name)[p2].lowercase()
                Log.d("Channel", channel)
                tbClientSwitch.isChecked = false
                webview.loadUrl("https://www.twitch.tv/popout/$channel/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz-0.0.1/panel")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Nothing
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (webview.url!!.contains("popout")) finish()
    }

    fun loadCustomClient() {
        Handler(Looper.getMainLooper()).post {
            if (webview.url == twitchLoginUrl || !Generations.isLoaded) return@post
            user = User(this, object : User.Callback {
                override fun onMoneyUpdated(shells: Int, silverShells: Int) {
                    boosters.onShellsUpdated(shells, silverShells)
                }
            })
            promo = Promo(this)
            boosters = Boosters(this, object : Boosters.Callback {
                override fun onPurchase() {
                    user.fetch()
                }

                override fun onDraw(cards: Array<Card>) {
                    val new = user.findNewCards(cards)
                    val draw = DraftDialog(new, cards)
                    draw.show(supportFragmentManager, "draft")
                }
            })
            tbClientSwitch.isChecked = true
        }
    }

    class CustomClient(private val callback: (String) -> Unit) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url != null) callback(url)
        }
    }
}