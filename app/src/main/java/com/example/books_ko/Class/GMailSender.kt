package com.example.books_ko.Class

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.PasswordAuthentication
import java.util.*
import javax.activation.DataHandler

import javax.activation.DataSource

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage



class GMailSender : javax.mail.Authenticator(

) {
    private val mailhost = "smtp.gmail.com"
    private var user = "lee333dan@gmail.com"
    private var password = "hipulkxqivsomwou"
    private var session: Session? = null
    private var emailCode: String? = null

    init {
        this.user = user
        this.password = password
        emailCode = createEmailCode()
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.host", mailhost)
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")

        //구글에서 지원하는 smtp 정보를 받아와 MimeMessage 객체에 전달해준다.
        session = Session.getDefaultInstance(props, this)
    }

    //생성된 이메일 인증코드 반환
    fun getEmailCode(): String? {
        return emailCode
    }

    private fun createEmailCode(): String? { //이메일 인증코드 생성
        val str = arrayOf(
            "a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "1", "2",
            "3", "4", "5", "6", "7", "8", "9"
        )
        var newCode = String()
        for (x in 0..7) {
            val random = (Math.random() * str.size).toInt()
            newCode += str[random]
        }
        return newCode
    }


    override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication? {
        //해당 메서드에서 사용자의 계정(id & password)을 받아 인증받으며 인증 실패시 기본값으로 반환됨.
        return javax.mail.PasswordAuthentication(user, password)
    }

    @Synchronized
    @Throws(Exception::class)
    fun sendMail(
        subject: String?, body: String, recipients: String
    ) {
        val message = MimeMessage(session)
        val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
        //본문 내용을 byte단위로 쪼개어 전달
        message.setSender(InternetAddress(user)) //본인 이메일 설정
        message.setSubject(subject) //해당 이메일의 본문 설정
        message.setDataHandler(handler)
        if (recipients.indexOf(',') > 0) message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(recipients)
        ) else message.setRecipient(
            Message.RecipientType.TO, InternetAddress(recipients)
        )
        Transport.send(message) //메시지 전달
    }

    class ByteArrayDataSource(private val data: ByteArray, private var type: String? = null) : DataSource {

        override fun getContentType(): String {
            return type ?: "application/octet-stream"
        }

        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(data)
        }

        override fun getName(): String {
            return "ByteArrayDataSource"
        }

        @Throws(IOException::class)
        override fun getOutputStream(): OutputStream {
            throw IOException("Not Supported")
        }

        fun setType(type: String?) {
            this.type = type
        }
    }
}