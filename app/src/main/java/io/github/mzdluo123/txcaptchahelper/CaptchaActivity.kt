package io.github.mzdluo123.txcaptchahelper

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_captcha.*

class CaptchaActivity : AppCompatActivity() {
    companion object{
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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webview.evaluateJavascript(
                    """
                    mqq.invoke = function(a,b,c){ return bridge.invoke(a,b,JSON.stringify(c))}"""
                        .trimIndent()
                ) {}
            }
        }
        WebView.setWebContentsDebuggingEnabled(true)
        webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webview.addJavascriptInterface(Bridge(),"bridge")
    }

    private fun authFinish(ticket:String){
        val intent = Intent().putExtra("ticket",ticket)
        setResult(RESULT_OK,intent)
        finish()
    }

    inner class Bridge {
        @JavascriptInterface
        fun invoke(cls: String?, method: String?, data: String?) {

            if (data != null) {
                val jsData = JsonParser.parseString(data)
                if (method == "onVerifyCAPTCHA") {
                    authFinish(jsData.asJsonObject["ticket"].asString)
                }
            }
        }
    }
}