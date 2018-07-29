package com.example.darkknight.click

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.darkknight.click.Extentions.getIntValue
import com.example.darkknight.click.Extentions.getStringValue
import com.example.darkknight.click.POJOs.ImageObject
import com.example.darkknight.click.POJOs.UserObject

class DBHelper private constructor(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val IMAGES_TABLE_NAME = "images"
    private val COL_ID = "id"
    private val COL_IMAGE_URI = "image_uri"
    private val COL_CAPTION = "caption"
    private val COL_LATITUDE = "latitude"
    private val COL_LONGITUDE = "longitude"
    private val COL_LOCATION_NAME = "location_name"

    private val USERS_TABLE_NAME = "users"
    private val COL_USER_ID = "user_id"
    private val COL_PROFILE_PIC = "profile_pic"
    private val COL_USER_NAME = "user_name"
    private val COL_EMAIL = "email"
    private val COL_SOCIAL_ID = "social_id"
    private val COL_PASSWORD = "password"

    private val mDb = writableDatabase


    companion object {
        private const val DB_VERSION = 1
        const val DB_NAME = "click.db"
        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null)
                dbInstance = DBHelper(context)

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $IMAGES_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $COL_USER_ID INTEGER," +
                " $COL_IMAGE_URI TEXT," +
                " $COL_CAPTION TEXT, " +
                "$COL_LATITUDE TEXT," +
                " $COL_LONGITUDE TEXT," +
                " $COL_LOCATION_NAME TEXT)")

        db.execSQL("CREATE TABLE $USERS_TABLE_NAME ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $COL_PROFILE_PIC TEXT," +
                " $COL_USER_NAME TEXT, " +
                "$COL_EMAIL TEXT," +
                " $COL_SOCIAL_ID TEXT," +
                " $COL_PASSWORD TEXT)")

        //      insertInitialAlarms(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun updateImage(image: ImageObject): Boolean {
        val selectionArgs = arrayOf(image.id.toString())
        val values = fillImageContentValues(image)
        val selection = "$COL_ID = ?"
        return mDb.update(IMAGES_TABLE_NAME, values, selection, selectionArgs) == 1
    }

    fun updateUser(user: UserObject): Boolean {
        val selectionArgs = arrayOf(user.userId.toString())
        val values = fillUserContentValues(user)
        val selection = "$COL_USER_ID = ?"
        return mDb.update(USERS_TABLE_NAME, values, selection, selectionArgs) == 1
    }


    fun deleteImage(image: ImageObject): Int {
        val args = image.id.toString()
        val selection = "$IMAGES_TABLE_NAME.$COL_ID IN ($args)"
        return mDb.delete(IMAGES_TABLE_NAME, selection, null)
    }

    //fun getImageWithId(id: Int) = getImages().firstOrNull { it.id == id }

    fun getUserWithId(id: Int) = getUsers().firstOrNull { it.userId == id }

    fun getUserByEmail(email: String) = getUsers().firstOrNull { it.email == email }

    fun getUserBySocial(socialId: String) = getUsers().firstOrNull { it.socialId == socialId }


    fun insertImage(image: ImageObject, db: SQLiteDatabase = mDb): Boolean {
        val values = fillImageContentValues(image)
        return db.insert(IMAGES_TABLE_NAME, null, values).toInt() != -1
    }

    fun insertUser(user: UserObject, db: SQLiteDatabase = mDb): Boolean {
        val values = fillUserContentValues(user)
        return db.insert(USERS_TABLE_NAME, null, values).toInt() != -1
    }

    private fun fillImageContentValues(image: ImageObject): ContentValues {
        return ContentValues().apply {
            put(COL_USER_ID, image.userId)
            put(COL_IMAGE_URI, image.imageUri)
            put(COL_CAPTION, image.caption)
            put(COL_LATITUDE, image.latitude)
            put(COL_LONGITUDE, image.longitude)
            put(COL_LOCATION_NAME, image.locationName)
        }
    }

    private fun fillUserContentValues(user: UserObject): ContentValues {
        return ContentValues().apply {
            put(COL_PROFILE_PIC, user.profile_pic)
            put(COL_USER_NAME, user.name)
            put(COL_EMAIL, user.email)
            put(COL_SOCIAL_ID, user.socialId)
            put(COL_PASSWORD, user.password)
        }
    }


    fun getUsers(): ArrayList<UserObject> {
        val users = ArrayList<UserObject>()
        val cols = arrayOf(COL_USER_ID, COL_PROFILE_PIC, COL_USER_NAME, COL_EMAIL, COL_SOCIAL_ID, COL_PASSWORD)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(USERS_TABLE_NAME, cols, null, null, null, null, null)
            if (cursor?.moveToFirst() == true) {
                do {
                    try {
                        val userId = cursor.getIntValue(COL_USER_ID)
                        val profilPic = cursor.getStringValue(COL_PROFILE_PIC)
                        val userName = cursor.getStringValue(COL_USER_NAME)
                        val email = cursor.getStringValue(COL_EMAIL)
                        val socialId = cursor.getStringValue(COL_SOCIAL_ID)
                        val password = cursor.getStringValue(COL_PASSWORD)

                        val user = UserObject(userId, profilPic, userName, email, socialId, password)
                        users.add(user)
                    } catch (e: Exception) {
                        continue
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }
        return users
    }

    fun getImages(userId: Int): ArrayList<ImageObject> {
        val images = ArrayList<ImageObject>()
        val cols = arrayOf(COL_ID, COL_USER_ID, COL_IMAGE_URI, COL_CAPTION, COL_LATITUDE, COL_LONGITUDE, COL_LOCATION_NAME)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(IMAGES_TABLE_NAME, cols, COL_USER_ID + "=$userId", null, null, null, "$COL_ID DESC")
            if (cursor?.moveToFirst() == true) {
                do {
                    try {
                        val id = cursor.getIntValue(COL_ID)
                        val userId = cursor.getIntValue(COL_USER_ID)
                        val imageUri = cursor.getStringValue(COL_IMAGE_URI)
                        val caption = cursor.getStringValue(COL_CAPTION)
                        val latitude = cursor.getStringValue(COL_LATITUDE)
                        val longitude = cursor.getStringValue(COL_LONGITUDE)
                        val location_name = cursor.getStringValue(COL_LOCATION_NAME)

                        val image = ImageObject(id, userId, imageUri, caption, latitude, longitude, location_name)
                        images.add(image)
                    } catch (e: Exception) {
                        continue
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }
        return images
    }
}