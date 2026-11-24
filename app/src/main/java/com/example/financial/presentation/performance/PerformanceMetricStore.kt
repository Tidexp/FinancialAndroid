package com.example.financial.presentation.performance

import android.content.Context
import android.os.SystemClock
import com.example.financial.BuildConfig

object PerformanceMetricStore {
    private const val PREFERENCES_NAME = "performance_benchmark_metrics"

    fun clear(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun record(context: Context, name: String, elapsedNanos: Long, itemCount: Int) {
        if (!BuildConfig.DEBUG) return
        val recordedAtNanos = SystemClock.elapsedRealtimeNanos()
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(name, "$elapsedNanos|$itemCount|$recordedAtNanos")
            .apply()
    }

    fun read(context: Context, name: String): String? =
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(name, null)
}
