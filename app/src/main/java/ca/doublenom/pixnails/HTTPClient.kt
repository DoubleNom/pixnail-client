package ca.doublenom.pixnails

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.*
import org.json.JSONArray
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

    fun addToRequestQueueArray(
        endpoint: String,
        onSuccess: Response.Listener<JSONArray>,
        onError: Response.ErrorListener,
        token: Boolean = true
    ) {
        if (headers.isEmpty() && token) return
        val authorization = if(token) headers["authorization"] else null

        val request = object : JsonArrayRequest(
            url + endpoint,
            onSuccess,
            onError
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (token) headers["Authorization"] = authorization!!
                return headers
            }
        }

        requestQueue.add(request)
    }

    fun addToRequestQueueObject(
        endpoint: String,
        onSuccess: Response.Listener<JSONObject>,
        onError: Response.ErrorListener,
        token: Boolean = true
    ) {
        if (headers.isEmpty() && token) return
        val authorization = if(token) headers["authorization"] else null

        val request = object : JsonObjectRequest(
            url + endpoint,
            onSuccess,
            onError
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (token) headers["Authorization"] = authorization!!
                return headers
            }
        }

        requestQueue.add(request)
    }

}
