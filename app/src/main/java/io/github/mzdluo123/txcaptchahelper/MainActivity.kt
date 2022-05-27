package io.github.mzdluo123.txcaptchahelper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var code: Int = 0
    private val client = OkHttpClient()

    companion object {
        const val REQUEST_CODE_SLIDE = 1
        const val REQUEST_CODE_QRSCAN = 2
        const val REQUEST_CODE_PERMISSION = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clean_btn.setOnClickListener {
            url_edit_text.text.clear()
        }
        next_btn.setOnClickListener {
            val url = url_edit_text.text.toString()
            if (url.isEmpty()) {
                Toast.makeText(this, "请填写URL或请求码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            kotlin.runCatching {
                code = url.toInt()
                processOnlineCode()
            }.onFailure {
                toCaptchaActivity(url)
            }

        }
        scan_btn.setOnClickListener {
            startActivityForResult(
                Intent(this, CaptureActivity::class.java),
                REQUEST_CODE_QRSCAN
            )
        }
        proj_location.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/mzdluo123/TxCaptchaHelper")
                )
            )
        }
    }

    private fun processOnlineCode() {
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("请稍后")
                .setMessage("正在获取信息")
                .setCancelable(false)
                .create()
        dialog.show()
        client.newCall(
            Request.Builder().url(BuildConfig.OnlineService + code).get().build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    dialog.dismiss()
                    if (response.code == 200) {
                        val url = response.body!!.string()
                        toCaptchaActivity(url)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "请求错误：" + response.code,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun submitTicket(ticket: String) {
        val alert =
            AlertDialog.Builder(this).setTitle("请稍后").setMessage("正在提交").setCancelable(false)
                .create()
        alert.show()
        client.newCall(
            Request.Builder().url(BuildConfig.OnlineService + "finish/" + code).post(
                FormBody.Builder().add("ticket", ticket).build()
            ).build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    alert.dismiss()
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onResponse(call: Call, response: Response) {

                runOnUiThread {
                    alert.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "提交成功，请在PC端再次发起请求得到ticket",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        })

    }

    private fun toCaptchaActivity(url: String) {
        startActivityForResult(
            Intent(this, CaptchaActivity::class.java).putExtra("url", url),
            REQUEST_CODE_SLIDE
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (requestCode == REQUEST_CODE_SLIDE) {
            val ticket = data.getStringExtra("ticket")
            if (ticket == null) {
                Toast.makeText(this, "出现未知错误，请联系开发者", Toast.LENGTH_SHORT).show()
                return
            }
            if (code != 0) {
                submitTicket(ticket)
                return
            }
            AlertDialog.Builder(this).setTitle("Ticket").setMessage(ticket).setPositiveButton(
                "复制"
            ) { _, _ ->  // dialog, which
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val result = ClipData.newPlainText(null, ticket)
                clipboardManager.setPrimaryClip(result)
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
            }.show()
            return
        }

        if (requestCode == REQUEST_CODE_QRSCAN) {
            val result = CameraScan.parseScanResult(data)
            toCaptchaActivity(result!!)
        } else if (requestCode == REQUEST_CODE_PERMISSION) {
            startActivityForResult(Intent(this, CaptureActivity::class.java), REQUEST_CODE_QRSCAN)
        }
    }
}
