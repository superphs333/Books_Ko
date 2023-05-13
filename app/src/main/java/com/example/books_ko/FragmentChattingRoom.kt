package com.example.books_ko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterMyBook
import com.example.books_ko.Adapter.Adapter_Chatting_Room
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Data.Data_Chatting_Room
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.FragmentChattingRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class FragmentChattingRoom : Fragment() {

    private var binding : FragmentChattingRoomBinding? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    val am = AboutMember
    var email = ""

    var arrayList: ArrayList<Data_Chatting_Room>? = ArrayList()
    private lateinit var adapterMyBook : Adapter_Chatting_Room
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            email = am.getEmailFromRoom(context)
            Log.i("정보태그","email->"+email)

            /*
            리사이클러뷰 셋팅
             */
            linearLayoutManager = LinearLayoutManager(context)
            adapterMyBook = Adapter_Chatting_Room(arrayList!!, context, activity,email)
            binding!!.rvChattingRooms.apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = adapterMyBook
            }

            // 데이터셋팅
            arrayList = getChattingRooms()
            Log.i("정보태그","arrayList->$arrayList")
            adapterMyBook.dataList = arrayList!!
            adapterMyBook.notifyDataSetChanged()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChattingRoomBinding.inflate(layoutInflater, container, false)
        val view: View = binding!!.getRoot()

        activity = requireActivity()
        context = requireContext()

        /*
        spinner셋팅
         */
        val data = context.resources.getStringArray(R.array.select_chatting_room_view)
        var adapter = ArrayAdapter(context,android.R.layout.simple_dropdown_item_1line,data)
        binding!!.spinSort.adapter = adapter


        /*
        btn_add_room => 채팅방 개설 액티비티 이동
         */
        binding!!.btnAddRoom.setOnClickListener {
            val intent = Intent(context, Activity_Add_Chatting_Room::class.java)
            startActivity(intent)
        }


        return binding?.root
    }

    /*
    채팅방 데이터 가져오기
     */
    suspend fun getChattingRooms(): ArrayList<Data_Chatting_Room>? = withContext(Dispatchers.IO) {

        val selectedPosition = binding!!.spinSort.selectedItemPosition
        Log.i("정보태그","selectedPosition->$selectedPosition")

        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)


        try {
            val response = myApi.Get_Chatting_Room_Data("Get_Data_Chatting_Rooms",email,selectedPosition).execute()
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    val dataChattingRooms =
                        response.body()?.data?.chattingRoomList as ArrayList<Data_Chatting_Room>?
                    dataChattingRooms


                } else {
                    Log.i("정보태그", "[getChattingRooms]서버에 연결은 되었으나 오류발생")
                    null
                }
            } else {
                Log.i("정보태그", "[getChattingRooms]result.staus isSuccessful X")
                null
            }
        } catch (e: IOException) {
            null
        }
    }


}