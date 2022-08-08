package com.example.hubspot.security.models

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.Request;

/**
 * This singleton class provides a single instance of a request queue. This request queue to is used
 * to store all outgoing json object requests.
 *
 * Code adapted from:
 * https://medium.com/@mendhie/send-device-to-device-push-notifications-without-server-side-code-238611c143
 */
class RequestQueueSingleton private constructor(context: Context) {
    private var requestQueue: RequestQueue?
    private val ctx: Context

    init {
        ctx = context
        requestQueue = getRequestQueue()
    }

    companion object {
        private var instance: RequestQueueSingleton? = null
        @Synchronized
        fun getInstance(context: Context): RequestQueueSingleton? {
            if (instance == null) {
                instance = RequestQueueSingleton(context)
            }
            return instance
        }
    }

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.applicationContext)
        }
        return requestQueue as RequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue().add<T>(req)
    }


}