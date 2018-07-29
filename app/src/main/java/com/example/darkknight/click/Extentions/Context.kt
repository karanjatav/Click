package com.example.darkknight.click.Extentions

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.darkknight.click.*
import com.example.darkknight.click.POJOs.UserObject

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getSharedPrefs(): SharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Context.prefs
    get() = getSharedPrefs()

fun Context.saveUser(userId: Int, name: String, email: String, profilePic: String, socialId: String) {
    prefs.edit().putInt(USER_ID, userId).apply()
    prefs.edit().putString(USER_NAME, name).apply()
    prefs.edit().putString(USER_EMAIL, email).apply()
    prefs.edit().putString(USER_PROFILE_PIC, profilePic).apply()
    prefs.edit().putString(USER_SOCIAL_ID, socialId).apply()
}

fun Context.removeUser() {
    prefs.edit().putInt(USER_ID, 0).apply()
    prefs.edit().putString(USER_NAME, null).apply()
    prefs.edit().putString(USER_EMAIL, null).apply()
    prefs.edit().putString(USER_PROFILE_PIC, null).apply()
    prefs.edit().putString(USER_SOCIAL_ID, null).apply()
}

fun Context.toastShort(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}


fun Context.toastLong(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_LONG).show()
}

fun Context.toastTest() {
    Toast.makeText(this, "TESTING TOAST", Toast.LENGTH_LONG).show()
}
