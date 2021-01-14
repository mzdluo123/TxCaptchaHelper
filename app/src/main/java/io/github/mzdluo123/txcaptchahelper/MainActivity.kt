package io.github.mzdluo123.txcaptchahelper

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
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
                Toast.makeText(this, "请填写URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivityForResult(
                Intent(this, CaptchaActivity::class.java).putExtra("url", url),
                REQUEST_CODE_SLIDE
            )
        }
        scan_btn.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION);
            } else {
//                startActivity(Intent(this, CaptchaActivity::class.java))
                startActivityForResult(Intent(this, CaptureActivity::class.java), REQUEST_CODE_QRSCAN)

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SLIDE) {
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
            return
        }
        if (requestCode == REQUEST_CODE_QRSCAN){
            val data = CameraScan.parseScanResult(data)
            startActivityForResult(
                Intent(this, CaptchaActivity::class.java).putExtra("url", data),
                REQUEST_CODE_SLIDE
            )
            return
        }
        if (requestCode == REQUEST_CODE_PERMISSION){
            startActivityForResult(Intent(this, CaptureActivity::class.java), REQUEST_CODE_QRSCAN)
        }
    }
}