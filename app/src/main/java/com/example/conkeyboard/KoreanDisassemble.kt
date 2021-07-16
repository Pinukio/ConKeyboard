package com.example.conkeyboard

class KoreanDisassemble {
    fun disassembleDiphthong(s: String): List<String> {
        return when(s) {
            "ㅟ" -> listOf("ㅜ", "ㅣ")
            "ㅞ" -> listOf("ㅜ", "ㅔ")
            "ㅝ" -> listOf("ㅜ", "ㅓ")
            "ㅙ" -> listOf("ㅗ", "ㅐ")
            "ㅘ" -> listOf("ㅗ", "ㅏ")
            "ㅚ" -> listOf("ㅗ", "ㅣ")
            "ㅢ" -> listOf("ㅡ", "ㅣ")
            else -> listOf()
        }
    }
    fun disassembleDoubleConsonant(s: String): List<String> {
        return when(s) {
            "ㄳ" -> listOf("ㄱ", "ㅅ")
            "ㄵ" -> listOf("ㄴ", "ㅈ")
            "ㄶ" -> listOf("ㄴ", "ㅎ")
            "ㄺ" -> listOf("ㄹ", "ㄱ")
            "ㄻ" -> listOf("ㄹ", "ㅁ")
            "ㄼ" -> listOf("ㄹ", "ㅂ")
            "ㄽ" -> listOf("ㄹ", "ㅅ")
            "ㄾ" -> listOf("ㄹ", "ㅌ")
            "ㄿ" -> listOf("ㄹ", "ㅍ")
            "ㅀ" -> listOf("ㄹ", "ㅎ")
            "ㅄ" -> listOf("ㅂ", "ㅅ")
            else -> listOf()
        }

    }
}