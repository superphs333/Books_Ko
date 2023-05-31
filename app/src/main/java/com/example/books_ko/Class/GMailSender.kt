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


// Gmail을 통해 이메일을 보내는 기능을 구현한 클래스(JavaMail API를 사용하여 이메일을 보내는 기능을 제공)
class GMailSender : javax.mail.Authenticator(
    // javax.mail.Authenticator를 상속하여 이메일 계정의 인증 정보를 제공
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

    // 이메일 인증 코드를 생성하는 메서드
        // 주어진 문자 및 숫자 배열에서 무작위로 선택하여 8자리 코드를 생성
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


    // 사용자의 Gmail계정 인증 정보를 반환
    override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication? {
        //해당 메서드에서 사용자의 계정(id & password)을 받아 인증받으며 인증 실패시 기본값으로 반환됨.
        return javax.mail.PasswordAuthentication(user, password)
    }

    // 이메일을 보내는 메서드
    @Synchronized
    @Throws(Exception::class)
    fun sendMail(
        subject: String?, body: String, recipients: String
    ) {
        // 주어진 제목, 본문 내용, 수신자 이메일 주소를 기반으로 MimeMessage 객체를 생성
        val message = MimeMessage(session)
        val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
        //본문 내용을 byte단위로 쪼개어 전달
        message.setSender(InternetAddress(user)) //본인 이메일 설정
        message.setSubject(subject) //해당 이메일의 본문 설정
        message.setDataHandler(handler)
        // 이메일 수신자 설정
        if (recipients.indexOf(',') > 0) message.setRecipients( // 여러 수신자
            Message.RecipientType.TO, InternetAddress.parse(recipients)
        ) else message.setRecipient( // 단일 수신자
            Message.RecipientType.TO, InternetAddress(recipients)
        )
        Transport.send(message) //메시지 전달
    }

    // 바이트 배열을 데이터로 사용하는 데이터 소스 (이메일 본문 내용을 byte 단위로 쪼개어 전달하기 위해)
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