package com.example.financial

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.financial.data.repository.FinancialRepository
import com.example.financial.presentation.performance.PerformanceMetricStore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

@RunWith(AndroidJUnit4::class)
class AutomatedPerformanceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun collectPerformanceMetricsAutomatically() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        val runId = arguments.getString("runId") ?: "manual"
        val version = arguments.getString("version") ?: "unknown"
        val datasetSizes = arguments.getString("datasetSizes")
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: listOf(1_000)
        val repeats = arguments.getString("repeats")?.toIntOrNull()?.coerceIn(1, 20) ?: 5
        val includeAi = arguments.getString("includeAi").toBoolean()
        val currentVariant = version == "optimized"
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser?.isAnonymous != true) {
            auth.signOut()
            Tasks.await(auth.signInAnonymously(), 60, TimeUnit.SECONDS)
        }

        val application = targetContext.applicationContext as FinancialApplication
        val database = application.database
        val repository = FinancialRepository(
            database.transactionDao(),
            database.accountDao(),
            database.accountGroupDao(),
            database.budgetDao(),
            database.chatMessageDao(),
            FirebaseFirestore.getInstance(),
            auth
        )
        val metricPreferences = targetContext.getSharedPreferences(
            "performance_benchmark_metrics",
            android.content.Context.MODE_PRIVATE
        )

        composeRule.waitUntil(60_000) {
            composeRule.onAllNodesWithText("Reports").fetchSemanticsNodes().isNotEmpty()
        }

        datasetSizes.forEach { datasetSize ->
            seedThroughUi(datasetSize, currentVariant)
            PerformanceMetricStore.clear(targetContext)
            measureReportQuery(repository, datasetSize, repeats, runId, version)

            val readyMetric = if (currentVariant) "report_aggregation" else "report_filter"
            val initialReportStart = SystemClock.elapsedRealtimeNanos()
            composeRule.onNodeWithText("Reports").performClick()
            composeRule.waitUntil(30_000) {
                val parts = metricPreferences.getString(readyMetric, null)?.split("|")
                val itemCount = parts?.getOrNull(1)?.toIntOrNull() ?: 0
                val recordedAtNanos = parts?.getOrNull(2)?.toLongOrNull() ?: 0L
                itemCount > 0 && recordedAtNanos >= initialReportStart
            }
            val initialReportCount = metricPreferences.getString(readyMetric, null)
                ?.split("|")
                ?.getOrNull(1)
                ?.toIntOrNull() ?: 0
            emit(
                runId,
                version,
                datasetSize,
                0,
                "report_open_and_load_ms",
                (SystemClock.elapsedRealtimeNanos() - initialReportStart) / 1_000_000.0,
                initialReportCount
            )

            val allTimeStartedAtNanos = SystemClock.elapsedRealtimeNanos()
            composeRule.onNodeWithText("All Time").performClick()
            waitForMetric(metricPreferences, readyMetric, allTimeStartedAtNanos, datasetSize)
            emit(
                runId,
                version,
                datasetSize,
                0,
                "report_all_time_load_ms",
                (SystemClock.elapsedRealtimeNanos() - allTimeStartedAtNanos) / 1_000_000.0,
                datasetSize
            )

            val aggregateMetric = if (currentVariant) {
                "report_aggregation"
            } else {
                "report_category_aggregation"
            }
            repeat(repeats) { iteration ->
                val period = if (iteration % 2 == 0) "3 Months" else "All Time"
                val startedAtNanos = SystemClock.elapsedRealtimeNanos()
                composeRule.onNodeWithText(period).performClick()
                waitForMetric(metricPreferences, aggregateMetric, startedAtNanos)
                val sample = iteration + 1
                emit(
                    runId,
                    version,
                    datasetSize,
                    sample,
                    "report_refresh_ms",
                    (SystemClock.elapsedRealtimeNanos() - startedAtNanos) / 1_000_000.0,
                    datasetSize
                )
                emitAggregationMetrics(
                    metricPreferences,
                    runId,
                    version,
                    datasetSize,
                    sample,
                    currentVariant
                )
            }

            collectInteractionMetrics(
                datasetSize,
                repeats,
                runId,
                version
            )

            if (includeAi) {
                collectAiMetrics(
                    metricPreferences,
                    runId,
                    version,
                    datasetSize
                )
            }

            collectAccountDetailMetrics(
                datasetSize,
                repeats,
                runId,
                version
            )
        }
    }

    private fun measureReportQuery(
        repository: FinancialRepository,
        datasetSize: Int,
        repeats: Int,
        runId: String,
        version: String
    ) {
        val cutoffMillis = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
        repeat(repeats + 1) { iteration ->
            var resultCount = 0
            val elapsedNanos = measureNanoTime {
                resultCount = runBlocking(Dispatchers.IO) {
                    repository.getReportTransactions(cutoffMillis).first().size
                }
            }
            if (iteration > 0) {
                emit(
                    runId,
                    version,
                    datasetSize,
                    iteration,
                    "report_query_ms",
                    elapsedNanos / 1_000_000.0,
                    resultCount
                )
            }
        }
    }

    private fun seedThroughUi(datasetSize: Int, optimized: Boolean) {
        composeRule.onNodeWithText("Settings").performClick()
        if (optimized) {
            composeRule.onNodeWithText("Guest Fake Data").performScrollTo().performClick()
            composeRule.onNodeWithText("Create $datasetSize transactions").performClick()
        } else {
            composeRule.onNodeWithText("Create ${datasetSize / 1_000}k transactions")
                .performScrollTo()
                .performClick()
            composeRule.onNodeWithText("Clear performance test data").performScrollTo()
        }
        val expectedStatus = if (optimized) {
            "Created $datasetSize test transactions in"
        } else {
            "Created $datasetSize performance test transactions."
        }
        composeRule.waitUntil(120_000) {
            composeRule.onAllNodesWithText(expectedStatus, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun emitAggregationMetrics(
        preferences: android.content.SharedPreferences,
        runId: String,
        version: String,
        datasetSize: Int,
        sample: Int,
        optimized: Boolean
    ) {
        val names = if (optimized) {
            listOf("report_aggregation")
        } else {
            listOf(
                "report_income_aggregation",
                "report_expense_aggregation",
                "report_category_aggregation"
            )
        }
        val measurements = names.mapNotNull { name ->
            val parts = preferences.getString(name, null)?.split("|") ?: return@mapNotNull null
            val elapsedNanos = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
            val itemCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
            elapsedNanos to itemCount
        }
        if (measurements.size == names.size) {
            emit(
                runId,
                version,
                datasetSize,
                sample,
                "report_aggregation_ms",
                measurements.sumOf { it.first } / 1_000_000.0,
                measurements.maxOf { it.second }
            )
        }
    }

    private fun collectInteractionMetrics(
        datasetSize: Int,
        repeats: Int,
        runId: String,
        version: String
    ) {
        shell("dumpsys gfxinfo $packageName reset")
        val pssBeforeKb = readPssKb(shell("dumpsys meminfo $packageName"))
        val tabs = listOf(
            "Accounts Source",
            "Budgets Status",
            "Charts & Trends",
            "Overview & AI"
        )
        repeat(repeats) { iteration ->
            val tab = tabs[iteration % tabs.size]
            val startedAtNanos = SystemClock.elapsedRealtimeNanos()
            composeRule.onNodeWithText(tab).performScrollTo().performClick()
            composeRule.waitForIdle()
            emit(
                runId,
                version,
                datasetSize,
                iteration + 1,
                "tab_switch_ms",
                (SystemClock.elapsedRealtimeNanos() - startedAtNanos) / 1_000_000.0,
                1
            )
            composeRule.onRoot().performTouchInput {
                swipeUp()
                swipeDown()
            }
        }
        composeRule.waitForIdle()

        val graphics = shell("dumpsys gfxinfo $packageName")
        val pssAfterKb = readPssKb(shell("dumpsys meminfo $packageName"))
        emitGraphicsMetrics(graphics, runId, version, datasetSize)
        emit(runId, version, datasetSize, 0, "pss_before_kb", pssBeforeKb, 0)
        emit(runId, version, datasetSize, 0, "pss_after_kb", pssAfterKb, 0)
    }

    private fun collectAccountDetailMetrics(
        datasetSize: Int,
        repeats: Int,
        runId: String,
        version: String
    ) {
        composeRule.onNodeWithText("Accounts").performClick()
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Performance Account 1")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        shell("dumpsys gfxinfo $packageName reset")
        val pssBeforeKb = readPssKb(shell("dumpsys meminfo $packageName"))
        repeat(repeats) { iteration ->
            val startedAtNanos = SystemClock.elapsedRealtimeNanos()
            composeRule.onNodeWithText("Performance Account 1").performClick()
            composeRule.waitUntil(30_000) {
                composeRule.onAllNodesWithText("Account Details").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("Transaction History").fetchSemanticsNodes().isNotEmpty()
            }
            emit(
                runId,
                version,
                datasetSize,
                iteration + 1,
                "account_detail_open_ms",
                (SystemClock.elapsedRealtimeNanos() - startedAtNanos) / 1_000_000.0,
                datasetSize / 5
            )

            val scrollStartedAtNanos = SystemClock.elapsedRealtimeNanos()
            composeRule.onRoot().performTouchInput { swipeUp() }
            composeRule.waitForIdle()
            composeRule.onRoot().performTouchInput { swipeDown() }
            composeRule.waitForIdle()
            emit(
                runId,
                version,
                datasetSize,
                iteration + 1,
                "account_history_scroll_ms",
                (SystemClock.elapsedRealtimeNanos() - scrollStartedAtNanos) / 1_000_000.0,
                datasetSize / 5
            )

            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.waitUntil(30_000) {
                composeRule.onAllNodesWithText("Performance Account 1")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }

        val graphics = shell("dumpsys gfxinfo $packageName")
        val pssAfterKb = readPssKb(shell("dumpsys meminfo $packageName"))
        emitGraphicsMetrics(graphics, runId, version, datasetSize, "account_")
        emit(runId, version, datasetSize, 0, "account_pss_before_kb", pssBeforeKb, 0)
        emit(runId, version, datasetSize, 0, "account_pss_after_kb", pssAfterKb, 0)
    }

    private fun collectAiMetrics(
        preferences: android.content.SharedPreferences,
        runId: String,
        version: String,
        datasetSize: Int
    ) {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            emit(runId, version, datasetSize, 1, "ai_skipped_missing_api_key", 1.0, 0)
            return
        }
        composeRule.onNodeWithText("Overview & AI").performScrollTo().performClick()
        val startedAtNanos = SystemClock.elapsedRealtimeNanos()
        composeRule.onNodeWithText("Get AI Analysis", substring = true)
            .performScrollTo()
            .performClick()
        composeRule.waitUntil(120_000) {
            metricRecordedAfter(preferences, "ai_complete", startedAtNanos)
        }
        val completionCount = preferences.getString("ai_complete", null)
            ?.split("|")
            ?.getOrNull(1)
            ?.toIntOrNull() ?: 0
        if (completionCount == 0) {
            emit(runId, version, datasetSize, 1, "ai_no_response_content", 1.0, 0)
            return
        }
        emitPreferenceMetric(preferences, "ai_first_content", runId, version, datasetSize)
        emitPreferenceMetric(preferences, "ai_complete", runId, version, datasetSize)
    }

    private fun emitPreferenceMetric(
        preferences: android.content.SharedPreferences,
        name: String,
        runId: String,
        version: String,
        datasetSize: Int
    ) {
        val parts = preferences.getString(name, null)?.split("|") ?: return
        val elapsedNanos = parts.getOrNull(0)?.toLongOrNull() ?: return
        val itemCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
        emit(runId, version, datasetSize, 1, "${name}_ms", elapsedNanos / 1_000_000.0, itemCount)
    }

    private fun waitForMetric(
        preferences: android.content.SharedPreferences,
        name: String,
        startedAtNanos: Long,
        expectedCount: Int? = null
    ) {
        composeRule.waitUntil(30_000) {
            metricRecordedAfter(preferences, name, startedAtNanos, expectedCount)
        }
    }

    private fun metricRecordedAfter(
        preferences: android.content.SharedPreferences,
        name: String,
        startedAtNanos: Long,
        expectedCount: Int? = null
    ): Boolean {
        val parts = preferences.getString(name, null)?.split("|") ?: return false
        val count = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val recordedAtNanos = parts.getOrNull(2)?.toLongOrNull() ?: return false
        val countMatches = if (expectedCount == null) count > 0 else count == expectedCount
        return recordedAtNanos >= startedAtNanos &&
            countMatches
    }

    private fun emitGraphicsMetrics(
        output: String,
        runId: String,
        version: String,
        datasetSize: Int,
        metricPrefix: String = ""
    ) {
        val totalFrames = requiredNumber(output, "Total frames rendered:\\s*([\\d,]+)", "total frames")
        val jankyFrames = requiredNumber(output, "Janky frames:\\s*([\\d,]+)", "janky frames")
        val jankyPercent = requiredNumber(
            output,
            "Janky frames:\\s*[\\d,]+\\s*\\(([\\d.]+)%\\)",
            "janky frame percentage"
        )
        val p95FrameMs = requiredNumber(output, "95th percentile:\\s*([\\d.]+)\\s*ms", "p95 frame time")
        emit(runId, version, datasetSize, 0, "${metricPrefix}total_frames", totalFrames, 0)
        emit(runId, version, datasetSize, 0, "${metricPrefix}janky_frames", jankyFrames, 0)
        emit(runId, version, datasetSize, 0, "${metricPrefix}janky_percent", jankyPercent, 0)
        emit(runId, version, datasetSize, 0, "${metricPrefix}p95_frame_ms", p95FrameMs, 0)
    }

    private fun readPssKb(output: String): Double =
        requiredNumber(output, "TOTAL PSS:\\s*([\\d,]+)", "total PSS")

    private fun requiredNumber(output: String, pattern: String, metric: String): Double =
        Regex(pattern).find(output)?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: throw AssertionError("Could not read $metric from device output.")

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use {
            it.readText()
        }
    }

    private fun emit(
        runId: String,
        version: String,
        datasetSize: Int,
        sample: Int,
        metric: String,
        value: Double,
        itemCount: Int
    ) {
        Log.i(
            "AutoPerfResult",
            "AUTO_PERF|$runId|$version|$datasetSize|$sample|$metric|${String.format(Locale.US, "%.3f", value)}|$itemCount"
        )
    }

    private companion object {
        const val packageName = "com.example.financial"
    }
}
