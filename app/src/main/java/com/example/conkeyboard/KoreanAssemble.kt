package com.example.conkeyboard

class KoreanAssemble {
    fun makeDiphthong(s1: String, s2: String): String {
        when(s1) {
            "ㅜ" -> {
                when(s2) {
                    "ㅣ" -> return "ㅟ"
                    "ㅔ" -> return "ㅞ"
                    "ㅓ" -> return "ㅝ"
                }
            }
            "ㅗ" -> {
                when(s2) {
                    "ㅐ" -> return "ㅙ"
                    "ㅏ" -> return "ㅘ"
                    "ㅣ" -> return "ㅚ"
                }
            }
            "ㅡ" -> {
                when(s2) {
                    "ㅣ" -> return "ㅢ"
                }
            }
        }
        return "error"
    }
    fun makeDoubleConsonant(s1: String, s2: String): String {
        when(s1) {
            "ㄱ" -> {
                if(s2 == "ㅅ")
                    return "ㄳ"
            }
            "ㄴ" -> {
                when(s2) {
                    "ㅈ" -> return "ㄵ"
                    "ㅎ" -> return "ㄶ"
                }
            }
            "ㄹ" -> {
                when(s2) {
                    "ㄱ" -> return "ㄺ"
                    "ㅁ" -> return "ㄻ"
                    "ㅂ" -> return "ㄼ"
                    "ㅅ" -> return "ㄽ"
                    "ㅌ" -> return "ㄾ"
                    "ㅍ" -> return "ㄿ"
                    "ㅎ" -> return "ㅀ"
                }
            }
            "ㅂ" -> {
                if(s2 == "ㅅ")
                    return "ㅄ"
            }
        }
        return "error"
    }
}