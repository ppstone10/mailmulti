package org.example.email

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "邮件发送中..."
        }
        setContentView(textView)

        EmailClient.context = this
        val client = EmailClient("dxscmzx2022@163.com", "HFSXbTFFwDnsCDwp")

        // 示例：接收邮件（接收最近 1 封邮件）
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.readLatestEmails(count = 1)

                // 确保接收完成并保存后，再启动发送
                val receivedDir = File(filesDir, "received")
                val yamlFile = File(receivedDir, "test.yml")

                if (!yamlFile.exists()) {
                    runOnUiThread {
                        textView.text = "❌ test.yml 文件不存在"
                    }
                    return@launch
                }

                client.sendEmail(
                    to = "dxscmzx2022@163.com",
                    subject = "Android 测试邮件",
                    body = "这是来自 androidMain 的测试",
                    attachment = yamlFile
                )

                runOnUiThread {
                    textView.text = "✅ 邮件发送成功！"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    textView.text = "❌ 出错：${e.message}"
                }
            }
        }


    }
}