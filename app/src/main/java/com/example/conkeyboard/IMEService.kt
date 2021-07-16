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
import com.github.kimkevin.hangulparser.HangulParser
import com.github.kimkevin.hangulparser.HangulParserException
import java.lang.Exception

class IMEService : InputMethodService(), View.OnTouchListener {
    private lateinit var btnArray: ArrayList<Button>
    private var shiftFlag: Int = 0
    private lateinit var ic: InputConnection
    private lateinit var vibrator: Vibrator
    private lateinit var handler: Handler
    private lateinit var longPressed: Runnable
    private var tmp = 0
    private lateinit var shiftBtn: ImageButton
    private lateinit var spCharBtn: Button
    private var spCharFlag = 0
    private var currentLang = "en"
    private lateinit var nextBtn: Button
    private val enList: List<String> = listOf(
        "q",
        "w",
        "e",
        "r",
        "t",
        "y",
        "u",
        "i",
        "o",
        "p",
        "a",
        "s",
        "d",
        "f",
        "g",
        "h",
        "j",
        "k",
        "l",
        "z",
        "x",
        "c",
        "v",
        "b",
        "n",
        "m",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "0"
    )
    private val koList: List<String> = listOf(
        "ㅂ",
        "ㅈ",
        "ㄷ",
        "ㄱ",
        "ㅅ",
        "ㅛ",
        "ㅕ",
        "ㅑ",
        "ㅐ",
        "ㅔ",
        "ㅁ",
        "ㄴ",
        "ㅇ",
        "ㄹ",
        "ㅎ",
        "ㅗ",
        "ㅓ",
        "ㅏ",
        "ㅣ",
        "ㅋ",
        "ㅌ",
        "ㅊ",
        "ㅍ",
        "ㅠ",
        "ㅜ",
        "ㅡ"
    )
    private val koShiftList: List<String> = listOf("ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅒ", "ㅖ")
    private val spCharList: List<String> = listOf(
        "+",
        "×",
        "÷",
        "=",
        "/",
        "_",
        "<",
        ">",
        "♡",
        "☆",
        "!",
        "@",
        "#",
        "%",
        "^",
        "&",
        "*",
        "(",
        ")",
        "~",
        "-",
        "\'",
        "\"",
        ":",
        ";",
        ",",
        "?",
        "￦",
        "\\",
        "|",
        "♤",
        "♧",
        "{",
        "}",
        "[",
        "]",
        "`",
        "•",
        "○",
        "●",
        "□",
        "■",
        "◇",
        "\$",
        "€",
        "£",
        "¥",
        "°",
        "《",
        "》",
        "¡",
        "¿"
    )
    private var isKoreanInputting: Boolean = false

    override fun onCreateInputView(): View {
        val container = LinearLayout(applicationContext)
        container.orientation = LinearLayout.VERTICAL
        container.setBackgroundColor(Color.RED)
        val height = resources.displayMetrics.heightPixels
        val width = resources.displayMetrics.widthPixels
        val w = width / 200
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val keyboardView: View = layoutInflater.inflate(R.layout.layout_keyboard, container, true)
        val layout: LinearLayout = keyboardView.findViewById(R.id.layout)
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (height * 0.4).toInt()
        )

        btnArray = ArrayList()
        val keyList: List<String> = enList + listOf("spaceBar", "period", "question")
        for(i in 0..38){
            val id: String = "btn_" + keyList[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            btnArray.add(keyboardView.findViewById(resourceID) as Button)
            btnArray[i].setOnTouchListener(this)
            val background: GradientDrawable = btnArray[i].background as GradientDrawable
            background.setStroke(
                w, ContextCompat.getColor(
                    applicationContext,
                    R.color.background_gray
                )
            )
        }

        val imgBtnArray: List<String> = listOf("del", "lang", "enter", "shift")
        for(i in 0..3) {
            val id: String = "btn_" + imgBtnArray[i]
            val resourceID: Int = resources.getIdentifier(id, "id", packageName)
            val imgBtn: ImageButton = keyboardView.findViewById(resourceID) as ImageButton
            imgBtn.setOnTouchListener(this)
            if(i == 1) {
                (imgBtn.background as GradientDrawable).setStroke(
                    w, ContextCompat.getColor(
                        applicationContext,
                        R.color.background_gray
                    )
                )
                imgBtn.setPadding(w * 5)
            }
            else {
                (imgBtn.background as LayerDrawable).setLayerInset(
                    1,
                    (w / 5 * 12),
                    w,
                    (w / 5 * 12),
                    w
                )//l t r b
                imgBtn.setPadding((w * 10.5).toInt())
            }
        }
        spCharBtn = keyboardView.findViewById(R.id.btn_spChar)
        nextBtn = keyboardView.findViewById(R.id.btn_next)
        (spCharBtn.background as LayerDrawable).setLayerInset(1, (w / 5 * 12), w, (w / 5 * 12), w)
        (nextBtn.background as LayerDrawable).setLayerInset(1, (w / 5 * 12), w, (w / 5 * 12), w)
        spCharBtn.setOnTouchListener(this)
        nextBtn.setOnTouchListener(this)

        shiftBtn = keyboardView.findViewById(R.id.btn_shift)
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
        return keyboardView
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        ic = currentInputConnection
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                try {
                        (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked))
                }
                catch (e: Exception) {
                    ((v?.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_clicked))
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
                        ((v.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
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
                        ((v.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_spChar -> {
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
                        ((v.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_next -> {
                        changeSpCharPage()
                        ((v.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_lang -> {
                        isKoreanInputting = false
                        if(shiftFlag == 1)
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
                        (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                    }
                    R.id.btn_spaceBar -> {
                        ic.commitText(" ", 1)
                        (v.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
                        isKoreanInputting = false
                    }
                    R.id.btn_enter -> {
                        ic.commitText("\n", 1)
                        ((v.background as LayerDrawable).findDrawableByLayerId(R.id.draw) as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.button_gray))
                        isKoreanInputting = false
                    }
                    else -> {
                        isKoreanInputting = currentLang == "ko"
                        (v?.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, R.color.white))
                        val value: String = (v as Button).text.toString()
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
                                if (s.isNotEmpty() && isKorean(s.toString()) && isKorean(value)) {
                                    koreanInputManager(s.toString(), value)
                                }
                                else {
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
        val consonantList: List<String> = listOf(
            "ㅂ",
            "ㅈ",
            "ㄷ",
            "ㄱ",
            "ㅅ",
            "ㅁ",
            "ㄴ",
            "ㅇ",
            "ㄹ",
            "ㅎ",
            "ㅋ",
            "ㅌ",
            "ㅊ",
            "ㅍ",
            "ㅃ",
            "ㅉ",
            "ㄸ",
            "ㄲ",
            "ㅆ"
        )
        return s in consonantList
    }
    private fun isVowel(s: String): Boolean {
        val vowelList: List<String> = listOf(
            "ㅛ",
            "ㅕ",
            "ㅑ",
            "ㅐ",
            "ㅔ",
            "ㅗ",
            "ㅓ",
            "ㅏ",
            "ㅣ",
            "ㅠ",
            "ㅜ",
            "ㅡ",
            "ㅒ",
            "ㅖ"
        )
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

        if(isConsonant(w)) { //앞 글자가 자음뿐 + 뒤에 모음이 옴
            if(isVowel(value)) {
                val jasoList = listOf(w, value)
                deleteChar(1)
                ic.commitText(HangulParser.assemble(jasoList), 1)
            }
            else {
                ic.commitText(value, 1)
            }
        }
        else if(isVowel(w)) {
            if(isVowel(value)) {
                val result = KoreanAssemble().makeDiphthong(w, value)
                if(result == "error") { //모음뿐 + 다른 모음
                    ic.commitText(value, 1)
                }
                else {
                    deleteChar(1)
                    ic.commitText(result, 1)
                }
            }
            else { //모음 + 자음
                ic.commitText(value, 1)
            }
        }
        else if(isDiphthong(w)) {
            ic.commitText(value, 1)
        }
        else {
            val jasoList: MutableList<String> = HangulParser.disassemble(w)
            when(jasoList.size) {
                2 -> {
                    if (isVowel(value)) { //모음 + 모음
                        val result = KoreanAssemble().makeDiphthong(jasoList[1], value)
                        if (result == "error") { //이중모음 아닌 경우
                            ic.commitText(value, 1)
                        } else { //이중모음
                            jasoList[1] = result
                            deleteChar(1)
                            ic.commitText(HangulParser.assemble(jasoList), 1)
                        }
                    }
                    else {
                        try {
                            jasoList.add(value)
                            val tmp = HangulParser.assemble(jasoList)
                            deleteChar(1)
                            ic.commitText(tmp, 1)
                        }
                        catch (e: HangulParserException) {
                            ic.commitText(value, 1)
                        }

                    }
                }
                3 -> {
                    if (isConsonant(value)) { //받침 + 자음
                        val result = KoreanAssemble().makeDoubleConsonant(jasoList[2], value)
                        if (result == "error") { //겹자음이 아닌 경우
                            ic.commitText(value, 1)
                        } else {
                            jasoList[2] = result
                            deleteChar(1)
                            ic.commitText(HangulParser.assemble(jasoList), 1)
                        }
                    }
                    else if(isConsonant(jasoList[2]) && isVowel(value)) { //받침 + 모음
                        val list: List<String> = listOf(jasoList[2], value)
                        jasoList.removeAt(2)
                        deleteChar(1)
                        ic.commitText(HangulParser.assemble(jasoList), 1)
                        ic.commitText(HangulParser.assemble(list), 1)
                    }
                    else if(isDoubleConsonant(jasoList[2]) && isVowel(value)){ //겹받침 + 모음
                        val list: List<String> = KoreanDisassemble().disassembleDoubleConsonant(jasoList[2])
                        val tmp: List<String> = listOf(list[1], value)
                        jasoList.removeAt(2)
                        jasoList.add(list[0])
                        deleteChar(1)
                        ic.commitText(HangulParser.assemble(jasoList), 1)
                        ic.commitText(HangulParser.assemble(tmp), 1)
                    }
                }
            }
        }
    }
    private fun koreanDeleteManager(s: String) {
        val w = s[0].toString()

        if(isConsonant(w) || isVowel(w)) {
            deleteChar(1)
            isKoreanInputting = false
        }
        else if(isDiphthong(w)) { //이중모음
            val list: List<String> = KoreanDisassemble().disassembleDiphthong(w)
            deleteChar(1)
            ic.commitText(list[0], 1)
        }
        else {
            val jasoList: MutableList<String> = HangulParser.disassemble(w)
            when(jasoList.size) {
                2 -> {
                    if(isDiphthong(jasoList[1])) {
                        val list: List<String> = KoreanDisassemble().disassembleDiphthong(jasoList[1])
                        jasoList[1] = list[0]
                        deleteChar(1)
                        ic.commitText(HangulParser.assemble(jasoList), 1)
                    }
                    else {
                        deleteChar(1)
                        ic.commitText(jasoList[0], 1)
                    }
                }
                3 -> {
                    if(isDoubleConsonant(jasoList[2])) {
                        val list: List<String> = KoreanDisassemble().disassembleDoubleConsonant(jasoList[2])
                        jasoList[2] = list[0]
                        deleteChar(1)
                        ic.commitText(HangulParser.assemble(jasoList), 1)
                    }
                    else {
                        jasoList.removeAt(2)
                        deleteChar(1)
                        ic.commitText(HangulParser.assemble(jasoList), 1)
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
}