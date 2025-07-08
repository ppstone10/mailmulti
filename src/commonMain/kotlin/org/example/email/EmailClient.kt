package org.example.email

import jdk.jfr.TransitionFrom
import java.io.File

data class EmailMeta(
    val index: Int,
    val subject: String,
    val from: String,
    val data: String
)

//定义了一个 跨平台 Email 客户端类的公共接口
expect class EmailClient (email : String, authCode : String){
    //读取最近5/？封邮件
    suspend fun readLatestEmails(count: Int = 5)
    //根据用户索引下载附件
    suspend fun getRecentEmailSummaries(count: Int = 5): List<EmailMeta>
    suspend fun downloadAttachmentByIndex(index : Int) : List<File>
    //发送邮件，to->目标邮箱 subject->邮件标题 body->邮件正文 attachment->附件
    suspend fun sendEmail(to: String, subject: String, body: String, attachment: File?)
}