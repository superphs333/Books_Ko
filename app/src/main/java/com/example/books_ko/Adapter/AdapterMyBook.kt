package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.databinding.ItemBookListBinding

class AdapterMyBook (
    var dataMyBooks: ArrayList<DataMyBook> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<AdapterMyBook.CustomViewHolder>(){

    lateinit var binding : ItemBookListBinding

    inner class CustomViewHolder(val binding: ItemBookListBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemBookListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataMyBooks[position]

        binding.txtTitle.text=item.title
    }

    override fun getItemCount(): Int {
        return dataMyBooks.size
    }


}