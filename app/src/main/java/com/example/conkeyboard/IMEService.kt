package com.example.conkeyboard

import android.content.ClipDescription
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.inputmethodservice.InputMethodService
import android.os.*
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.io.*
import java.lang.Exception

class IMEService : InputMethodService(), View.OnTouchListener, OnItemClick {
    private lateinit var btnArray: ArrayList<Button>
    private lateinit var imageBtnArray: ArrayList<ImageButton>
    private var shiftFlag: Int = 0
    private lateinit var ic: InputConnection
    private lateinit var vibrator: Vibrator
    private lateinit var handler: Handler
    private lateinit var longPressed: Runnable
    private var tmp = 0
    private lateinit var shiftBtn: ImageButton
    private lateinit var spCharBtn: Button
    private lateinit var enterBtn: ImageButton
    private var spCharFlag = 0
    private var enterFlag = 0 //0이면 enter
    private var currentLang = "en"
    private lateinit var nextBtn: Button
    private var editorInfo: EditorInfo? = null
    private lateinit var layout: LinearLayout
    private lateinit var conField: LinearLayout
    private var num = 0
    private var isDarkMode: Boolean = false
    private val enList: List<String> = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m")
    private val numList: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val koList: List<String> = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ", "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
    private val koShiftList: List<String> = listOf("ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅒ", "ㅖ")
    private val spCharList: List<String> = listOf("+", "×", "÷", "=", "/", "_", "<", ">", "♡", "☆", "!", "@", "#", "%", "^", "&", "*", "(", ")", "~", "-", "\'", "\"", ":", ";", ",", "?", "￦", "\\", "|", "♤", "♧", "{", "}", "[", "]", "`", "•", "○", "●", "□", "■", "◇", "\$", "€", "£", "¥", "°", "《", "》", "¡", "¿")
    private var isKoreanInputting: Boolean = false
    private lateinit var adapter: ConFieldAdapter
    private var pos: Int = -1
    private lateinit var keysLayout: LinearLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var recycler: RecyclerView

    override fun onCreateInputView(): View {
        val height = resources.displayMetrics.heightPixels
        val width = resources.displayMetrics.widthPixels
        val w = width / 200
        val h = height / 200
        val keyboardView: View
        val isPortrait: Boolean

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            keyboardView = layoutInflater.inflate(R.layout.keyboard_portrait, null)
            layout = keyboardView.findViewById(R.id.layout)
            keysLayout = keyboardView.findViewById(R.id.layout_keyboard_portrait)
            layout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (height * 0.4).toInt()
            )
            isPortrait = true
        }
        else {
            keyboardView = layoutInflater.inflate(R.layout.keyboard_landscape, null)
            keysLayout = keyboardView.findViewById(R.id.layout_keyboard_landscape)
            layout = keyboardView.findViewById(R.id.layout)
            layout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (height * 0.55).toInt()
            )
            isPortrait = false
        }
        btnArray = ArrayList()
        val keyList: List<String> = enList + numList + listOf("spaceBar", "period", "question")
        num =
                if(isPortrait) w
                else h

        for(i in 0..38){
            val id: String = "btn_" + keyList[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            btnArray.add(keysLayout.findViewById(resourceID) as Button)
            btnArray[i].setOnTouchListener(this)
        }

        val imgBtnArray: List<String> = listOf("del", "lang", "enter", "shift")
        imageBtnArray = ArrayList()
        for(i in 0..3) {
            val id: String = "btn_" + imgBtnArray[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)

            imageBtnArray.add(keysLayout.findViewById(resourceID) as ImageButton)
            imageBtnArray[i].setOnTouchListener(this)
            if(i == 1) {
                if(isPortrait)
                    imageBtnArray[i].setPadding((w*5.5).toInt())
                else
                    imageBtnArray[i].setPadding((h*4.5).toInt())
            }
            else {
                if(isPortrait)
                    imageBtnArray[i].setPadding((h*4.5).toInt())
                else
                    imageBtnArray[i].setPadding((h*4.5).toInt())
            }
        }
        val shopBtn: ImageView = keyboardView.findViewById(R.id.btn_con_shop)
        shopBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if(isPortrait)
            shopBtn.setPadding(w*4)
        else
            shopBtn.setPadding(w)
        spCharBtn = keysLayout.findViewById(R.id.btn_spChar)
        nextBtn = keysLayout.findViewById(R.id.btn_next)
        conField = layout.findViewById(R.id.field_con)
        (spCharBtn.background as LayerDrawable).setLayerInset(1, (num / 5 * 12), num, (num / 5 * 12), num)
        (nextBtn.background as LayerDrawable).setLayerInset(1, (num / 5 * 12), num, (num / 5 * 12), num)
        spCharBtn.setOnTouchListener(this)
        nextBtn.setOnTouchListener(this)

        shiftBtn = keysLayout.findViewById(R.id.btn_shift)
        enterBtn = keysLayout.findViewById(R.id.btn_enter)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        handler = Handler()
        longPressed = Runnable {
            when {
                tmp < 10 -> {
                    val s =  ic.getTextBeforeCursor(1, 0)
                    if(s.isNotEmpty())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, 135))
                    if(isKoreanInputting)
                        koreanDeleteManager(s.toString())
                    else {
                        tmp += 1
                        deleteChar(1)
                    }
                }
                else -> {
                    deleteChar(10)
                }
            }
            handler.postDelayed(longPressed, 70)
        }

        when {
            spCharFlag == 1 -> changeToSpChar()
            spCharFlag == 2 -> {
                changeToSpChar()
                changeSpCharPage()
            }
            currentLang == "ko" -> changeToKorean()
        }
        if(shiftFlag != 0)
            changeShiftFlag(shiftFlag)

        recycler = keyboardView.findViewById(R.id.recycler_title_cons)
        viewPager = keyboardView.findViewById(R.id.viewpager)

        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        val bitmapArrayList: ArrayList<Bitmap?> = ArrayList()
        val useConList = PreferenceManager().getConList(applicationContext, "use")
        val isPortrait: Boolean = (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        val width = resources.displayMetrics.widthPixels
        val w = width / 200

        if(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            changeToDarkTheme()
            isDarkMode = true
        }
        else {
            changeToLightTheme()
            isDarkMode = false
        }

        if(useConList != null) {
            for(i in useConList.indices) {
                bitmapArrayList.add(loadPNG(getPath(useConList[i].conNum), "title.jpg"))
            }
            adapter =
                    if(isPortrait) ConFieldAdapter(bitmapArrayList, this, w*4, isDarkMode)
                    else ConFieldAdapter(bitmapArrayList, this, (w*1.5).toInt(), isDarkMode)

            recycler.adapter = adapter
        }

        if(pos != -1) {
            keysLayout.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
            pos = -1
        }


        editorInfo = info
        try {
            val str = (info!!.imeOptions.toString(16))
            val value = Integer.decode(str[str.length - 1].toString())
            when(value) {
                EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEND-> {
                    if(isDarkMode)
                        enterBtn.setImageResource(R.drawable.arrow_white)
                    else
                        enterBtn.setImageResource(R.drawable.arrow_black)
                    enterFlag = value
                }
                else -> {
                    if(isDarkMode)
                        enterBtn.setImageResource(R.drawable.enter_white)
                    else
                        enterBtn.setImageResource(R.drawable.enter_black)
                    enterFlag = 0
                }
            }
        }
        catch (e: Exception) {
            enterBtn.setImageResource(R.drawable.enter_black)
            enterFlag = 0
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        ic = currentInputConnection
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                try {
                    if(isDarkMode)
                        (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked_dark))
                    else
                        (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked))
                }
                catch (e: Exception) {
                    if(isDarkMode)
                        ((v?.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked_dark))
                    else
                        ((v?.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked))
                }
                tmp = 0
                vibrator.vibrate(VibrationEffect.createOneShot(10, 135))
                if (v?.id == R.id.btn_del) {

                    if(isKoreanInputting)
                        koreanDeleteManager(ic.getTextBeforeCursor(1, 0).toString())
                    else {
                        deleteChar(1)
                    }
                    handler.postDelayed(longPressed, 400)
                }
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressed)

                when (v?.id) {
                    R.id.btn_del -> {
                        if(shiftFlag == 1)
                            changeShiftFlag(0)
                        if(isDarkMode)
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_shift -> {
                        when (currentLang) {
                            "en" -> {
                                when (shiftFlag) {
                                    0 -> changeShiftFlag(1)
                                    1 -> changeShiftFlag(2)
                                    2 -> changeShiftFlag(0)
                                }
                            }
                            "ko" -> {
                                when (shiftFlag) {
                                    0 -> changeShiftFlag(1)
                                    1 -> changeShiftFlag(0)
                                }
                            }
                        }
                        if(isDarkMode)
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_spChar -> {
                        ic.finishComposingText()
                        isKoreanInputting = false
                        if (spCharFlag == 0)
                            changeToSpChar()
                        else {
                            when (currentLang) {
                                "en" -> changeToEnglish()
                                "ko" -> changeToKorean()
                            }
                            changeShiftFlag(shiftFlag)
                        }
                        if(isDarkMode)
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_next -> {
                        changeSpCharPage()
                        if(isDarkMode)
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_lang -> {
                        ic.finishComposingText()
                        isKoreanInputting = false

                        if(shiftFlag != 0)
                            changeShiftFlag(0)
                        when (currentLang) {
                            "en" -> {
                                if (spCharFlag == 0)
                                    changeToKorean()
                                else
                                    changeToEnglish()
                            }
                            "ko" -> {
                                if (spCharFlag == 0)
                                    changeToEnglish()
                                else
                                    changeToKorean()
                            }
                        }
                        if(isDarkMode)
                            (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_spaceBar -> {
                        ic.finishComposingText()
                        ic.commitText(" ", 1)
                        if(isDarkMode)
                            (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_color_dark))
                        else
                            (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
                        isKoreanInputting = false
                    }
                    R.id.btn_enter -> {
                        when(enterFlag) {
                            0 -> {
                                ic.finishComposingText()
                                ic.commitText("\n", 1)
                            }
                            else -> {
                                ic.performEditorAction(enterFlag)
                            }
                        }
                        if(isDarkMode)
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                        else
                            ((v.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                        isKoreanInputting = false
                    }
                    else -> {
                        if(isDarkMode)
                            (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_color_dark))
                        else
                            (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
                        val value: String = (v as Button).text.toString()
                        if(!isKorean(value))
                            isKoreanInputting = false
                        when (currentLang) {
                            "en" -> {
                                ic.commitText(value, 1)
                                if (shiftFlag == 1)
                                    changeShiftFlag(0)
                            }
                            "ko" -> {
                                val s = ic.getTextBeforeCursor(1, 0)
                                if(shiftFlag == 1)
                                    changeShiftFlag(0)
                                if (s.isNotEmpty() && isKorean(s.toString()) && isKoreanInputting) {
                                    koreanInputManager(s.toString(), value)
                                }
                                else if(isKorean(value)) {
                                    ic.setComposingText(value, 1)
                                    isKoreanInputting = true
                                }
                                else {
                                    ic.finishComposingText()
                                    ic.commitText(value, 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    private fun changeShiftFlag(flag: Int){
        when(flag){
            0 -> {
                if(isDarkMode)
                    shiftBtn.setImageResource(R.drawable.shift_white)
                else
                    shiftBtn.setImageResource(R.drawable.shift_black)
                shiftFlag = 0
                when (currentLang) {
                    "en" -> {
                        for (i in 0..25) {
                            val text: Char = btnArray[i].text[0]
                            btnArray[i].text = text.toLowerCase().toString()
                        }
                    }
                    "ko" -> {
                        for (i in 0..4) {
                            btnArray[i].text = koList[i]
                        }
                        for (i in 8..9) {
                            btnArray[i].text = koList[i]
                        }
                    }
                }
            }
            1 -> {
                shiftBtn.setImageResource(R.drawable.shift_blue)
                shiftFlag = 1
                when (currentLang) {
                    "en" -> {
                        for (i in 0..25) {
                            val text: Char = btnArray[i].text[0]
                            btnArray[i].text = text.toUpperCase().toString()
                        }
                    }
                    "ko" -> {
                        for (i in 0..4) {
                            btnArray[i].text = koShiftList[i]
                        }
                        for (i in 5..6) {
                            btnArray[i + 3].text = koShiftList[i]
                        }
                    }
                }
            }
            2 -> {
                shiftBtn.setImageResource(R.drawable.shift_blue_filled)
                shiftFlag = 2
                for (i in 0..25) {
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
    private fun changeToEnglish() {
        btnArray[36].text = "English"
        for(i in 0..25) {
            btnArray[i].text = enList[i]
        }
        if(spCharFlag != 0) {
            spCharFlag = 0
            nextBtn.text = "1/2"
            nextBtn.visibility = View.GONE
            shiftBtn.visibility = View.VISIBLE
            spCharBtn.text = "!#1"
        }
        currentLang = "en"

    }
    private fun changeToKorean() {
        btnArray[36].text = "한글"
        for (i in 0..25) {
            btnArray[i].text = koList[i]
        }
        if(spCharFlag != 0) {
            spCharFlag = 0
            nextBtn.text = "1/2"
            nextBtn.visibility = View.GONE
            shiftBtn.visibility = View.VISIBLE
            spCharBtn.text = "!#1"
        }
        currentLang = "ko"
    }
    private fun changeToSpChar() {
        for(i in 0..25) {
            btnArray[i].text = spCharList[i]
            shiftBtn.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
            when(currentLang) {
                "en" -> spCharBtn.text = "ABC"
                "ko" -> spCharBtn.text = "가"
            }
        }
        spCharFlag = 1
    }
    private fun changeSpCharPage() {
        when(spCharFlag) {
            1 -> {
                for (i in 0..25) {
                    btnArray[i].text = spCharList[i + 26]
                    nextBtn.text = "2/2"
                    spCharFlag = 2
                }
            }
            2 -> {
                for (i in 0..25) {
                    btnArray[i].text = spCharList[i]
                    nextBtn.text = "1/2"
                    spCharFlag = 1
                }
            }
        }
    }
    private fun isConsonant(s: String): Boolean {
        val consonantList: List<String> = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ")
        return s in consonantList
    }
    private fun isVowel(s: String): Boolean {
        val vowelList: List<String> = listOf("ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅠ", "ㅜ", "ㅡ", "ㅒ", "ㅖ")
        return s in vowelList
    }
    private fun isDiphthong(s: String): Boolean {
        val diphthongList: List<String> = listOf("ㅟ", "ㅞ", "ㅝ", "ㅙ", "ㅘ", "ㅚ", "ㅢ")
        return s in diphthongList
    }
    private fun isDoubleConsonant(s: String): Boolean {
        val doubleConsonantList: List<String> = listOf("ㄳ", "ㄵ", "ㄶ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅄ")
        return s in doubleConsonantList
    }
    private fun koreanInputManager(s: String, value: String) {
        val w = s[0].toString()
        isKoreanInputting = true
        if(isConsonant(w)) { //앞 글자가 자음뿐 + 뒤에 모음이 옴
            if(isVowel(value)) {
                val jasoList = listOf(w, value)
                ic.setComposingText(HangulParser.assemble(jasoList), 1)
            }
            else {
                ic.finishComposingText()
                ic.setComposingText(value, 1)
            }
        }
        else if(isVowel(w)) {
            if(isVowel(value)) {
                val result = KoreanAssemble().makeDiphthong(w, value)
                if(result == "error") { //모음뿐 + 다른 모음
                    ic.finishComposingText()
                    ic.setComposingText(value, 1)
                }
                else
                    ic.setComposingText(result, 1)
            }
            else { //모음 + 자음
                ic.finishComposingText()
                ic.setComposingText(value, 1)
            }
        }
        else if(isDiphthong(w)) { //이중모음 + 다른 거
            ic.finishComposingText()
            ic.setComposingText(value, 1)
        }
        else {
            val jasoList: MutableList<String> = HangulParser.disassemble(w)
            when(jasoList.size) {
                2 -> {
                    if (isVowel(value)) { //모음 + 모음
                        val result = KoreanAssemble().makeDiphthong(jasoList[1], value)
                        if (result == "error") { //이중모음 아닌 경우
                            ic.finishComposingText()
                            ic.setComposingText(value, 1)
                        } else { //이중모음
                            jasoList[1] = result
                            ic.setComposingText(HangulParser.assemble(jasoList), 1)
                        }
                    }
                    else { //모음 + 자음
                        try {
                            jasoList.add(value)
                            val tmp = HangulParser.assemble(jasoList)
                            ic.setComposingText(tmp, 1)
                        }
                        catch (e: HangulParserException) { //까+ㄸ처럼 안 합쳐지는 경우
                            ic.finishComposingText()
                            ic.setComposingText(value, 1)
                        }

                    }
                }
                3 -> {
                    if (isConsonant(value)) { //받침 + 자음
                        val result = KoreanAssemble().makeDoubleConsonant(jasoList[2], value)
                        if (result == "error") { //겹자음이 아닌 경우
                            ic.finishComposingText()
                            ic.setComposingText(value, 1)
                        } else {
                            jasoList[2] = result
                            ic.setComposingText(HangulParser.assemble(jasoList), 1)
                        }
                    }
                    else if(isConsonant(jasoList[2]) && isVowel(value)) { //받침 + 모음
                        val list: List<String> = listOf(jasoList[2], value)
                        jasoList.removeAt(2)
                        ic.setComposingText(HangulParser.assemble(jasoList), 1)
                        ic.finishComposingText()
                        ic.setComposingText(HangulParser.assemble(list), 1)
                    }
                    else if(isDoubleConsonant(jasoList[2]) && isVowel(value)){ //겹받침 + 모음
                        val list: List<String> = KoreanDisassemble().disassembleDoubleConsonant(jasoList[2])
                        val tmp: List<String> = listOf(list[1], value)
                        jasoList.removeAt(2)
                        jasoList.add(list[0])
                        ic.setComposingText(HangulParser.assemble(jasoList), 1)
                        ic.finishComposingText()
                        ic.setComposingText(HangulParser.assemble(tmp), 1)
                    }
                }
            }
        }
    }
    private fun koreanDeleteManager(s: String) {
        val w = s[0].toString()

        if(isConsonant(w) || isVowel(w)) {
            ic.finishComposingText()
            deleteChar(1)
            isKoreanInputting = false
        }
        else if(isDiphthong(w)) { //이중모음
            val list: List<String> = KoreanDisassemble().disassembleDiphthong(w)
            ic.setComposingText(list[0], 1)
        }
        else {
            val jasoList: MutableList<String> = HangulParser.disassemble(w)
            when(jasoList.size) {
                2 -> {
                    if(isDiphthong(jasoList[1])) { // 이중모음
                        val list: List<String> = KoreanDisassemble().disassembleDiphthong(jasoList[1])
                        jasoList[1] = list[0]
                        ic.setComposingText(HangulParser.assemble(jasoList), 1)
                    }
                    else
                        ic.setComposingText(jasoList[0], 1)
                }
                3 -> {
                    if(isDoubleConsonant(jasoList[2])) {
                        val list: List<String> = KoreanDisassemble().disassembleDoubleConsonant(jasoList[2])
                        jasoList[2] = list[0]
                        ic.setComposingText(HangulParser.assemble(jasoList), 1)
                    }
                    else {
                        jasoList.removeAt(2)
                        ic.setComposingText(HangulParser.assemble(jasoList), 1)
                    }
                }
            }
        }
    }
    private fun isKorean(s: String): Boolean {
        val c = s[0].toInt()
        if(c in 0x1100..0x11FF || c in 0x3130..0x318F || c in 0xAC00..0xD7A3) return true
        return false
    }
    private fun changeToLightTheme() {
        layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.background_gray))
        conField.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.background_field_con))
        for(i in 0..38) {
            (btnArray[i].background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
        }
        for(i in 0..3) {
            if(i == 1)
                (imageBtnArray[i].background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
            else {
                ((imageBtnArray[i].background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                ((imageBtnArray[i].background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.background_gray))
            }
        }

        ((spCharBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
        ((spCharBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.background_gray))
        spCharBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))

        ((nextBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
        ((nextBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.background_gray))
        nextBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))

        setLightStroke()
    }
    private fun changeToDarkTheme() {
        layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.black))
        conField.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.background_field_con_dark))
        for(i in 0..38) {
            (btnArray[i].background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_color_dark))
            btnArray[i].setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        }
        val resIdList: List<Int> = listOf(R.drawable.backspace_white, R.drawable.globe_white, R.drawable.enter_white, R.drawable.shift_white)
        for(i in 0..3) {
            if(i == 1)
                (imageBtnArray[i].background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
            else {
                ((imageBtnArray[i].background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
                ((imageBtnArray[i].background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.black))
            }
            imageBtnArray[i].setImageResource(resIdList[i])
        }
        ((spCharBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
        ((spCharBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.black))
        spCharBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))

        ((nextBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_background) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_dark))
        ((nextBtn.background as LayerDrawable).findDrawableByLayerId(R.id.btn_img_stroke) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.black))
        nextBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))

        setDarkStroke()

    }
    private fun setLightStroke() {
        for(i in 0..38) {
            val background: GradientDrawable = btnArray[i].background as GradientDrawable
            background.setStroke(
                    num, ContextCompat.getColor(
                    applicationContext,
                    R.color.background_gray
            )
            )
        }
        for(i in 0..3) {
            if(i == 1) {
                (imageBtnArray[i].background as GradientDrawable).setStroke(
                        num, ContextCompat.getColor(
                        applicationContext,
                        R.color.background_gray
                )
                )
            }
            else {
                (imageBtnArray[i].background as LayerDrawable).setLayerInset(
                        1,
                        (num / 5 * 12),
                        num,
                        (num / 5 * 12),
                        num
                )//l t r b
            }
        }
    }
    private fun setDarkStroke() {
        for(i in 0..38) {
            val background: GradientDrawable = btnArray[i].background as GradientDrawable
            background.setStroke(
                    num, ContextCompat.getColor(
                    applicationContext,
                    R.color.black
            )
            )
        }
        for(i in 0..3) {
            if(i == 1) {
                (imageBtnArray[i].background as GradientDrawable).setStroke(
                        num, ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                )
                )
            }
            else {
                (imageBtnArray[i].background as LayerDrawable).setLayerInset(
                        1,
                        (num / 5 * 12),
                        num,
                        (num / 5 * 12),
                        num
                )//l t r b
            }
        }
    }
    override fun onClick(position: Int) {
        if(position == -2) {
            val intent = Intent(this, SettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else {
            var prePos = pos
            if(pos == -1) {
                if(isDarkMode)
                    layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.background_field_con_dark))
                pos = position
                keysLayout.visibility = View.GONE
                viewPager.visibility = View.VISIBLE
                val useConList = PreferenceManager().getConList(applicationContext, "use")!!
                val viewPagerAdapter = ViewPagerAdapter(useConList, this, applicationContext, isDarkMode)
                viewPager.adapter = viewPagerAdapter
                viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(posi: Int) {
                        super.onPageSelected(posi)
                        prePos = pos
                        pos = posi

                        if(prePos == -1) {
                            if(isDarkMode) {
                                adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.title_con_clicked_dark))
                            }
                            else {
                                adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.title_con_clicked))
                            }
                        }
                        else {
                            if(isDarkMode) {
                                adapter.setTitleConColor(prePos, ContextCompat.getColor(applicationContext, R.color.background_field_con_dark))
                                adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.title_con_clicked_dark))
                            }
                            else {
                                adapter.setTitleConColor(prePos, ContextCompat.getColor(applicationContext, R.color.background_field_con))
                                adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.title_con_clicked))
                            }
                        }
                    }
                })
                viewPager.setCurrentItem(pos, false)
            }
            else {
                if(pos == position) {
                    if(isDarkMode) {
                        adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.background_field_con_dark))
                        layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.black))
                    }
                    else
                        adapter.setTitleConColor(pos, ContextCompat.getColor(applicationContext, R.color.background_field_con))
                    pos = -1
                    viewPager.visibility = View.GONE
                    keysLayout.visibility = View.VISIBLE
                }
                else {
                    viewPager.setCurrentItem(position, false)
                }
            }

        }
    }

    override fun onConClick(conNum: String, conName: String) {
        val mimeTypes: Array<String> = EditorInfoCompat.getContentMimeTypes(currentInputEditorInfo)

        when(conName.substring(conName.length - 3, conName.length)) {
            "png" -> {
                val pngSupported: Boolean = ("image/png" in mimeTypes) or ("image/*" in mimeTypes)

                if(pngSupported) {
                    commitPNG(conNum, conName)
                }
                else {
                    Toast.makeText(applicationContext, "이미지를 어디로 보낼까요?", Toast.LENGTH_SHORT).show()
                    val path = (File(getPath(conNum), conName))
                    val uri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".fileprovider", path)
                    val intent = Intent(this, ShareActivity::class.java)
                    intent.putExtra("uri", uri.toString())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }
            }
            "gif" -> {
                val gifSupported: Boolean = ("image/gif" in mimeTypes) or ("image/*" in mimeTypes)

                if(gifSupported) {
                    commitGIF(conNum, conName)
                }
                else {
                    Toast.makeText(applicationContext, "이미지를 어디로 보낼까요?", Toast.LENGTH_SHORT).show()
                    val path = (File(getPath(conNum), conName))
                    val uri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".fileprovider", path)
                    val intent = Intent(this, ShareActivity::class.java)
                    intent.putExtra("uri", uri.toString())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        val diff = newSelEnd - oldSelEnd

        if(isKoreanInputting) {
            if((diff != 1 && diff != 0) || !TextUtils.isEmpty(ic.getSelectedText(0))) {
                ic.finishComposingText()
                isKoreanInputting = false
            }
        }
    }

    private fun loadPNG(path: File, name: String): Bitmap? {
        return try {
            val f = File(path, name)
            val b = f.readBytes()
            BitmapFactory.decodeByteArray(b, 0, b.size)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun getPath(conNum: String): File {
        val directory = ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, conNum)
        if(!file.exists()) {
            file.mkdir()
        }
        return file
    }

    private fun commitPNG(conNum: String, conName: String) {
        val path = (File(getPath(conNum), conName))
        val uri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".fileprovider", path)
        val inputContentInfo = InputContentInfoCompat(
            uri,
            ClipDescription("dccon", arrayOf("image/png")),
            null
        )
        var flags = 0
        flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        InputConnectionCompat.commitContent(currentInputConnection, currentInputEditorInfo, inputContentInfo, flags, null)
    }

    private fun commitGIF( conNum: String, conName: String) {
        val path = (File(getPath(conNum), conName))
        val uri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".fileprovider", path)
        val inputContentInfo = InputContentInfoCompat(
            uri,
            ClipDescription("dccon", arrayOf("image/gif")),
            null
        )
        var flags = 0
        flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        InputConnectionCompat.commitContent(currentInputConnection, currentInputEditorInfo, inputContentInfo, flags, null)
    }

}