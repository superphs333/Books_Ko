package com.example.books_ko.Function

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object AboutPicture {

    // 카메라에서 사진 가져오기
        // return : 파일 경로
    fun cameraOnePicture(resultLauncher: ActivityResultLauncher<Intent>,context: Context): String {
        // 카메라에서 이미지를 가져옴
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolveActivity = intent.resolveActivity(context.packageManager)
        if(resolveActivity !== null){ // 설치 되어 있는 경우
            // 1. 임의의 경로에 파일 만들기
            var photo_File: File? = null
            try{
                photo_File = createImageFile(context);
            }catch (e:Exception){
                e.printStackTrace()
                Log.d("정보태그", "createImageFile 오류=>" + e.message)
            }
            // 2. FileProvider를 통해서 파일의 uri값을 만든다(이런식으로 했을 때 onActivity도달 가능)
            if(photo_File!=null){
                val photoUri = FileProvider.getUriForFile(context,context.packageName,photo_File)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                resultLauncher.launch(intent)
                return photo_File.absolutePath;
            }
        }else{ // 설치가 되어 있지 않은 경우
            Toast.makeText(
                context, "실행 할 수 있는 앱이 없습니다. 카메라 어플을 설치해주세요", Toast.LENGTH_SHORT
            ).show()
        }
        return ""
    }

    /*
    임시파일 변환하기
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // 1. String prefix 부분
        val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "Profile_$timeStamp"+"_"
        // 2. directory 부분(file)
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // 3. 임시 파일 생성
        val image = File.createTempFile(imageFileName,".jpg",storageDir)
        return image
    }

    fun gallery_one_picture(resultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent()
        intent.type = "image/*"
        // 이미지를 열 수 있는 앱을 호출
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

}