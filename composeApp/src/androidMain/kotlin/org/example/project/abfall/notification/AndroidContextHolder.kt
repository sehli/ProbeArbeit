package org.example.project.abfall.notification

import android.content.Context

object AndroidContextHolder {
    @Volatile
    var applicationContext: Context? = null
}
