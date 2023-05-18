package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Activity_Add_Comment
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Data.SliderItem
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.Function.SliderAdapterT1
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemBookMemoBinding
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterBookMemo (
    var dataList: ArrayList<Data_Book_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    var email : String // 사용자의 이메일
) : RecyclerView.Adapter<AdapterBookMemo.CustomViewHolder>(){

    lateinit var binding : ItemBookMemoBinding
    val fc = FunctionCollection

    inner class CustomViewHolder(val binding: ItemBookMemoBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemBookMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]

//        val testemail = email
//
//        if(item.follow){
//            Log.i("정보태그","follow")
//        }else if(item.email == email){
//            Log.i("정보태그","자기자신")
//        }else{
//            Log.i("정보태그","follow아님")
//        }

        /*
        데이터 셋팅
         */
        // 프로필 이미지 셋팅 [개선] 필요
        Glide.with(holder.itemView.context)
            .load(dataList.get(position).profileUrl)
            .into(binding.imgProfile)
        binding.txtNickname.text = item.nickname // 닉네임
        binding.txtFollow.visibility = if (item.follow==1 || item.email == email) View.GONE else View.VISIBLE // 팔로우(자기자신이 아니거나, 팔로우 상태가 아닌 경우에만)
        binding.txtPage.text = "${item.page}p" // 페이지
        binding.txtHeartCount.text = item.countHeart.toString() // 하트개수
        binding.txtCommentCount.text = item.countComment.toString() // 댓글개수
        binding.txtBook.text = item.title // 책 제목
        binding.txtDateTime.text = item.dateTime // 날짜, 시간
        binding.txtMemo.text = item.memo // 메모
        // open : spinner셋팅 -> 값에 따라 spinner 셋팅
        binding.spinnerSelectOpen.visibility = View.GONE
        when (item.open) {
            "all" -> binding.txtOpen.text = "전체"
            "follow" -> binding.txtOpen.text = "팔로우"
            else -> binding.txtOpen.text = "비공개"
        }
        /*
        좋아요
         */
        // 표시
        binding.imgHeart.setImageResource(if (item.checkHeart==1) R.drawable.fill_heart else R.drawable.empty_heart)
        // 좋아요 기능
        binding.imgHeart.setOnClickListener{itemHeart ->
            //Log.i("정보태그", "checkHeart->"+dataList[position].checkHeart)
            CoroutineScope(Dispatchers.Main).launch {
                val map: MutableMap<String, String> = HashMap()
                map["p_idx_memo"] = item.idx.toString()
                map["p_email"] = email
                if(item.checkHeart == 0 && !item.email.equals(email)){
                    map["alarm"] = "true"
                }
                val goServerForResult = fc.goServerForResult(context,"Update_heart_check",map)
                if(goServerForResult["status"]=="success"){
                    //Log.i("정보태그","goServerForResult : ${goServerForResult}")
                    // 하트 상태 변경
                    val drawableResId = if (item.checkHeart == 1) R.drawable.empty_heart else R.drawable.fill_heart
                    val drawable = context.getDrawable(drawableResId)
                    item.checkHeart = if (item.checkHeart == 1) 0 else 1
                    // 하트 갯수 변경
                    val data: Map<String, Any> = goServerForResult["data"] as Map<String, Any>
                    //Log.i("정보태그", "data->"+data)
                    binding.txtHeartCount.text = data["heartCount"].toString()
                    item.countHeart = data["heartCount"].toString().toIntOrNull() ?: 0
                    notifyDataSetChanged()
                }else{
                    Toast.makeText(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show()
                }
            }
        }
        /*
        팔로우
         */
        binding.txtFollow.setOnClickListener{itemFollow ->
            if(item.follow == 0){ // 팔로우 상태가 아닌 경우에만
                val map: MutableMap<String, String> = HashMap()
                map["from_email"] = email
                map["to_email"] = item.email
                CoroutineScope(Dispatchers.Main).launch  {
                    val goServer = fc.goServer(context,"following",map)
                    if(goServer){
                        binding.txtFollow.visibility = View.GONE
                        item.follow = 1
                        notifyDataSetChanged()
                    }
                }
            }
        }
        /*
        댓글이미지 -> 해당 메모에 대한 댓글다는 액티비티로 이동
         */
        binding.imgComment.setOnClickListener{
            val intent = Intent(context, Activity_Add_Comment::class.java)
            intent.putExtra("idx_memo", item.idx)
            intent.putExtra("writer_email",item.email)
            activity.startActivity(intent)
        }
        /*
        이미지 가져오기(슬라이드 셋팅)
         */
        val imgUrlList = item.imgUrls
            .removeSurrounding("[", "]") // 대괄호 제거
            .split(", ") // 구분자인 ", "로 분할
            .map { it.removeSurrounding("\"") } // 각 아이템의 시작과 끝의 따옴표 제거
            .toTypedArray()
//        imgUrlList.forEach { // Log.i("정보태그","imgUrl->${it}") }
        /*
        이미지 슬라이더
         */
        val imgSliderAdapter = SliderAdapterT1(context) // 어댑터 생성
        binding.sliderView.setSliderAdapter(imgSliderAdapter) // 어댑터 셋팅
        // 슬라이드 생성
        binding.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH)
        binding.sliderView.setIndicatorSelectedColor(Color.WHITE)
        binding.sliderView.setIndicatorUnselectedColor(Color.GRAY)
        binding.sliderView.setScrollTimeInSec(3) // 스크롤 지연(초) 설정
        binding.sliderView.setAutoCycle(true)
        binding.sliderView.startAutoCycle() // 자동으로 뒤집기를 시작
        binding.sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        binding.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        // 데이터 넣기
        val sliderItemList = imgUrlList.mapIndexed { index, imageUrl ->
            SliderItem().apply {
                description = "${index + 1}/${imgUrlList.size}"
                this.imageUrl =context.getString(R.string.server_url)+imageUrl
            }
        }.toMutableList()
        imgSliderAdapter.renewItems(sliderItemList) // 슬라이더에 리스트 반영





    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}