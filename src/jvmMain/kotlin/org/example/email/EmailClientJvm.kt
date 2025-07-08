package org.example.email

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPStore
import jakarta.activation.DataHandler
import jakarta.activation.FileDataSource
import jakarta.mail.*
import jakarta.mail.internet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

//emailClient的jvm特定实现、主要依赖JavaMail API（Jakarta Mail）和Jakarta Activation
actual class EmailClient actual constructor (
    private val email: String, //邮箱账号
    private val authCode: String //邮箱授权码
) {
    private lateinit var cachedMessages: List<Message>

    //创建会话对象，根据isStmp传递判断启用接收(IMAP)还是发送(SMTP)配置
    private fun createSession(isSmtp: Boolean): Session {
        val props = Properties().apply {
            if (isSmtp) {
                put("mail.smtp.host", "smtp.163.com") //网易邮箱的 SMTP 地址
                put("mail.smtp.port", "465") //SMTPS 加密端口
                put("mail.smtp.auth", "true") //需要认证
                put("mail.smtp.ssl.enable", "true") //启用 SSL
                put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3") //安全协议版本，避免安全报错
            } else {
                put("mail.store.protocol", "imaps") //使用 IMAPS 协议收信（加密的 IMAP）
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

    //读取邮件
    actual suspend fun readLatestEmails(count: Int) {
        //创建会话
        val session = createSession(false)
        //连接邮箱
        val store = (session.getStore("imaps") as IMAPStore).apply {
            connect("imap.163.com", 993, email, authCode)
            // 防止网易报 Unsafe Login 错误
            id(mapOf("name" to "MailDemo", "version" to "1.0"))
        }

        //打开收件箱文件夹
        val inbox = (store.getFolder("INBOX") as IMAPFolder).apply { open(Folder.READ_ONLY) }
        //读取最近 count 封邮件
        inbox.messages.takeLast(count).forEach { msg ->
            //判断邮件是否是 Multipart（通常带附件的邮件是）
            val multipart = msg.content as? Multipart
            multipart?.let {
                for (i in 0 until it.count) {
                    val part = it.getBodyPart(i)
                    //如果该部分是附件（disposition = ATTACHMENT），则提取文件名保存到本地目录
                    if (Part.ATTACHMENT.equals(part.disposition, true)) {
                        val filename = part.fileName ?: "unknown.yml"
                        val saveTo = File("received/$filename")
                        saveTo.parentFile.mkdirs()
                        part.inputStream.use { input -> saveTo.outputStream().use { input.copyTo(it) } }
                    }
                }
            }
        }

        //释放资源
        inbox.close(false)
        store.close()
    }

    //to指向目标邮箱，subject为邮件标题，text为邮件正文，attachment为邮件附件
    actual suspend fun sendEmail(
        to: String,
        subject: String,
        body: String,
        attachment: File?
    ) {
        val session = createSession(true)

        //设置发件人、收件人、主题（UTF-8 编码，避免乱码）
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(email))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, Charsets.UTF_8.name())
        }

        if (attachment == null) {
            // 简单文本邮件
            message.setText(body, Charsets.UTF_8.name())
        } else {
            // 创建邮件体和附件的复合结构
            val multipart = MimeMultipart()

            // 1. 正文部分
            val textPart = MimeBodyPart().apply {
                setText(body, Charsets.UTF_8.name())
            }
            multipart.addBodyPart(textPart)

            // 2. 附件部分
            val filePart = MimeBodyPart().apply {
                dataHandler = DataHandler(FileDataSource(attachment))
                fileName = MimeUtility.encodeText(attachment.name) // 防中文乱码
                disposition = Part.ATTACHMENT
            }
            multipart.addBodyPart(filePart)

            // 设置整体内容
            message.setContent(multipart)
        }

        // 发送邮件
        Transport.send(message)
        println("✅ 邮件已成功发送至 $to，包含附件：${attachment?.name ?: "无"}")
    }

    //获取最近5/？邮件
    actual suspend fun getRecentEmailSummaries(count: Int): List<EmailMeta> = withContext(Dispatchers.IO) {
        //创建会话
        val session = createSession(false)
        //连接邮箱
        val store = (session.getStore("imaps") as IMAPStore).apply {
            connect("imap.163.com", 993, email, authCode)
            // 防止网易报 Unsafe Login 错误
            id(mapOf("name" to "MailDemo", "version" to "1.0"))
        }
        //打开收件箱文件夹
        val inbox = store.getFolder("INBOX").apply { open(Folder.READ_ONLY) }
        val messages = inbox.messages.takeLast(count)
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
                val file = File("received", name).apply {
                    parentFile.mkdirs()
                    outputStream().use { out -> part.inputStream.copyTo(out) }
                }
                savedFiles.add(file)
            }
        }
        savedFiles
    }
}