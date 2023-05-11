package com.example.books_ko.Function

/*
리사이클러뷰에 삭제를 위한 스와이프 및 드래그 앤 드롭을 지원하는 유틸리티 클래스
: 사용자가 액션을 수행 할 때 이벤트를 수신하는 RecyclerView 및 이벤트에 반응하는 콜백 메서드가 선언되어 있는 Callback클래스와 함께 사용한다
=> 구조적으로 RecyclerView와 ItemTouchHelper.Callback을 연결시켜줌
 */
interface ItemTouchHelperListener {

    fun onItemMove(from_position: Int, to_position: Int): Boolean
        // 매개변수 : from_position:움직임이 시작한 위치, to_position : 끝난위치
    fun onItemSwipe(position: Int)
}