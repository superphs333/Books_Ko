package com.example.books_ko

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "정보태그(MyFirebaseMessagingService)"
    }

    /**
     *  [START on_new_token]
    FCM 등록 토큰이 업데이트되면 호출된다(이전 토큰의 보안이 손상된 경우 발생 할 수 있음)
    - FCM등록 토큰이 처음 생성될 때 호출되므로 여기서 토큰을 검색할 수 있다
     */
    override fun onNewToken(token: String) {
        Log.d("정보태그", "onNewToken함수 호출");
        Log.d("정보태그", "Refreshed token: " + token);
    }

    // 메세지를 받았을 때 동작하는 메서드
        /* 메세지 종류
        - 데이터 메세지(클라이언트 앱에서 처리함) -> 자동으로 알림 탭에 표시를 해주지 않기 때문에, 웹앱에 해당 메세지를 처리하는 기능을 작성하지 않으면 사용자는 메세지의 수신 여부 확인x
            받을 때) removeMessage.getData().get("key")
        - 알림 메세지
            받을 때) reoveMessage.getNotification().getBody()
         */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // removeMessage.from = 메세지의 발진자 정보
        Log.d(TAG, "[\"onMessageReceived함수 호출]From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) { // 데이터 페이로드가 포함되어 있는 경우(데이터를 처리해야 하는 경우와 그렇지 않은 경우를 구분하여 처리)
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // 시간이 오래 걸리는 작업 분기
            if (/* Check if data needs to be processed by long running job */ true) { // 시간이 오래 걸리는 작엄(10초 이상)
                scheduleJob()
            } else { // Handle message within 10 secondsㄷㄷ
                handleNow()
            }


            /*
            받아온 정보 변수에 넣어주기
             */
            val map = remoteMessage.data
            showNotification(map)
        }

        // 알림 페이로드가 포함되어있는지 여부
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // 앱에서 FCM 메시지를 수신하여 알림을 생성하고자 하는 경우, 해당 부분에 알림 생성 코드를 추가
    }
    // [END receive_message]

    private fun showNotification(map: Map<String, String>) {
        var intent: Intent? = null

        // putExtra에 보낼 내용 분기
        val sort = map.get("sort")
        var channel_id = ""
        var channel_name = ""
        var remoteviews: RemoteViews? = null
        when (sort) {
//            "For_chatting_room_waiting_list" -> {
//                intent = Intent(this, Activity_Chatting_Room::class.java)
//                intent.putExtra("room_idx", map["room_idx"].toString().toInt())
//                channel_id = getString(R.string.Channel_ID_Chatting_Wating)
//                channel_name = "모임에 참여"
//                remoteviews = getCustomDesign(map)
//            }
            "For_Follow" -> {
                intent = Intent(this, Activity_Management_Follow::class.java)
                channel_id = getString(R.string.Channel_ID_Follow)
                channel_name = "팔로잉"
                remoteviews = getCustomDesign(map)
            }
//            "For_Chatting" -> {
//                intent = Intent(this, Activity_Chatting::class.java)
//                intent.putExtra("room_idx", map["room_idx"].toString().toInt())
//                channel_id = getString(R.string.Channel_ID_Chatting)
//                channel_name = "채팅"
//                remoteviews = getCustomDesign_for_Chatting(map)
//            }
            "For_memo_like" -> {
                intent = Intent(this, Activity_Management_Follow::class.java)
                channel_id = getString(R.string.Channel_ID_Follow)
                channel_name = "메모 좋아요"
                remoteviews = getCustomDesign(map)
            }
            "For_Comment" -> {
                intent = Intent(this, Activity_Management_Follow::class.java) // [개선] 변경해야함
                channel_id = getString(R.string.Channel_ID_MEMO_COMMENT)
                channel_name = "메모댓글"
                remoteviews = getCustomDesign(map)
            }
        }
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

         val pendingIntent = PendingIntent.getActivity(
             this,
             0,
             intent,
             PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
         )

        /*
        알림 생성하기
         */
        var builder = NotificationCompat.Builder(applicationContext,channel_id)
            .setSmallIcon(R.mipmap.ic_launcher) // 작은아이콘 (사용자가 볼 수 있는 유일한 필수 콘텐츠)
            .setAutoCancel(true) // 알람 터치시 자동으로 삭제할 것인지 설정
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true) // 한번만 울리기(중복된 알림은 발생해도 알리지 않음)
            .setContentIntent(pendingIntent)
        // 버전별로 분기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setContent(remoteviews)
        } else {
            builder.setContentTitle(map["title"].toString())
                .setContentText(map["message"].toString())
                .setSmallIcon(R.mipmap.ic_launcher)
        }
        // 알림 채널을 시스템에 등록함
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channel_id,channel_name,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel) // 등록
        }
        notificationManager.notify(0,builder.build()) // 알림 표시

    }

    private fun getCustomDesign(map: Map<String, String>): RemoteViews {
        Log.i("정보태그","[getCustomDesign]map->"+map)
        Log.i("정보태그","[getCustomDesign]packageName->"+applicationContext.packageName)
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.noti_basic)
        //remoteViews.setTextViewText(R.id.noti_title, map["title"])
        remoteViews.setTextViewText(R.id.noti_message, map["message"])
        remoteViews.setImageViewResource(R.id.noti_icon, R.mipmap.ic_launcher)
        return remoteViews
    }




    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
        // [END dispatch_job]
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }
    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
        override fun doWork(): Result {
            // TODO(developer): add long running task here.
            return Result.success()
        }
    }


    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("FCM Message")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }




}