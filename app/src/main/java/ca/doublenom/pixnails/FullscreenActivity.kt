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
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import com.auth0.android.jwt.JWT
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
    private lateinit var refresh: ImageButton

    private lateinit var tbClientSwitch: ToggleButton

    private var tokenExpiration: Long? = null

    private var keepOriginal = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        httpClient = HTTPClient.getInstance(applicationContext)

        user = User(this, object : User.Callback {
            override fun onMoneyUpdated(shells: Int, silverShells: Int) {
                boosters.onShellsUpdated(shells, silverShells)
            }
        })
        promo = Promo(this)
        boosters = Boosters(this, object : Boosters.Callback {
            override fun onPurchase() {
//                    user.fetch()
            }

            override fun onDraw(cards: Array<Card>) {
                user.fetch()
                val new = user.findNewCards(cards)
                val draw = DraftDialog(new, cards)
                draw.show(supportFragmentManager, "draft")
            }
        })

    }

    private fun loadApp() {
        val fb = findViewById<FloatingActionButton>(R.id.debug_button)
        fb.setOnClickListener {
            val cards = ArrayList<Card>(200)
            for(set in Generations.getGenerationsName()) {
                for(i in 0 until Generations.getGenerationSize(set)) {
                    cards.add(Card(set, i, Puddyness.Normal))
                    cards.add(Card(set, i, Puddyness.Super))
                    cards.add(Card(set, i, Puddyness.Giga))
                }
            }
            val new = user.findNewCards(cards.toTypedArray())
            val draw = DraftDialog(new, cards.toTypedArray())
            draw.show(supportFragmentManager, "draft")
        }

        refresh = findViewById(R.id.refresh)
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
        refresh.setOnClickListener {
            keepOriginal = !tbClientSwitch.isChecked
            tbClientSwitch.isChecked = false
            Handler(Looper.getMainLooper()).postDelayed({
                webview.reload()
            }, 100)
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
                        val jwt = JWT(auth.split(" ")[1])
                        tokenExpiration = jwt.expiresAt!!.time
                        loadCustomClient()
                    }
                }
                return super.shouldInterceptRequest(request)
            }
        })
        // We need to change user agent to load desktop website in order to load
        // overlay extensions
        val newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
        webview.settings.userAgentString = newUA

        webview.webViewClient = CustomClient {
            if ((it == "https://www.twitch.tv" || it.contains("https://www.twitch.tv/?"))) {
                val sp = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
                val channel = sp.getInt(getString(R.string.last_channel), 0)
                Log.d("Channel", "$channel")
                sChannels.setSelection(channel)
                toolbar.visibility = View.VISIBLE
                loadCustomClient()
            }
        }
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(twitchLoginUrl)

        sChannels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                loadChannel(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Nothing
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(tokenExpiration == null || System.currentTimeMillis() >= tokenExpiration!!) {
            loadApp()
        }
    }

    fun loadChannel(num: Int) {
        getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit {
            this.putInt(getString(R.string.last_channel), num)
        }
        val channel = resources.getStringArray(R.array.channels_name)[num].lowercase()
        val type = resources.getIntArray(R.array.channels_type)[num]
        Log.d("Channel", channel)
        tbClientSwitch.isChecked = false
        if(type == 0) {
            webview.loadUrl("https://www.twitch.tv/popout/$channel/extensions/39l3u7h2njvvw0vijwldod0ks8wzpz/panel")
        } else {
            webview.loadUrl("https://www.twitch.tv/$channel")
        }
    }

    fun loadCustomClient() {
        Handler(Looper.getMainLooper()).post {
            if (webview.url == twitchLoginUrl || !Generations.isLoaded) return@post
            if(!keepOriginal) tbClientSwitch.isChecked = true
            keepOriginal = false
        }
    }

    class CustomClient(private val callback: (String) -> Unit) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url != null) callback(url)
        }
    }
}