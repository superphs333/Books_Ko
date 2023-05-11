package com.example.bookapp

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.books_ko.Function.ItemTouchHelperListener

/*
앱과 ItemTouchHelper사이의 계약에 해당하는 클래스
ViewHolder에 대해 어떠한 터치 행동이 가능한지 제어하고, 사용자가 정의한 액션을 수행할 대 콜백을 받는다
 */
class ItemTouchHelperCallback(private val listener: ItemTouchHelperListener) : ItemTouchHelper.Callback() {

    // 드래그 앤 드롭을 허용할지 여부를 반환합니다.
    override fun isLongPressDragEnabled(): Boolean = true

    // 스와이프를 허용할지 여부를 반환합니다.
    override fun isItemViewSwipeEnabled(): Boolean = true

    /*
    드래그 방향과 드래그 이동을 정의하는 함수 : 드래그의 방향을 정의하고, 움직임을 리턴한다
    각각의 뷰에 수행할 수 있는 작업을 컨트롤 하기 위해서는, 이 메서드를 오버라이드 하고
    적절한 방향을 반환해야 함
    (makeMovementFlags(int,int) or SimpleCallback을 사용 할 수 있음)
     */
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//        val drag_flags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
//        val swipe_flags = ItemTouchHelper.START or ItemTouchHelper.END
        val drag_flags = ItemTouchHelper.LEFT  or ItemTouchHelper.RIGHT // 드래그 방향 (아이템 순서 변경)
        val swipe_flags =  ItemTouchHelper.UP   or ItemTouchHelper.DOWN// 스와이프 방향(화면에서 없어짐)
        return makeMovementFlags(drag_flags, swipe_flags)
    }



    /*
    아이템이 움직일 때 호출되는 함수
    사용자가 아이템을 드래그한다면, 이 itemtouchhelper은 onMove를 호출함
    => 어댑터에서 아이템을 이전위치에서 새로운 위치로 이동해야 하고
    리사이클러뷰의 어댑터에서 notifyItemMoved(int,int)를 호출해야 함
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        Log.i("정보태그", "onMove")
        listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true  // 아이템 위치를 변경하겠다는 의미로 true를 반환
    }

    /*
    ItemTouchHelper는 범위를 벗어날 때까지 View를 애니메이션화한 다음 이 메소드를 호출합니다.
     여기서 어댑터를 업데이트 해야하고 관련된 Adapter의 notify 이벤트를 호출
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.i("정보태그", "onSwiped")
        listener.onItemSwipe(viewHolder.adapterPosition)
            // onItemSwipe -> 삭제한 아이템의 위치를 어댑터엦 ㅓㄴ달함
    }
}
