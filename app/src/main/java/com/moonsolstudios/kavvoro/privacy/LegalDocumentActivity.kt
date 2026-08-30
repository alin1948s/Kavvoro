package com.moonsolstudios.kavvoro.privacy

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.core.text.htmlEncode
import java.io.IOException

/**
 * Full-screen in-game document surface for the canonical legal pages and studio About page.
 *
 * Legal copy remains owned by the public MoonSol Studios site. The same canonical /web tree is
 * packaged as Android assets, which keeps the in-game disclosure identical and available offline.
 * External links (Google policies, Play Store and email) are handed to Android.
 */
@Suppress("LockedOrientationActivity", "SourceLockedOrientationActivity")
class LegalDocumentActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var titleView: TextView
    private var page: LegalDocumentPage = LegalDocumentPage.PRIVACY
    private var suppressExternalLinksUntil = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        page = intent.getStringExtra(EXTRA_PAGE)
            ?.let { value -> LegalDocumentPage.entries.firstOrNull { it.name == value } }
            ?: LegalDocumentPage.PRIVACY

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 9, 15))
        }
        root.addView(
            createToolbar(),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, TOOLBAR_HEIGHT_DP.dp())
        )
        webView = createWebView()
        root.addView(
            webView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        setContentView(root)
        loadPage(page)
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        if (::webView.isInitialized) webView.onResume()
    }

    override fun onPause() {
        if (::webView.isInitialized) webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView = WebView(this).apply {
        setBackgroundColor(Color.rgb(7, 9, 15))
        settings.javaScriptEnabled = false
        settings.domStorageEnabled = false
        settings.allowFileAccess = true
        settings.allowContentAccess = false
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.setSupportZoom(false)
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.scheme.equals("file", ignoreCase = true)) {
                    return handleLocalLink(request.url)
                }
                return handleLink(request.url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.toUri().scheme.equals("file", ignoreCase = true)) {
                    return handleLocalLink(url.toUri())
                }
                return handleLink(url.toUri())
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) showLoadError(page.url.orEmpty())
            }
        }
    }

    private fun createToolbar(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(12.dp(), 0, 12.dp(), 0)
        setBackgroundColor(Color.rgb(9, 13, 20))

        addView(TextView(this@LegalDocumentActivity).apply {
            text = "‹"
            textSize = 34f
            setTextColor(Color.rgb(69, 242, 255))
            gravity = Gravity.CENTER
            isClickable = true
            setOnClickListener { finish() }
            contentDescription = "Back"
        }, LinearLayout.LayoutParams(44.dp(), LinearLayout.LayoutParams.MATCH_PARENT))

        titleView = TextView(this@LegalDocumentActivity).apply {
            textSize = 18f
            setTextColor(Color.rgb(247, 244, 255))
            gravity = Gravity.CENTER_VERTICAL
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(8.dp(), 0, 8.dp(), 0)
        }
        addView(titleView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

        if (page != LegalDocumentPage.ABOUT) {
            addView(toolbarButton("PRIVACY") { loadPage(LegalDocumentPage.PRIVACY) })
            addView(toolbarButton("TERMS") { loadPage(LegalDocumentPage.TERMS) })
            addView(toolbarButton("DATA") { loadPage(LegalDocumentPage.DATA_DELETION) })
        }
    }

    private fun toolbarButton(label: String, action: () -> Unit): TextView = TextView(this).apply {
        text = label
        textSize = 10f
        setTextColor(Color.rgb(185, 194, 208))
        gravity = Gravity.CENTER
        setPadding(6.dp(), 0, 6.dp(), 0)
        isClickable = true
        setOnClickListener { action() }
    }

    private fun loadPage(nextPage: LegalDocumentPage) {
        page = nextPage
        titleView.text = nextPage.title
        // A tap on a Settings row can finish on the newly created WebView after the
        // Activity transition. Ignore that one stale gesture so it cannot activate
        // the first Play Store/mail link in the About document.
        suppressExternalLinksUntil = SystemClock.uptimeMillis() + LINK_GESTURE_GUARD_MS
        if (nextPage == LegalDocumentPage.ABOUT) {
            webView.loadDataWithBaseURL(
                "https://brainroot-chaos-kavaroo.web.app/about/",
                aboutHtml(),
                "text/html",
                "UTF-8",
                null
            )
        } else {
            loadBundledLegalPage(nextPage)
        }
    }

    private fun loadBundledLegalPage(nextPage: LegalDocumentPage) {
        val assetPath = when (nextPage) {
            LegalDocumentPage.PRIVACY -> "privacy/index.html"
            LegalDocumentPage.TERMS -> "terms/index.html"
            LegalDocumentPage.DATA_DELETION -> "data-deletion/index.html"
            LegalDocumentPage.ABOUT -> return
        }
        val html = try {
            assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (_: IOException) {
            showLoadError(nextPage.url.orEmpty())
            return
        }

        // The website uses root-relative routes. Rewrite only those routes for the
        // android_asset base URL; all legal text and external links stay unchanged.
        val localHtml = html
            .replace("href=\"/assets/legal.css\"", "href=\"../assets/legal.css\"")
            .replace("src=\"/assets/", "src=\"../assets/")
            .replace("href=\"/privacy/\"", "href=\"../privacy/index.html\"")
            .replace("href=\"/terms/\"", "href=\"../terms/index.html\"")
            .replace("href=\"/data-deletion/\"", "href=\"../data-deletion/index.html\"")
            .replace("href=\"/\"", "href=\"../index.html\"")
        val baseUrl = "file:///android_asset/${assetPath.substringBeforeLast('/')}/"
        webView.loadDataWithBaseURL(baseUrl, localHtml, "text/html", "UTF-8", nextPage.url)
    }

    private fun handleLocalLink(uri: Uri): Boolean {
        val path = uri.path.orEmpty()
        val target = when {
            path.endsWith("/privacy/index.html") -> LegalDocumentPage.PRIVACY
            path.endsWith("/terms/index.html") -> LegalDocumentPage.TERMS
            path.endsWith("/data-deletion/index.html") -> LegalDocumentPage.DATA_DELETION
            else -> null
        }
        if (target != null) {
            loadPage(target)
            return true
        }
        return false
    }

    private fun handleLink(uri: Uri): Boolean {
        if (SystemClock.uptimeMillis() < suppressExternalLinksUntil) return true
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()
        val isOfficialPage = scheme == "https" && host == OFFICIAL_HOST
        if (isOfficialPage) return false

        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No app can open this link.", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun showLoadError(url: String) {
        val escapedUrl = url.htmlEncode()
        webView.loadDataWithBaseURL(
            null,
            """
            <!doctype html><html><head><meta name=viewport content='width=device-width, initial-scale=1'>
            <style>body{background:#07090f;color:#f7f4ff;font:16px/1.6 sans-serif;padding:28px}h1{color:#45f2ff}p{color:#b9c2d0}a{color:#45f2ff}</style>
            </head><body><h1>Document unavailable</h1><p>Connect to the internet and try again to read the current MoonSol Studios document.</p><p><a href='$escapedUrl'>Retry</a></p></body></html>
            """.trimIndent(),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun aboutHtml(): String = """
        <!doctype html><html lang='en'><head><meta name='viewport' content='width=device-width, initial-scale=1'>
        <style>
        :root{color-scheme:dark;--bg:#07090f;--panel:#0e141e;--panel2:#111b29;--text:#f7f4ff;--muted:#b9c2d0;--cyan:#45f2ff;--pink:#ff4d8d;--gold:#ffcf4a;--line:#273344}
        *{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:16px/1.6 Arial,sans-serif;padding:24px 18px 42px}main{max-width:720px;margin:0 auto}
        .eyebrow{margin:0;color:var(--pink);font-size:12px;font-weight:800;letter-spacing:.12em;text-transform:uppercase}.hero h1{margin:4px 0 8px;font-size:34px}.hero p{margin:0;color:var(--muted)}
        section{margin-top:28px;padding-top:24px;border-top:1px solid var(--line)}h2{margin:0 0 10px;font-size:22px}h3{margin:0;color:var(--text);font-size:19px}.muted{color:var(--muted)}.card{margin:14px 0;padding:18px;border:1px solid var(--line);border-radius:14px;background:linear-gradient(145deg,var(--panel2),var(--panel))}.card p{color:var(--muted);margin:8px 0}.button{display:inline-block;margin-top:8px;padding:10px 14px;border:1px solid var(--cyan);border-radius:8px;background:#102630;color:var(--text);font-weight:700;text-decoration:none}.button.disabled{border-color:var(--line);background:#151a25;color:#8792a2}.contact{color:var(--cyan)}
        </style></head><body><main>
        <div class='hero'><p class='eyebrow'>Independent game studio</p><h1>MoonSol Studios</h1><p>We create original mobile games with bold worlds, expressive characters and satisfying gameplay moments.</p></div>
        <section><h2>Our games</h2>
          <article class='card'><h3>Brainrot Chaos: Kavvoro</h3><p>A fast one-touch physics arcade game where every tap bends an unstable Rift. Collect Brainballs, survive moving hazards and build your streak.</p><a class='button' href='https://play.google.com/store/apps/details?id=com.moonsolstudios.kavvoro'>Download on Google Play</a></article>
          <article class='card'><h3>Kavvoro Rift Lab</h3><p>Our next game in the Kavvoro universe. More details and its store listing will be published when the game is ready.</p><span class='button disabled'>Coming soon to Google Play</span></article>
          <article class='card'><h3>More MoonSol Studios games</h3><p>Additional original games are in development. Store pages will appear here as each title is announced.</p><span class='button disabled'>Coming soon to Google Play</span></article>
        </section>
        <section><h2>Platforms</h2><p class='muted'>Our games are being developed for Android and will also be available on iOS. Availability and launch timing may differ by title.</p></section>
        <section><h2>Stay in touch</h2><p class='muted'>For support, privacy or legal questions, contact <a class='contact' href='mailto:moonsolstudios@gmail.com'>moonsolstudios@gmail.com</a>.</p><p class='muted'>© 2026 MoonSol Studios.</p></section>
        </main></body></html>
    """.trimIndent()

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.decorView.windowInsetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val TOOLBAR_HEIGHT_DP = 52
        private const val LINK_GESTURE_GUARD_MS = 750L
        private const val OFFICIAL_HOST = "brainroot-chaos-kavaroo.web.app"
        const val EXTRA_PAGE = "com.moonsolstudios.kavvoro.extra.LEGAL_PAGE"

        fun intent(activity: android.content.Context, page: LegalDocumentPage): Intent =
            Intent(activity, LegalDocumentActivity::class.java).putExtra(EXTRA_PAGE, page.name)
    }
}
