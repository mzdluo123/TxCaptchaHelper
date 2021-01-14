package io.github.mzdluo123.txcaptchahelper

import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 1
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
                Toast.makeText(this, "请填写URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivityForResult(
                Intent(this, CaptchaActivity::class.java).putExtra("url", url),
                REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (data == null) {
                return
            }
            val ticket = data.getStringExtra("ticket")
            if (ticket == null) {
                Toast.makeText(this, "出现未知错误，请联系开发者", Toast.LENGTH_SHORT).show()
                return
            }
            AlertDialog.Builder(this).setTitle("Ticket").setMessage(ticket).setPositiveButton("复制",
                DialogInterface.OnClickListener { dialog, which ->
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val data = ClipData.newPlainText(null, ticket)
                    clipboardManager.setPrimaryClip(data)
                    Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
                }).show()
        }

    }
}