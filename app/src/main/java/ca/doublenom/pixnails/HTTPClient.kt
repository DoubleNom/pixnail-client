package ca.doublenom.pixnails

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.Exception
import java.lang.NullPointerException

class HTTPClient constructor(context: Context) {
    private val url = "https://twitch-pixnails.web.app"
    var headers = HashMap<String, String>()

    companion object {
        @Volatile
        private var INSTANCE: HTTPClient? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HTTPClient(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun addToRequestQueue(endpoint: String, onSuccess: Response.Listener<JSONObject>, onError: Response.ErrorListener) {
        val authorization = headers["authorization"]!!

        val request = object: JsonObjectRequest(
            url + endpoint,
            onSuccess,
            onError
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = authorization
                return headers
            }
        }

        requestQueue.add(request)
    }

}
