package io.github.mzdluo123.txcaptchahelper

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_captcha.*

class CaptchaActivity : AppCompatActivity() {
    companion object {
        const val RESULT_OK = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captcha)
        initWebView()
        webview.loadUrl(intent.getStringExtra("url"))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return onJsBridgeInvoke(request!!.url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                return onJsBridgeInvoke(Uri.parse(url))
            }
        }
        WebView.setWebContentsDebuggingEnabled(true)
        webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
    }

    private fun onJsBridgeInvoke(request: Uri): Boolean {
        if (request.path.equals("/onVerifyCAPTCHA")) {
            val p = request.getQueryParameter("p")
            val jsData = JsonParser.parseString(p).asJsonObject
            authFinish(jsData["ticket"].asString)
        }
        return false
    }

    private fun authFinish(ticket: String) {
        val intent = Intent().putExtra("ticket", ticket)
        setResult(RESULT_OK, intent)
        finish()
    }
}
