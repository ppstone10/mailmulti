package org.example.email

import android.content.Context
import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.*
import com.sun.mail.imap.IMAPStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//emailClient的Android端特定实现，主要依赖JavaMail API和Android本地文件系统
actual class EmailClient actual constructor(
    private val email: String,
    private val authCode: String
) {
    private lateinit var cachedMessages: List<Message>
    actual suspend fun getRecentEmailSummaries(count: Int): List<EmailMeta> = withContext(Dispatchers.IO) {
        val session = createSession(false)
        val store = session.getStore("imaps") as IMAPStore
        store.connect("imap.163.com", 993, email, authCode)
        store.id(mapOf("name" to "AndroidMail", "version" to "1.0"))
        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_ONLY)
        val messages = folder.messages.takeLast(count)
        cachedMessages = messages

        messages.mapIndexed { i, msg ->
            EmailMeta(i, msg.subject ?: "(无标题)", msg.from.firstOrNull()?.toString() ?: "?", msg.sentDate.toString())
        }
    }

    actual suspend fun downloadAttachmentByIndex(index: Int): List<File> = withContext(Dispatchers.IO) {
        val msg = cachedMessages.getOrNull(index) ?: return@withContext emptyList()
        val parts = msg.content as? Multipart ?: return@withContext emptyList()

        val savedFiles = mutableListOf<File>()
        for (i in 0 until parts.count) {
            val part = parts.getBodyPart(i)
            if (Part.ATTACHMENT.equals(part.disposition, true)) {
                val name = part.fileName ?: "unnamed.txt"
                val file = File(EmailClient.context.filesDir, "received/$name").apply {
                    parentFile?.mkdirs()
                    outputStream().use { out -> part.inputStream.copyTo(out) }
                }
                savedFiles.add(file)
            }
        }
        savedFiles
    }
    //Android 端需要访问文件存储路径，比如：context.filesDir/received ，用 context 来获取 Android 应用内部目录
    //需在 Activity 中设置 EmailClient.context = this
    companion object {
        lateinit var context: Context
    }

    private fun createSession(isSmtp: Boolean): Session {
        val props = Properties().apply {
            if (isSmtp) {
                //Transport.send() 内部会根据这个配置连接端口，无需显式写 connect(host, port, ...)
                put("mail.smtp.host", "smtp.163.com")
                put("mail.smtp.port", "465")
                put("mail.smtp.auth", "true")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            } else {
                //IMAP的端口配置：默认是在 connect(...) 中定义的，不像SMTP自动读取端口
                put("mail.store.protocol", "imaps")
                put("mail.imap.ssl.enable", "true")
                put("mail.imap.ssl.protocols", "TLSv1.2 TLSv1.3")
            }
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, authCode)
            }
        })
    }

    actual suspend fun sendEmail(to: String, subject: String, body: String, attachment: File?) =
        withContext(Dispatchers.IO) {
            val session = createSession(true)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject, Charsets.UTF_8.name())
            }

            if (attachment == null) {
                message.setText(body, Charsets.UTF_8.name())
            } else {
                val multipart = MimeMultipart()

                val textPart = MimeBodyPart().apply {
                    setText(body, Charsets.UTF_8.name())
                }
                multipart.addBodyPart(textPart)

                val filePart = MimeBodyPart().apply {
                    dataHandler = DataHandler(FileDataSource(attachment))
                    fileName = MimeUtility.encodeText(attachment.name)
                    disposition = Part.ATTACHMENT
                }
                multipart.addBodyPart(filePart)

                message.setContent(multipart)
            }

            //使用 session 的配置，自动处理 message 的发送,是一个高级封装函数
            Transport.send(message)
        }

    actual suspend fun readLatestEmails(count: Int) = withContext(Dispatchers.IO) {
        val session = createSession(false)
        val store = session.getStore("imaps") as IMAPStore
        store.connect("imap.163.com", 993, email, authCode)
        store.id(mapOf("name" to "AndroidMail", "version" to "1.0"))

        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_ONLY)

        folder.messages.takeLast(count).forEach { msg ->
            val multipart = msg.content as? Multipart
            multipart?.let {
                for (i in 0 until it.count) {
                    val part = it.getBodyPart(i)
                    if (Part.ATTACHMENT.equals(part.disposition, true)) {
                        val filename = part.fileName ?: "unknown.yml"
                        val saveDir = File(context.filesDir, "received")
                        val file = File(saveDir, filename)
                        file.parentFile.mkdirs()
                        part.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
                    }
                }
            }
        }

        folder.close(false)
        store.close()
    }
}