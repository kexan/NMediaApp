package ru.netology.nmedia.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        fun <T> List<T>.replacePost(newPost: T, block: (T) -> Boolean): List<T> {
            return map {
                if (block(it)) newPost else it
            }
        }
    }
}