package com.example.books_ko

import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.databinding.ActivityBookUrlBinding

class Activity_Book_URL : AppCompatActivity() {

    private lateinit var binding: ActivityBookUrlBinding
    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView(intent.getStringExtra("url")!!)
    }

    private fun initWebView(url: String) {
        binding.webView.apply {
            settings.run {
                loadWithOverviewMode = true // 페이지를 축소하여 전체 페이지를 보여줌
                useWideViewPort = true // 페이지의 초기 scale을 1로 설정하여 전체 페이지를 화면에 맞게 축소
                javaScriptEnabled = true // javascript 허용
                javaScriptCanOpenWindowsAutomatically = true // javascript의 window.open허용
                domStorageEnabled = true // 로컬저장소에서 허용할지 여부
                supportMultipleWindows() // 여러개의 윈도우를 지원(새 창을 열 때 새 WebView를 생성하지 않고도 기존 WebView에서 새 창을 열 수 있다)
            }
            webChromeClient = WebChromeClient() // web client 를 chrome 으로 설정
            webViewClient = SslWebViewConnect()

            Log.i("정보태그","url->$url")
            loadUrl(url!!)
        }
    }

    class SslWebViewConnect : WebViewClient() {
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.proceed() // SSL 에러가 발생해도 계속 진행!
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true //응용프로그램이 직접 url를 처리함
        }
    }
}
