package com.example.conkeyboard

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conkeyboard.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), OnItemClick {
    private var totalList: List<ConData>? = null

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
                    adapter.setLoadingFlag(false)
                    if(totalList != null) {
                        adapter.addItem(loadData(totalList!!, page++))
                        adapter.notifyItemRangeChanged((page - 1) * 30, 30)
                        adapter.setLoadingFlag(true)
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
                    val retrofit = RetrofitConnection(applicationContext)
                    val searchedConsCall: Call<List<ConData>> = retrofit.server.getSearchedCons(v.text.toString())
                    searchedConsCall.enqueue(object: Callback<List<ConData>> {
                        override fun onResponse(call: Call<List<ConData>>, response: Response<List<ConData>>) {
                            totalList = response.body()
                            if(totalList != null) {
                                adapter.addItem(loadData(totalList!!, page++))
                                binding.recyclerSearch.addOnScrollListener(listener)
                                binding.recyclerSearch.adapter = adapter
                            }
                        }

                        override fun onFailure(call: Call<List<ConData>>, t: Throwable) {
                            Toast.makeText(applicationContext, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
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
        return if(totalList.size - 1 < (page + 1) * 30) {
            totalList.subList(page * 30, totalList.size - 1)
        }
        else totalList.subList(page * 30, (page + 1) * 30)
    }

    override fun onClick(conNum: String) {
        val intent = Intent(this, ConInfoActivity::class.java)
        intent.putExtra("conNum", conNum)
        startActivity(intent)
    }
}