/**
 * ChildModule.kt
 * Package: com.rasel.RasFocus
 *
 * RasFocus+ — Child/Student Persona Module
 * =========================================
 * This single-file module contains:
 *   1. Cloudinary upload helper (screenshot delivery)
 *   2. Background Services: FocusAccessibilityService, ScreenCaptureService,
 *      LocationTracker, FirebaseCommandListener
 *   3. Child Student Dashboard (Jetpack Compose — Material 3)
 *   4. Navigation integration snippet for MainActivity
 *
 * Dependencies required in build.gradle.kts (:app):
 * --------------------------------------------------
 *   // Cloudinary
 *   implementation("com.cloudinary:cloudinary-android:3.0.2")
 *
 *   // Firebase
 *   implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
 *   implementation("com.google.firebase:firebase-database-ktx")
 *   implementation("com.google.firebase:firebase-auth-ktx")
 *
 *   // Google Play Services — Location
 *   implementation("com.google.android.gms:play-services-location:21.3.0")
 *
 *   // Jetpack Compose (BOM)
 *   implementation(platform("androidx.compose:compose-bom:2024.06.00"))
 *   implementation("androidx.compose.ui:ui")
 *   implementation("androidx.compose.material3:material3")
 *   implementation("androidx.compose.ui:ui-tooling-preview")
 *   implementation("androidx.navigation:navigation-compose:2.7.7")
 *   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
 *
 * AndroidManifest.xml permissions required:
 * ------------------------------------------
 *   <uses-permission android:name="android.permission.INTERNET" />
 *   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *   <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 *   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
 *   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
 *   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 *   <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
 *   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 *
 * Register services in AndroidManifest.xml:
 * ------------------------------------------
 *   <service android:name=".child.FocusAccessibilityService"
 *       android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
 *       android:exported="true">
 *       <intent-filter>
 *           <action android:name="android.accessibilityservice.AccessibilityService" />
 *       </intent-filter>
 *       <meta-data android:name="android.accessibilityservice"
 *           android:resource="@xml/accessibility_service_config" />
 *   </service>
 *
 *   <service android:name=".child.ScreenCaptureService"
 *       android:foregroundServiceType="mediaProjection"
 *       android:exported="false" />
 *
 *   <service android:name=".child.LocationTracker"
 *       android:foregroundServiceType="location"
 *       android:exported="false" />
 *
 *   <service android:name=".child.FirebaseCommandListener"
 *       android:exported="false" />
 *
 * res/xml/accessibility_service_config.xml:
 * ------------------------------------------
 *   <?xml version="1.0" encoding="utf-8"?>
 *   <accessibility-service
 *       xmlns:android="http://schemas.android.com/apk/res/android"
 *       android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged"
 *       android:accessibilityFeedbackType="feedbackGeneric"
 *       android:accessibilityFlags="flagDefault"
 *       android:canRetrieveWindowContent="true"
 *       android:notificationTimeout="100"
 *       android:description="@string/accessibility_service_description" />
 */

package com.rasel.RasFocus.child

// ─────────────────────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────────────────────

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.database.database
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 0 — Constants & Theme
// ─────────────────────────────────────────────────────────────────────────────

private const val TAG = "RasFocus_Child"

/** Cloudinary credentials — keep these in BuildConfig / encrypted prefs in prod */
object CloudinaryConfig {
    const val CLOUD_NAME   = "do320jmaf"
    const val API_KEY      = "585237258563414"
    const val API_SECRET   = "Oz6aQ3OqZw2DFlnn5_d1QCQuClo"
    const val UPLOAD_PRESET = "rasfocus_screenshots"   // Create unsigned preset in Cloudinary dashboard
}

object ChildColors {
    val Teal        = Color(0xFF0096B4)
    val TealLight   = Color(0xFF33B5CC)
    val TealDark    = Color(0xFF006E85)
    val TealSurface = Color(0xFFE0F7FA)
    val White       = Color(0xFFFFFFFF)
    val OffWhite    = Color(0xFFF5FEFF)
    val TextPrimary = Color(0xFF0D2B33)
    val TextSecond  = Color(0xFF4A7A87)
    val Emergency   = Color(0xFFD32F2F)
    val EmergencyBg = Color(0xFFFFEBEE)
    val Success     = Color(0xFF2E7D32)
    val Warning     = Color(0xFFF57F17)
}

private val ChildTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,  fontSize = 36.sp, letterSpacing = (-1).sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.Bold,   fontSize = 22.sp, letterSpacing = (-0.3).sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 14.sp, letterSpacing = 0.3.sp),
)

private val ChildColorScheme = lightColorScheme(
    primary         = ChildColors.Teal,
    onPrimary       = ChildColors.White,
    primaryContainer= ChildColors.TealSurface,
    onPrimaryContainer = ChildColors.TealDark,
    secondary       = ChildColors.TealLight,
    surface         = ChildColors.White,
    background      = ChildColors.OffWhite,
    onBackground    = ChildColors.TextPrimary,
    onSurface       = ChildColors.TextPrimary,
)

@Composable
fun ChildTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChildColorScheme,
        typography  = ChildTypography,
        shapes      = Shapes(
            small  = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large  = RoundedCornerShape(24.dp),
        ),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 1 — Cloudinary Upload Helper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Call once (e.g., in Application.onCreate) before using [uploadScreenshotToCloudinary].
 */
fun initCloudinary(context: Context) {
    try {
        MediaManager.get()
    } catch (e: Exception) {
        val config = mapOf(
            "cloud_name"  to CloudinaryConfig.CLOUD_NAME,
            "api_key"     to CloudinaryConfig.API_KEY,
            "api_secret"  to CloudinaryConfig.API_SECRET,
            "secure"      to true
        )
        MediaManager.init(context.applicationContext, config)
    }
}

/**
 * Uploads a screenshot [File] to Cloudinary (child_screenshots folder).
 * Returns the secure HTTPS URL on success, throws on failure.
 *
 * Designed to be called from a coroutine (suspend function).
 */
suspend fun uploadScreenshotToCloudinary(
    imageFile: File,
    childUid: String
): String = suspendCancellableCoroutine { continuation ->

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val publicId  = "child_screenshots/${childUid}/${timestamp}"

    val requestId = MediaManager.get()
        .upload(imageFile.absolutePath)
        .option("folder",    "child_screenshots")
        .option("public_id", publicId)
        .option("resource_type", "image")
        .unsigned(CloudinaryConfig.UPLOAD_PRESET)   // Switch to signed if preset is signed
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {
                Log.d(TAG, "Cloudinary upload started: $requestId")
            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                val progress = if (totalBytes > 0) (bytes * 100 / totalBytes) else 0
                Log.d(TAG, "Upload progress: $progress%")
            }

            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                val secureUrl = resultData?.get("secure_url") as? String
                if (secureUrl != null) {
                    Log.i(TAG, "Screenshot uploaded: $secureUrl")
                    continuation.resume(secureUrl)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("Cloudinary returned null secure_url")
                    )
                }
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                val msg = error?.description ?: "Unknown Cloudinary error"
                Log.e(TAG, "Cloudinary upload failed: $msg")
                continuation.resumeWithException(RuntimeException(msg))
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                Log.w(TAG, "Cloudinary upload rescheduled: ${error?.description}")
            }
        })
        .dispatch()

    continuation.invokeOnCancellation {
        MediaManager.get().cancelRequest(requestId)
    }
}

/**
 * Pushes the uploaded screenshot URL to the child's Firebase node.
 */
fun pushScreenshotUrlToFirebase(childUid: String, secureUrl: String) {
    val db        = Firebase.database.reference
    val timestamp = System.currentTimeMillis()
    val entry     = mapOf(
        "url"       to secureUrl,
        "timestamp" to timestamp,
        "status"    to "delivered"
    )
    db.child("children/$childUid/screenshots").push().setValue(entry)
        .addOnSuccessListener { Log.i(TAG, "Screenshot URL pushed to Firebase") }
        .addOnFailureListener { Log.e(TAG, "Failed to push URL: ${it.message}") }

    // Clear the command flag so it doesn't re-trigger
    db.child("children/$childUid/commands/request_screenshot").setValue(false)
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 2 — BACKGROUND SERVICES
// ─────────────────────────────────────────────────────────────────────────────

// ── 2a. FocusAccessibilityService ───────────────────────────────────────────

/**
 * Monitors foreground app. Blocks apps in the Firebase `blocked_apps` list by
 * launching a full-screen overlay. Also intercepts RasFocus+ uninstall attempts.
 */
class FocusAccessibilityService : AccessibilityService() {

    private val db by lazy { Firebase.database.reference }
    private val childUid get() = Firebase.auth.currentUser?.uid ?: ""

    /** Live-updated set of blocked package names from Firebase */
    private val blockedApps = mutableSetOf<String>()
    private var blockedAppsListener: ValueEventListener? = null

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType  = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags         = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100L
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        listenForBlockedApps()
        Log.i(TAG, "FocusAccessibilityService connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        blockedAppsListener?.let {
            db.child("children/$childUid/blocked_apps").removeEventListener(it)
        }
        removeOverlay()
    }

    // ── Accessibility Events ─────────────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || childUid.isEmpty()) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pkg = event.packageName?.toString() ?: return
                handleAppForeground(pkg)
                checkForUninstallAttempt(pkg, event)
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "FocusAccessibilityService interrupted")
    }

    // ── Blocked App Logic ────────────────────────────────────────────────────

    private fun handleAppForeground(packageName: String) {
        // Whitelist: never block RasFocus+ itself or system UI
        val whiteList = setOf(
            "com.rasel.RasFocus",
            "com.android.systemui",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher"
        )
        if (packageName in whiteList) {
            removeOverlay()
            return
        }

        if (packageName in blockedApps) {
            showBlockedOverlay(packageName)
        } else {
            removeOverlay()
        }
    }

    private fun listenForBlockedApps() {
        if (childUid.isEmpty()) return
        val ref = db.child("children/$childUid/blocked_apps")
        blockedAppsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blockedApps.clear()
                snapshot.children.forEach { child ->
                    child.getValue(String::class.java)?.let { blockedApps.add(it) }
                }
                Log.d(TAG, "Blocked apps updated: $blockedApps")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load blocked apps: ${error.message}")
            }
        }
        ref.addValueEventListener(blockedAppsListener!!)
    }

    // ── Uninstall Prevention ─────────────────────────────────────────────────

    private fun checkForUninstallAttempt(packageName: String, event: AccessibilityEvent) {
        // Package installer dialogs typically have these package names
        val installerPackages = setOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.miui.packageinstaller"
        )
        if (packageName !in installerPackages) return

        // Check if the node tree mentions "RasFocus" in the description
        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return
        val nodeText = collectNodeText(rootNode)
        if ("RasFocus" in nodeText || "rasel" in nodeText.lowercase()) {
            Log.w(TAG, "Uninstall attempt detected for RasFocus+! Blocking.")
            // Go back to home immediately
            performGlobalAction(GLOBAL_ACTION_HOME)
            // Notify parent via Firebase
            if (childUid.isNotEmpty()) {
                db.child("children/$childUid/alerts/uninstall_attempt")
                    .setValue(System.currentTimeMillis())
            }
        }
    }

    private fun collectNodeText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        fun traverse(n: AccessibilityNodeInfo?) {
            n ?: return
            sb.append(n.text ?: "")
            sb.append(n.contentDescription ?: "")
            for (i in 0 until n.childCount) traverse(n.getChild(i))
        }
        traverse(node)
        return sb.toString()
    }

    // ── Overlay Management ───────────────────────────────────────────────────

    private fun showBlockedOverlay(blockedPackage: String) {
        if (overlayView != null) return  // already showing
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        val appLabel = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(blockedPackage, 0)
            ).toString()
        } catch (e: Exception) { blockedPackage }

        val inflater = LayoutInflater.from(this)
        // Inflate a simple blocking view (XML-free: build programmatically)
        val tv = android.widget.TextView(this).apply {
            text = "🚫  $appLabel is blocked by your parent.\n\nContact your parent to unlock it."
            textSize = 18f
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setTextColor(android.graphics.Color.WHITE)
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#CC000000"))
            addView(tv)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(layout, params)
            overlayView = layout
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay: ${e.message}")
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
            overlayView = null
        }
    }
}

// ── 2b. ScreenCaptureService ─────────────────────────────────────────────────

/**
 * Foreground service that holds a MediaProjection token.
 * Listens to Firebase `children/{uid}/commands/request_screenshot`.
 * When the flag is true, captures the screen and uploads to Cloudinary.
 */
class ScreenCaptureService : Service() {

    companion object {
        const val CHANNEL_ID           = "ScreenCaptureChannel"
        const val NOTIF_ID             = 1001
        const val EXTRA_RESULT_CODE    = "result_code"
        const val EXTRA_RESULT_INTENT  = "result_intent"

        fun buildStartIntent(
            context: Context,
            resultCode: Int,
            data: Intent
        ) = Intent(context, ScreenCaptureService::class.java).apply {
            putExtra(EXTRA_RESULT_CODE,   resultCode)
            putExtra(EXTRA_RESULT_INTENT, data)
        }
    }

    private val serviceScope     = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val db               by lazy { Firebase.database.reference }
    private val childUid         get() = Firebase.auth.currentUser?.uid ?: ""

    private var mediaProjection  : MediaProjection?  = null
    private var virtualDisplay   : VirtualDisplay?   = null
    private var imageReader      : ImageReader?       = null
    private var screenshotListener: ValueEventListener? = null

    // ── Service Lifecycle ────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIF_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        val resultCode   = intent?.getIntExtra(EXTRA_RESULT_CODE,   Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val resultData   = intent.getParcelableExtra<Intent>(EXTRA_RESULT_INTENT) ?: return START_NOT_STICKY

        val projManager  = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection  = projManager.getMediaProjection(resultCode, resultData)

        initVirtualDisplay()
        listenForScreenshotCommands()

        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        screenshotListener?.let {
            db.child("children/$childUid/commands/request_screenshot").removeEventListener(it)
        }
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Display & Capture ────────────────────────────────────────────────────

    private fun initVirtualDisplay() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)

        val width  = metrics.widthPixels
        val height = metrics.heightPixels
        val dpi    = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, android.graphics.ImageFormat.RGB_565, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "RasFocusCapture",
            width, height, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null, null
        )
    }

    private fun listenForScreenshotCommands() {
        if (childUid.isEmpty()) return
        val ref = db.child("children/$childUid/commands/request_screenshot")
        screenshotListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requested = snapshot.getValue(Boolean::class.java) ?: false
                if (requested) {
                    Log.i(TAG, "Screenshot command received")
                    serviceScope.launch { captureAndUpload() }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Screenshot listener cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(screenshotListener!!)
    }

    private suspend fun captureAndUpload() {
        try {
            // Brief delay to let the display settle
            delay(300)
            val image = imageReader?.acquireLatestImage()
                ?: run { Log.w(TAG, "No image available"); return }

            val planes   = image.planes
            val buffer   = planes[0].buffer
            val width    = image.width
            val height   = image.height
            val pixStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixStride * width

            val bitmap = android.graphics.Bitmap.createBitmap(
                width + rowPadding / pixStride, height,
                android.graphics.Bitmap.Config.RGB_565
            )
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()

            // Save to cache dir
            val file = File(cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
            }

            // Upload
            val secureUrl = uploadScreenshotToCloudinary(file, childUid)
            pushScreenshotUrlToFirebase(childUid, secureUrl)
            file.delete()

        } catch (e: Exception) {
            Log.e(TAG, "Screenshot capture/upload failed: ${e.message}")
            // Report failure to Firebase
            db.child("children/$childUid/commands/screenshot_error")
                .setValue(e.message ?: "unknown error")
        }
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Screen Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "RasFocus+ screen monitoring service" }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("RasFocus+")
        .setContentText("Focus monitoring active")
        .setSmallIcon(android.R.drawable.ic_menu_camera)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}

// ── 2c. LocationTracker ──────────────────────────────────────────────────────

/**
 * Foreground service using FusedLocationProviderClient.
 * Pushes GPS coordinates to Firebase every [LOCATION_INTERVAL_MS] milliseconds.
 */
class LocationTracker : Service() {

    companion object {
        const val CHANNEL_ID          = "LocationChannel"
        const val NOTIF_ID            = 1002
        const val LOCATION_INTERVAL_MS = 5 * 60 * 1000L  // 5 minutes
    }

    private val db        by lazy { Firebase.database.reference }
    private val childUid  get()  = Firebase.auth.currentUser?.uid ?: ""

    private lateinit var fusedClient : FusedLocationProviderClient
    private var locationCallback     : LocationCallback? = null

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIF_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
        startTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Location Logic ───────────────────────────────────────────────────────

    private fun startTracking() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        ).apply {
            setMinUpdateDistanceMeters(50f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    pushLocation(location.latitude, location.longitude, location.accuracy)
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted: ${e.message}")
        }
    }

    /**
     * Pushes location data to: children/{uid}/location
     * Keeps a trail under children/{uid}/location_history (last 50 entries)
     */
    private fun pushLocation(lat: Double, lng: Double, accuracy: Float) {
        if (childUid.isEmpty()) return
        val timestamp = System.currentTimeMillis()
        val locationData = mapOf(
            "lat"       to lat,
            "lng"       to lng,
            "accuracy"  to accuracy,
            "timestamp" to timestamp
        )
        // Live location (always overwrite)
        db.child("children/$childUid/location").setValue(locationData)

        // History trail (push new entry, trim to 50)
        db.child("children/$childUid/location_history").push().setValue(locationData)
        Log.d(TAG, "Location pushed: $lat, $lng")
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Location Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "RasFocus+ location tracking" }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("RasFocus+ — Location Active")
        .setContentText("Your location is shared with your parent.")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}

// ── 2d. FirebaseCommandListener ──────────────────────────────────────────────

/**
 * Listens to the parent's Firebase command node and executes directives:
 *   - is_locked        → show full-screen blocking overlay
 *   - request_screenshot → trigger ScreenCaptureService
 *   - screen_time_limit → notify when limit is reached
 *   - bedtime_active   → lock device during configured bedtime window
 */
class FirebaseCommandListener : Service() {

    companion object {
        const val CHANNEL_ID = "CommandChannel"
        const val NOTIF_ID   = 1003
    }

    private val db       by lazy { Firebase.database.reference }
    private val childUid get()  = Firebase.auth.currentUser?.uid ?: ""

    private var commandsListener : ValueEventListener? = null
    private var lockOverlayView  : View? = null
    private var windowManager    : WindowManager? = null

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        listenForCommands()
        return START_STICKY
    }

    override fun onDestroy() {
        commandsListener?.let {
            db.child("children/$childUid/commands").removeEventListener(it)
        }
        removeLockOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Command Listener ─────────────────────────────────────────────────────

    private fun listenForCommands() {
        if (childUid.isEmpty()) return
        val ref = db.child("children/$childUid/commands")

        commandsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isLocked       = snapshot.child("is_locked").getValue(Boolean::class.java) ?: false
                val bedtimeActive  = snapshot.child("bedtime_active").getValue(Boolean::class.java) ?: false
                val screenTimeMsg  = snapshot.child("screen_time_message").getValue(String::class.java)
                val panicAck       = snapshot.child("sos_acknowledged").getValue(Boolean::class.java) ?: false

                when {
                    isLocked || bedtimeActive -> showLockOverlay(
                        if (bedtimeActive) "Bedtime Mode" else "Locked by Parent",
                        if (bedtimeActive)
                            "It's bedtime! The device will unlock in the morning. 🌙"
                        else
                            "Your parent has locked this device. Please check in with them."
                    )
                    else -> removeLockOverlay()
                }

                if (screenTimeMsg != null) {
                    Toast.makeText(this@FirebaseCommandListener, screenTimeMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Command listener cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(commandsListener!!)
    }

    // ── Lock Overlay ─────────────────────────────────────────────────────────

    private fun showLockOverlay(title: String, message: String) {
        if (lockOverlayView != null) return
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "SYSTEM_ALERT_WINDOW not granted — cannot show lock overlay")
            return
        }

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity     = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#F00096B4"))

            val titleView = android.widget.TextView(context).apply {
                text      = "🔒  $title"
                textSize  = 24f
                setTextColor(android.graphics.Color.WHITE)
                typeface  = android.graphics.Typeface.DEFAULT_BOLD
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 0, 32, 24)
            }
            val msgView = android.widget.TextView(context).apply {
                text      = message
                textSize  = 16f
                setTextColor(android.graphics.Color.argb(220, 255, 255, 255))
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                setPadding(48, 0, 48, 0)
            }
            addView(titleView)
            addView(msgView)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(container, params)
            lockOverlayView = container
        } catch (e: Exception) {
            Log.e(TAG, "Lock overlay error: ${e.message}")
        }
    }

    private fun removeLockOverlay() {
        lockOverlayView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
            lockOverlayView = null
        }
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Parental Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "RasFocus+ parental command listener" }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("RasFocus+ Active")
        .setContentText("Parental controls are enabled.")
        .setSmallIcon(android.R.drawable.ic_lock_lock)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 3 — CHILD / STUDENT DASHBOARD (Jetpack Compose)
// ─────────────────────────────────────────────────────────────────────────────

// ── 3a. ViewModel ────────────────────────────────────────────────────────────

data class Restriction(val label: String, val icon: String, val isActive: Boolean)

data class ChildUiState(
    val studentName       : String = "Student",
    val parentConnected   : Boolean = false,
    val pomodoroSeconds   : Int  = 25 * 60,
    val pomodoroRunning   : Boolean = false,
    val pomodoroPhase     : String  = "Focus",    // "Focus" | "Break"
    val restrictions      : List<Restriction> = emptyList(),
    val sosLoading        : Boolean = false,
    val sosSuccess        : Boolean = false,
    val pinDigits         : List<String> = List(6) { "" },
    val pinError          : String? = null,
    val pinLoading        : Boolean = false,
    val pinSuccess        : Boolean = false,
)

class ChildViewModel : ViewModel() {

    private val db by lazy { Firebase.database.reference }
    private val childUid get() = Firebase.auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState : StateFlow<ChildUiState> = _uiState.asStateFlow()

    private var pomodoroJob : Job? = null

    init {
        if (childUid.isNotEmpty()) loadRestrictions()
    }

    // ── Pomodoro ──────────────────────────────────────────────────────────────

    fun togglePomodoro() {
        if (_uiState.value.pomodoroRunning) pausePomodoro() else startPomodoro()
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        val phase = _uiState.value.pomodoroPhase
        val totalSeconds = if (phase == "Focus") 25 * 60 else 5 * 60
        _uiState.update { it.copy(pomodoroSeconds = totalSeconds, pomodoroRunning = false) }
    }

    private fun startPomodoro() {
        _uiState.update { it.copy(pomodoroRunning = true) }
        pomodoroJob = viewModelScope.launch {
            while (_uiState.value.pomodoroSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(pomodoroSeconds = it.pomodoroSeconds - 1) }
            }
            // Switch phase
            val nextPhase   = if (_uiState.value.pomodoroPhase == "Focus") "Break" else "Focus"
            val nextSeconds = if (nextPhase == "Focus") 25 * 60 else 5 * 60
            _uiState.update { it.copy(
                pomodoroPhase   = nextPhase,
                pomodoroSeconds = nextSeconds,
                pomodoroRunning = false
            )}
        }
    }

    private fun pausePomodoro() {
        pomodoroJob?.cancel()
        _uiState.update { it.copy(pomodoroRunning = false) }
    }

    // ── Restrictions ─────────────────────────────────────────────────────────

    private fun loadRestrictions() {
        db.child("children/$childUid/commands")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Restriction>()
                    val isLocked      = snapshot.child("is_locked").getValue(Boolean::class.java) ?: false
                    val bedtimeActive = snapshot.child("bedtime_active").getValue(Boolean::class.java) ?: false
                    val bedtimeRange  = snapshot.child("bedtime_range").getValue(String::class.java)

                    if (isLocked) list.add(Restriction("Device Locked by Parent", "🔒", true))
                    if (bedtimeActive) list.add(Restriction("Bedtime Lock: $bedtimeRange", "🌙", true))

                    snapshot.child("blocked_apps_labels").children.forEach { c ->
                        c.getValue(String::class.java)?.let {
                            list.add(Restriction("$it is Blocked", "🚫", true))
                        }
                    }
                    _uiState.update { it.copy(restrictions = list) }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── PIN Pairing ───────────────────────────────────────────────────────────

    fun updatePinDigit(index: Int, value: String) {
        val digits = _uiState.value.pinDigits.toMutableList()
        digits[index] = value.take(1)
        _uiState.update { it.copy(pinDigits = digits, pinError = null) }
    }

    fun submitPin() {
        val pin = _uiState.value.pinDigits.joinToString("")
        if (pin.length < 6) {
            _uiState.update { it.copy(pinError = "Enter all 6 digits") }
            return
        }
        _uiState.update { it.copy(pinLoading = true, pinError = null) }

        // Look up the PIN in Firebase: /pairing_codes/{pin} → parentUid
        db.child("pairing_codes/$pin")
            .get()
            .addOnSuccessListener { snapshot ->
                val parentUid = snapshot.getValue(String::class.java)
                if (parentUid != null) {
                    // Link child to parent
                    db.child("children/$childUid/parent_uid").setValue(parentUid)
                    db.child("parents/$parentUid/children/$childUid").setValue(true)
                    _uiState.update { it.copy(
                        pinLoading   = false,
                        pinSuccess   = true,
                        parentConnected = true
                    )}
                } else {
                    _uiState.update { it.copy(
                        pinLoading = false,
                        pinError   = "Invalid PIN. Ask your parent for the code."
                    )}
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(
                    pinLoading = false,
                    pinError   = "Connection failed. Check your internet."
                )}
            }
    }

    // ── SOS / Panic ───────────────────────────────────────────────────────────

    fun triggerSOS(context: Context) {
        if (childUid.isEmpty()) return
        _uiState.update { it.copy(sosLoading = true) }

        // Get last known location then push SOS
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                val sosData = mapOf(
                    "triggered_at" to System.currentTimeMillis(),
                    "lat"          to (loc?.latitude  ?: 0.0),
                    "lng"          to (loc?.longitude ?: 0.0),
                    "acknowledged" to false,
                    "child_uid"    to childUid
                )
                db.child("children/$childUid/sos").setValue(sosData)

                // Also push to parent's alert node
                db.child("children/$childUid/parent_uid").get()
                    .addOnSuccessListener { snap ->
                        val parentUid = snap.getValue(String::class.java)
                        if (parentUid != null) {
                            db.child("parents/$parentUid/sos_alerts/$childUid").setValue(sosData)
                        }
                    }

                _uiState.update { it.copy(sosLoading = false, sosSuccess = true) }

                viewModelScope.launch {
                    delay(4000)
                    _uiState.update { it.copy(sosSuccess = false) }
                }
            }.addOnFailureListener {
                _uiState.update { it.copy(sosLoading = false) }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing for SOS: ${e.message}")
            _uiState.update { it.copy(sosLoading = false) }
        }
    }
}

// ── 3b. Dashboard Screen ──────────────────────────────────────────────────────

@Composable
fun ChildDashboardScreen(
    viewModel: ChildViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ChildTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChildColors.OffWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp)    // room for SOS button
            ) {
                // ── Header ───────────────────────────────────────────────────
                ChildDashboardHeader(studentName = uiState.studentName)

                Spacer(Modifier.height(8.dp))

                // ── Pairing Card ─────────────────────────────────────────────
                AnimatedVisibility(visible = !uiState.pinSuccess && !uiState.parentConnected) {
                    ConnectParentCard(
                        pinDigits    = uiState.pinDigits,
                        pinError     = uiState.pinError,
                        pinLoading   = uiState.pinLoading,
                        onDigitChange = viewModel::updatePinDigit,
                        onSubmit      = viewModel::submitPin,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                AnimatedVisibility(visible = uiState.pinSuccess || uiState.parentConnected) {
                    ConnectedSuccessBanner(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Pomodoro Timer ───────────────────────────────────────────
                PomodoroCard(
                    seconds     = uiState.pomodoroSeconds,
                    isRunning   = uiState.pomodoroRunning,
                    phase       = uiState.pomodoroPhase,
                    onToggle    = viewModel::togglePomodoro,
                    onReset     = viewModel::resetPomodoro,
                    modifier    = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(16.dp))

                // ── Active Restrictions ──────────────────────────────────────
                ActiveRestrictionsCard(
                    restrictions = uiState.restrictions,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── SOS Button (floating at bottom) ──────────────────────────────
            SOSButton(
                loading  = uiState.sosLoading,
                success  = uiState.sosSuccess,
                onPress  = { viewModel.triggerSOS(context) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
            )
        }
    }
}

// ── 3c. Header ───────────────────────────────────────────────────────────────

@Composable
private fun ChildDashboardHeader(studentName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ChildColors.Teal, ChildColors.TealDark)
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column {
            Text(
                text  = "Good ${timeOfDayGreeting()} 👋",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = ChildColors.White.copy(alpha = 0.8f)
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = studentName,
                style = MaterialTheme.typography.displayLarge.copy(color = ChildColors.White)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "Stay focused. Stay great. ✨",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = ChildColors.White.copy(alpha = 0.75f)
                )
            )
        }
    }
}

private fun timeOfDayGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Morning"
        in 12..16 -> "Afternoon"
        else      -> "Evening"
    }
}

// ── 3d. Connect Parent Card ───────────────────────────────────────────────────

@Composable
private fun ConnectParentCard(
    pinDigits    : List<String>,
    pinError     : String?,
    pinLoading   : Boolean,
    onDigitChange: (Int, String) -> Unit,
    onSubmit     : () -> Unit,
    modifier     : Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ChildColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                tint       = ChildColors.Teal,
                modifier   = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "Connect with Parent",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Enter the 6-digit PIN from your parent's RasFocus+ app.",
                style = MaterialTheme.typography.bodyLarge.copy(color = ChildColors.TextSecond),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 6 individual PIN digit boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                val focusRequesters = remember { List(6) { FocusRequester() } }

                pinDigits.forEachIndexed { index, digit ->
                    PinDigitBox(
                        digit         = digit,
                        focused       = false,
                        focusRequester= focusRequesters[index],
                        onValueChange = { newVal ->
                            onDigitChange(index, newVal)
                            if (newVal.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        },
                        onBackspace = {
                            if (digit.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    )
                }
            }

            // Error message
            AnimatedVisibility(visible = pinError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = pinError ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = ChildColors.Emergency),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = onSubmit,
                enabled  = !pinLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChildColors.Teal)
            ) {
                if (pinLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color    = ChildColors.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Pair with Parent", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun PinDigitBox(
    digit         : String,
    focused       : Boolean,
    focusRequester: FocusRequester,
    onValueChange : (String) -> Unit,
    onBackspace   : () -> Unit
) {
    val borderColor by animateColorAsState(
        if (digit.isNotEmpty()) ChildColors.Teal else Color.LightGray,
        label = "PinBorderColor"
    )

    OutlinedTextField(
        value           = digit,
        onValueChange   = { newVal ->
            if (newVal.length <= 1 && newVal.all { it.isDigit() }) onValueChange(newVal)
        },
        modifier        = Modifier
            .size(width = 44.dp, height = 56.dp)
            .focusRequester(focusRequester),
        textStyle       = TextStyle(
            textAlign  = TextAlign.Center,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = ChildColors.TextPrimary
        ),
        singleLine      = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction    = ImeAction.Next
        ),
        shape           = RoundedCornerShape(12.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ChildColors.Teal,
            unfocusedBorderColor = borderColor,
            cursorColor          = ChildColors.Teal,
            focusedContainerColor= ChildColors.TealSurface.copy(alpha = 0.4f),
        )
    )
}

// ── 3e. Connected Banner ──────────────────────────────────────────────────────

@Composable
private fun ConnectedSuccessBanner(modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier            = Modifier.padding(20.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint   = ChildColors.Success,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Connected to Parent",
                    style = MaterialTheme.typography.titleLarge.copy(color = ChildColors.Success)
                )
                Text(
                    "Parental supervision is active.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = ChildColors.Success.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

// ── 3f. Pomodoro Card ─────────────────────────────────────────────────────────

@Composable
private fun PomodoroCard(
    seconds   : Int,
    isRunning : Boolean,
    phase     : String,
    onToggle  : () -> Unit,
    onReset   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    val totalSeconds  = if (phase == "Focus") 25 * 60 else 5 * 60
    val progress      = seconds.toFloat() / totalSeconds.toFloat()
    val minutes       = seconds / 60
    val secs          = seconds % 60

    // Animate the ring color
    val ringColor by animateColorAsState(
        targetValue = if (phase == "Focus") ChildColors.Teal else Color(0xFF4CAF50),
        animationSpec = tween(600),
        label = "RingColor"
    )

    // Pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ChildColors.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "🍅  Pomodoro Timer",
                    style = MaterialTheme.typography.titleLarge
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (phase == "Focus") ChildColors.TealSurface else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text  = phase,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (phase == "Focus") ChildColors.Teal else ChildColors.Success
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Arc ring timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke  = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    val padding = 12.dp.toPx()
                    val topLeft = Offset(padding, padding)
                    val arcSize = Size(size.width - padding * 2, size.height - padding * 2)

                    // Background track
                    drawArc(
                        color     = ChildColors.TealSurface,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = stroke
                    )
                    // Progress arc
                    drawArc(
                        color     = ringColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = stroke
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "%02d:%02d".format(minutes, secs),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 40.sp,
                            color    = ChildColors.TextPrimary
                        )
                    )
                    Text(
                        text  = if (isRunning) "Focusing…" else "Paused",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ChildColors.TextSecond
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick  = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape    = RoundedCornerShape(16.dp),
                    border   = BorderStroke(1.5.dp, ChildColors.Teal)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = ChildColors.Teal)
                    Spacer(Modifier.width(6.dp))
                    Text("Reset", color = ChildColors.Teal)
                }

                Button(
                    onClick  = onToggle,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChildColors.Teal)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isRunning) "Pause" else "Start")
                }
            }
        }
    }
}

// ── 3g. Active Restrictions Card ─────────────────────────────────────────────

@Composable
private fun ActiveRestrictionsCard(
    restrictions : List<Restriction>,
    modifier     : Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ChildColors.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint     = ChildColors.Teal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Active Restrictions",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Your parent has set up these rules to keep you safe.",
                style = MaterialTheme.typography.bodyLarge.copy(color = ChildColors.TextSecond)
            )

            Spacer(Modifier.height(16.dp))

            if (restrictions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ChildColors.TealSurface.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No active restrictions 🎉",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ChildColors.Teal
                        )
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    restrictions.forEach { restriction ->
                        RestrictionRow(restriction)
                    }
                }
            }
        }
    }
}

@Composable
private fun RestrictionRow(restriction: Restriction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (restriction.isActive) ChildColors.TealSurface.copy(alpha = 0.6f)
                else Color.LightGray.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(restriction.icon, fontSize = 20.sp)
        Text(
            restriction.label,
            style    = MaterialTheme.typography.bodyLarge.copy(
                color  = if (restriction.isActive) ChildColors.TealDark else ChildColors.TextSecond,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )
        if (restriction.isActive) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ChildColors.Teal
            ) {
                Text(
                    "Active",
                    style    = MaterialTheme.typography.labelLarge.copy(
                        color    = ChildColors.White,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ── 3h. SOS / Panic Button ────────────────────────────────────────────────────

@Composable
private fun SOSButton(
    loading  : Boolean,
    success  : Boolean,
    onPress  : () -> Unit,
    modifier : Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue    = if (loading) 0.96f else 1f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label          = "SOSScale"
    )

    val bgColor by animateColorAsState(
        targetValue   = if (success) ChildColors.Success else ChildColors.Emergency,
        animationSpec = tween(400),
        label         = "SOSColor"
    )

    Button(
        onClick  = onPress,
        modifier = modifier
            .height(62.dp)
            .scale(scale),
        enabled  = !loading,
        shape    = RoundedCornerShape(20.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = bgColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = 6.dp,
            pressedElevation  = 2.dp
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color    = ChildColors.White,
                strokeWidth = 2.5.dp
            )
        } else if (success) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "SOS Sent! Help is on the way.",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        } else {
            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "🆘  Emergency / SOS",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 4 — PERMISSION HELPERS
// ─────────────────────────────────────────────────────────────────────────────

object ChildPermissions {

    /** Checks and requests all required runtime permissions for the child module. */
    @Composable
    fun RequestAllPermissions(onAllGranted: () -> Unit) {
        val context = LocalContext.current
        var overlayChecked  by remember { mutableStateOf(false) }
        var locationGranted by remember { mutableStateOf(false) }
        var notifGranted    by remember { mutableStateOf(false) }

        // SYSTEM_ALERT_WINDOW — must be handled via Settings intent (not runtime request)
        LaunchedEffect(Unit) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                overlayChecked = true
            }
        }

        // Location permission
        val locationLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            locationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

        // Notification permission (Android 13+)
        val notifLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted -> notifGranted = granted }

        LaunchedEffect(overlayChecked) {
            if (overlayChecked) {
                locationLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        LaunchedEffect(locationGranted) {
            if (locationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else if (locationGranted) {
                notifGranted = true
            }
        }

        LaunchedEffect(overlayChecked, locationGranted, notifGranted) {
            if (overlayChecked && locationGranted && notifGranted) {
                onAllGranted()
            }
        }
    }

    /** Starts all child background services. Call after permissions are granted. */
    fun startAllServices(context: Context) {
        // FirebaseCommandListener and LocationTracker can start immediately
        context.startForegroundService(Intent(context, FirebaseCommandListener::class.java))
        context.startForegroundService(Intent(context, LocationTracker::class.java))

        // ScreenCaptureService requires a MediaProjection token from the user.
        // The Activity must call MediaProjectionManager.createScreenCaptureIntent()
        // and handle the result before starting this service.
        Log.i(TAG, "Background services started (ScreenCaptureService needs MediaProjection token)")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 5 — MAIN_ACTIVITY NAVIGATION INTEGRATION
// ─────────────────────────────────────────────────────────────────────────────

/*
 * ══════════════════════════════════════════════════════════════════════════════
 * HOW TO WIRE ChildModule.kt INTO YOUR NAVIGATION GRAPH
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * In your MainActivity.kt (or NavGraph.kt), add the following inside your
 * NavHost composable. The "I am a Child" button on the Onboarding screen
 * navigates to the route below, and the ChildDashboardScreen takes over.
 *
 * ── NavGraph.kt ──────────────────────────────────────────────────────────────
 *
 * sealed class Screen(val route: String) {
 *     object Onboarding   : Screen("onboarding")
 *     object ChildDash    : Screen("child_dashboard")
 *     object ParentDash   : Screen("parent_dashboard")
 * }
 *
 * @Composable
 * fun RasFocusNavGraph(navController: NavHostController) {
 *     NavHost(
 *         navController    = navController,
 *         startDestination = Screen.Onboarding.route
 *     ) {
 *
 *         composable(Screen.Onboarding.route) {
 *             OnboardingScreen(
 *                 onChildSelected = {
 *                     navController.navigate(Screen.ChildDash.route) {
 *                         popUpTo(Screen.Onboarding.route) { inclusive = true }
 *                     }
 *                 },
 *                 onParentSelected = {
 *                     navController.navigate(Screen.ParentDash.route) {
 *                         popUpTo(Screen.Onboarding.route) { inclusive = true }
 *                     }
 *                 }
 *             )
 *         }
 *
 *         // ── Child Route ────────────────────────────────────────────────────
 *         composable(Screen.ChildDash.route) {
 *             // Request permissions, then show dashboard
 *             ChildPermissions.RequestAllPermissions(
 *                 onAllGranted = {
 *                     ChildPermissions.startAllServices(LocalContext.current)
 *                 }
 *             )
 *             ChildDashboardScreen()       // <── From ChildModule.kt
 *         }
 *
 *         composable(Screen.ParentDash.route) {
 *             ParentDashboardScreen()      // Your parent module
 *         }
 *     }
 * }
 *
 * ── MainActivity.kt ──────────────────────────────────────────────────────────
 *
 * class MainActivity : ComponentActivity() {
 *
 *     // MediaProjection result launcher — needed for ScreenCaptureService
 *     private val projectionLauncher = registerForActivityResult(
 *         ActivityResultContracts.StartActivityForResult()
 *     ) { result ->
 *         if (result.resultCode == Activity.RESULT_OK && result.data != null) {
 *             val serviceIntent = ScreenCaptureService.buildStartIntent(
 *                 context     = this,
 *                 resultCode  = result.resultCode,
 *                 data        = result.data!!
 *             )
 *             startForegroundService(serviceIntent)
 *         }
 *     }
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         initCloudinary(this)   // Initialize Cloudinary SDK once
 *
 *         setContent {
 *             val navController = rememberNavController()
 *             RasFocusNavGraph(navController = navController)
 *         }
 *     }
 *
 *     // Call this when the user is in the Child persona and grants screen-share permission
 *     fun requestMediaProjection() {
 *         val projManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
 *         projectionLauncher.launch(projManager.createScreenCaptureIntent())
 *     }
 * }
 * ══════════════════════════════════════════════════════════════════════════════
 */
