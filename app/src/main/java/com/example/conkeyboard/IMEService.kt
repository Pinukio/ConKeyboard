package com.example.conkeyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class IMEService : InputMethodService(), View.OnTouchListener {
    private lateinit var btnArray: ArrayList<Button>
    private var shiftFlag: Int = 0
    private lateinit var ic: InputConnection
    private lateinit var vibrator: Vibrator
    private lateinit var handler: Handler
    private lateinit var longPressed: Runnable
    private var tmp = 0
    private lateinit var shiftBtn: ImageButton
    private var flag = 0
    private val charList: List<Char> = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    private val spCharList: List<Char> = listOf('+', '×', '÷', '=', '/', '_', '<', '>', '♡', '☆', '!', '@', '#', '~', '%', '^', '&', '*', '(', ')', '-', '\'', '\"', ':', ';', ',', '?', '`', '￦', '\\', '|', '♤', '♧', '{', '}', '[', ']', '•', '○', '●', '□', '■', '◇', '$', '¥', '°', '《', '》', '¡', '¿')

    override fun onCreateInputView(): View {
        val container = LinearLayout(applicationContext)
        container.orientation = LinearLayout.VERTICAL
        container.setBackgroundColor(Color.RED)
        val height = resources.displayMetrics.heightPixels
        val width = resources.displayMetrics.widthPixels
        val w = width / 200
        container.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val keyboardView: View = layoutInflater.inflate(R.layout.layout_keyboard, container, true)
        val layout: LinearLayout = keyboardView.findViewById(R.id.layout)
        layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height * 0.4).toInt())
        //keyboardView.layoutParams = LinearLayout.LayoutParams(0, 0)

        btnArray = ArrayList()
        for(i in 0..35){
            val id: String = "btn_" + charList[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            btnArray.add(keyboardView.findViewById(resourceID) as Button)
            btnArray[i].setOnTouchListener(this)
            val background: GradientDrawable = btnArray[i].background as GradientDrawable
            background.setStroke(w, ContextCompat.getColor(applicationContext, R.color.background_gray))
        }
        val strArray: List<String> = listOf("spaceBar", "period", "question")
        for(i in 0..2){
            val id: String = "btn_" + strArray[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            btnArray.add(keyboardView.findViewById(resourceID) as Button)
            btnArray[i+36].setOnTouchListener(this)
            val background: GradientDrawable = btnArray[i+36].background as GradientDrawable
            background.setStroke(w, ContextCompat.getColor(applicationContext, R.color.background_gray))
        }
        val imgBtnArray: List<String> = listOf("del", "lang", "enter", "shift", "spChar")
        for(i in 0..4) {
            val id: String = "btn_" + imgBtnArray[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            val imgBtn: ImageButton = keyboardView.findViewById(resourceID) as ImageButton
            imgBtn.setOnTouchListener(this)
            if(i == 1) {
                (imgBtn.background as GradientDrawable).setStroke(w, ContextCompat.getColor(applicationContext, R.color.background_gray))
                imgBtn.setPadding(w*5)
            }
            else {
                (imgBtn.background as LayerDrawable).setLayerInset(1, (w/5*12), w, (w/5*12), w)//l t r b
                imgBtn.setPadding((w*10.5).toInt())
            }
        }
        shiftBtn = keyboardView.findViewById(R.id.btn_shift)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        handler = Handler()
        longPressed = Runnable {
            when {
                tmp < 10 -> {
                    tmp += 1
                    vibrator.vibrate(VibrationEffect.createOneShot(10, 135))
                    deleteChar(1)
                }
                else -> {
                    deleteChar(10)
                }
            }
            handler.postDelayed(longPressed, 70)
        }
        return when(flag) {
            0 -> keyboardView
            else -> layoutInflater.inflate(R.layout.dfa, null)
        }
        //return keyboardView
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        ic = currentInputConnection
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tmp = 0
                vibrator.vibrate(VibrationEffect.createOneShot(10, 135))
                if(v?.id == R.id.btn_del) {
                    deleteChar(1)
                    handler.postDelayed(longPressed, 400)
                }
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressed)
                when(v?.id){
                    R.id.btn_del -> {}
                    R.id.btn_shift -> {
                        when(shiftFlag){
                            0 -> changeShiftFlag(1)
                            1 -> changeShiftFlag(2)
                            2 -> changeShiftFlag(0)
                        }
                    }
                    R.id.btn_spChar -> {
                        //flag = 1
                        //setInputView(onCreateInputView())

                    }
                    R.id.btn_lang -> {}
                    R.id.btn_spaceBar -> {
                        ic.commitText(" ", 1)
                    }
                    R.id.btn_enter -> {
                        ic.commitText("\n", 1)
                    }
                    else -> {
                        val value: String = (v as Button).text.toString()
                        ic.commitText(value, 1)
                        if(shiftFlag == 1)
                            changeShiftFlag(0)
                    }
                }
            }
        }
        return true
    }

    private fun changeShiftFlag(flag: Int){
        when(flag){
            0 -> {
                shiftBtn.setImageResource(R.drawable.shift_black)
                shiftFlag = 0
                for(i in 0..25) {
                    val text: Char = btnArray[i].text[0]
                    btnArray[i].text = text.toLowerCase().toString()
                }
            }
            1 -> {
                shiftBtn.setImageResource(R.drawable.shift_blue)
                shiftFlag = 1
                for(i in 0..25) {
                    val text: Char = btnArray[i].text[0]
                    btnArray[i].text = text.toUpperCase().toString()
                }
            }
            2 -> {
                shiftBtn.setImageResource(R.drawable.shift_blue_filled)
                shiftFlag = 2
                for(i in 0..25) {
                    val text: Char = btnArray[i].text[0]
                    btnArray[i].text = text.toUpperCase().toString()
                }
            }
        }
    }
    private fun deleteChar(num: Int) {
        val text: CharSequence? = ic.getSelectedText(0)
        if(TextUtils.isEmpty(text))
            ic.deleteSurroundingText(num, 0)
        else
            ic.commitText("", 1)
    }
    private fun changeToSpChar() {
        for(i in 0..25){
            btnArray[i].text = spCharList[i].toString()
            shiftBtn.setImageResource(android.R.color.transparent)

        }
    }
}