package org.example.email

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmailClient.context = this

        val textView = TextView(this).apply {
            text = "📩 加载最近邮件中..."
        }
        setContentView(textView)

        CoroutineScope(Dispatchers.Main).launch {
            val client = EmailClient("dxscmzx2022@163.com", "HFSXbTFFwDnsCDwp")

            try {
                val summaries = client.getRecentEmailSummaries(5)
                val subjects = summaries.mapIndexed { i, it ->
                    "[$i] ${it.subject.take(50)}"
                }.toTypedArray()

                AlertDialog.Builder(this@TestActivity)
                    .setTitle("选择一封邮件下载附件")
                    .setItems(subjects) { _, which ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val files = client.downloadAttachmentByIndex(which)
                            runOnUiThread {
                                textView.text = if (files.isNotEmpty())
                                    "✅ 下载完成：${files.joinToString("\n") { it.name }}"
                                else
                                    "⚠️ 没有找到附件"
                            }
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            } catch (e: Exception) {
                textView.text = "❌ 出错：${e.message}"
                e.printStackTrace()
            }
        }
    }
}