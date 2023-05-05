package com.example.books_ko

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.FragmentBooksBinding


class Fragment_Books : Fragment(), View.OnClickListener {

    private var binding : FragmentBooksBinding? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    val am = AboutMember

    // FAB
    var fab_open: Animation? = null
    var fab_close: Animation? = null
    var openFlag = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBooksBinding.inflate(layoutInflater, container, false)
        val view: View = binding!!.getRoot()


        activity = requireActivity()
        context = requireContext()

        /*
        floating버튼 설정
         */
        fab_open = AnimationUtils.loadAnimation(context,R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        // 버튼 상태 초기화(닫혀있어야 함)
        binding!!.floatingSelf.startAnimation(fab_close)
        binding!!.floatingSearch.startAnimation(fab_close)
        // 초기 클릭 불가능
        binding!!.floatingSelf.isClickable = false
        binding!!.floatingSearch.isClickable = false
        // 클릭시 동작
        binding!!.floating.setOnClickListener(this)
        binding!!.floatingSelf.setOnClickListener(this)
        binding!!.floatingSearch.setOnClickListener(this)




        return binding?.root
    }

    override fun onClick(v: View?) {
        val id = v!!.id
        when (id) {
            R.id.floating -> {
                val builder = AlertDialog.Builder(context)
                val str = arrayOf("직접추가", "도서검색")
                builder.setTitle("어떤 방식으로 도서를 추가하시겠습니까?")
                    .setNegativeButton("취소", null)
                    .setItems(
                        str
                    )  // 리스트 목록에 사용할 배열
                    { dialog, which ->
                        Log.d("정보태그", "선택된것=" + str[which])
                        if (str[which] == "직접추가") { // 직접추가
                            Log.i("정보태그","선택->직접추가")

//                            val intent2 = Intent(context, Activity_Book_Add::class.java)
//                            startActivity(intent2)
                        } else { // 도서찾기
                            Log.i("정보태그","선택->도서검색")
                            val intent = Intent(context, Activity_Book_Search::class.java)
                            startActivity(intent)
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.floating_search -> {
                // why_change 값이 "reason2"일 때 수행할 로직
                // ...
            }
            R.id.floating_self -> {
                // why_change 값이 "reason3"일 때 수행할 로직
                // ...
            }
        }

    }

}