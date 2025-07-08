import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.example.email.EmailClient
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JOptionPane

class EmailClientJvmTest {
    @Test
    fun testSendAndReceive() = runBlocking {
        val client = EmailClient("dxscmzx2022@163.com", "HFSXbTFFwDnsCDwp")
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val formattedTime = currentTime.format(formatter)
        client.sendEmail("dxscmzx2022@163.com", "桌面端项目文件$formattedTime", "桌面端项目yaml文件", File("sending/test1.yml"))
        client.readLatestEmails(1)
    }

    @Test
    fun main() = runBlocking {
        val client = EmailClient("dxscmzx2022@163.com", "HFSXbTFFwDnsCDwp")

        val summaries = client.getRecentEmailSummaries(5)
        val subjects = summaries.mapIndexed { i, it ->
            "[$i] ${it.subject.take(60)}"
        }.toTypedArray()

        val selectedIndex = JOptionPane.showInputDialog(
            null,
            "选择一封邮件下载附件：",
            "📥 邮件选择",
            JOptionPane.PLAIN_MESSAGE,
            null,
            subjects,
            subjects.firstOrNull()
        )?.let { chosen ->
            subjects.indexOf(chosen)
        }

        if (selectedIndex != null && selectedIndex >= 0) {
            val files = client.downloadAttachmentByIndex(selectedIndex)
            if (files.isNotEmpty()) {
                JOptionPane.showMessageDialog(null, "✅ 附件已保存：\n${files.joinToString("\n") { it.name }}")
            } else {
                JOptionPane.showMessageDialog(null, "⚠️ 没有找到附件")
            }
        } else {
            println("用户取消或未选择")
        }
    }
}