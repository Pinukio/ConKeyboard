package com.example.conkeyboard

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conkeyboard.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class SearchActivity : AppCompatActivity(), OnItemClick {
    private var totalList: List<ConData>? = null
    private var remained = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var page = 0

        binding.edittext.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s != null) {
                    if(s.isEmpty()) {
                        binding.btnClose.visibility = View.INVISIBLE
                    }
                    else {
                        binding.btnClose.visibility = View.VISIBLE
                    }
                }
                else {
                    binding.btnClose.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogStyle)
            builder.apply {
                setPositiveButton("확인", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                    binding.edittext.requestFocus()
                })
            }
            builder.setMessage("두 글자 이상 입력해주세요.")
            builder.create()
        }
        val adapter = SearchAdapter(this)
        val listener = object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1

                if(!binding.recyclerSearch.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    if(totalList != null && remained) {
                        val list = loadData(totalList!!, page)
                        if(list.size < 30)
                            adapter.setItemRemained(false)
                        val imgList: ArrayList<URL> = ArrayList()
                        for(i in list.indices) {
                            imgList.add(URL(list[i].photo[0]))
                        }
                        val baList = ConvertToByteArrayTask().execute(imgList).get()
                        if(baList != null) {
                            val bitmapList: ArrayList<Bitmap?> = ArrayList()
                            for(i in baList.indices) {
                                val ba = baList[i]
                                if(ba != null) {
                                    bitmapList.add(BitmapFactory.decodeByteArray(ba, 0, ba.size))
                                }
                                else {
                                    bitmapList.add(null)
                                }
                            }
                            adapter.addItem(list, bitmapList)
                            page++
                            adapter.notifyItemRangeChanged((page - 1) * 30, 30)
                        }
                    }
                }
            }
        }
        binding.edittext.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(v.text.length < 2) {
                    alertDialog.show()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edittext.windowToken, 0)
                }
                else {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edittext.windowToken, 0)

                    if(binding.recyclerSearch.adapter != null) {
                        adapter.resetItem()
                        totalList = null
                        remained = true
                        adapter.setItemRemained(remained)
                        binding.recyclerSearch.post { adapter.notifyDataSetChanged() }
                        page = 0
                    }
                    else {
                        binding.recyclerSearch.adapter = adapter
                        binding.recyclerSearch.addOnScrollListener(listener)
                    }

                    val retrofit = RetrofitConnection(applicationContext)
                    val json = JsonForSearch(v.text.toString())

                    val searchedConsCall: Call<List<ConData>> = retrofit.server.getSearchedCons(json)
                    searchedConsCall.enqueue(object: Callback<List<ConData>> {
                        override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                            totalList = response.body()
                            if(totalList != null && totalList!!.isNotEmpty()) {
                                if(totalList!!.size < 30)
                                    adapter.setItemRemained(false)
                                val list = loadData(totalList!!, page)
                                val imgList: ArrayList<URL> = ArrayList()
                                for(i in list.indices) {
                                    imgList.add(URL(list[i].photo[0]))
                                }
                                val baList = ConvertToByteArrayTask().execute(imgList).get()
                                if(baList != null) {
                                    val bitmapList: ArrayList<Bitmap?> = ArrayList()
                                    for(i in baList.indices) {
                                        val ba = baList[i]
                                        if(ba != null) {
                                            bitmapList.add(BitmapFactory.decodeByteArray(ba, 0, ba.size))
                                        }
                                        else {
                                            bitmapList.add(null)
                                        }
                                    }
                                    adapter.addItem(list, bitmapList)
                                    if(totalList!!.size < 30)
                                        adapter.notifyItemRangeChanged(0, totalList!!.size)
                                    else
                                        adapter.notifyItemRangeChanged(0, 30)
                                    page += 1

                                }
                            }
                            else if(totalList != null && totalList!!.isEmpty()) {
                                Toast.makeText(applicationContext, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                                adapter.setItemRemained(false)
                                binding.recyclerSearch.post{ adapter.notifyDataSetChanged() }

                            }
                        }

                        override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
                            Toast.makeText(applicationContext, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            adapter.setItemRemained(false)
                            binding.recyclerSearch.post{ adapter.notifyDataSetChanged() }
                        }
                    })
                }
            }
            false
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnClose.setOnClickListener {
            binding.edittext.text.clear()
        }
    }

    private fun loadData(totalList: List<ConData>, page: Int): List<ConData> {
        return if(totalList.size < (page + 1) * 30) {
            remained = false
            totalList.subList(page * 30, totalList.size)
        } else
            totalList.subList(page * 30, (page + 1) * 30)
    }

    override fun onClick(conNum: String) {
        val intent = Intent(this, ConInfoActivity::class.java)
        intent.putExtra("conNum", conNum)
        startActivity(intent)
    }
}