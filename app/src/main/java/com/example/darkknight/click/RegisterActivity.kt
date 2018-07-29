package com.example.darkknight.click

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.darkknight.click.Extentions.*
import com.example.darkknight.click.POJOs.ImageObject
import com.example.darkknight.click.POJOs.UserObject
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_pic.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    var socialData: JSONObject? = null
    private var profilePicUrl: String? = ""
    var socialId: String? = ""
    lateinit var user: UserObject
    var isEdit: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setSupportActionBar(toolbar_reg)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        iv_profile_reg.setOnClickListener {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(this)
        }

        if (intent.getStringExtra("socialData") != null) {
            socialData = JSONObject(intent.getStringExtra("socialData"))
            setupSocial()
            //toastShort("socialId===" + socialData!!.getString("id"))
        }

        btn_register.setOnClickListener { checkAndSaveUser() }

        checkAndSetupData()

    }


    private fun checkAndSetupData() {
        if (prefs.getInt(USER_ID, 0) != 0) {
            user = dbHelper.getUserWithId(prefs.getInt(USER_ID, 0))!!

            toolbar_reg.title = "Edit Profile"

            Glide.with(this).load(user.profile_pic).apply(RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform()).into(iv_profile_reg)

            profilePicUrl = user.profile_pic
            socialId = user.socialId


            et_name_reg.setText(user.name)
            et_name_reg.setSelection(user.name.length)
            et_email_reg.setText(user.email)
            et_password_reg.setText(user.password)
            et_con_password_reg.setText(user.password)

            btn_register.text = "Save"
            isEdit = true
        }
    }


    private fun setupSocial() {
        if (socialData!!.has("id")) {
            socialId = socialData!!.getString("id")
        }
        if (socialData!!.has("picture")) {
            profilePicUrl = socialData!!.getString("picture")
            Glide.with(this).load(profilePicUrl).into(iv_profile_reg)
        }
        if (socialData!!.has("name")) {
            et_name_reg.setText(socialData!!.getString("name"))
        }
        if (socialData!!.has("email")) {
            et_email_reg.setText(socialData!!.getString("email"))
        }
    }


    private fun checkAndSaveUser() {
        if (TextUtils.isEmpty(et_name_reg.text) ||
                TextUtils.isEmpty(et_email_reg.text) ||
                TextUtils.isEmpty(et_password_reg.text) ||
                TextUtils.isEmpty(et_con_password_reg.text)) {
            toastLong("Please fill the empty fields")
        } else if (!isValidEmail(et_email_reg.text.toString())) {
            toastLong("Please enter a valid email")

        } else if (dbHelper.getUserByEmail(et_email_reg.text.toString()) != null && !isEdit) {
            toastLong("User with same Email Id exist")

        } else if (et_password_reg.text.toString().trim().length < 8) {
            toastLong("Password should be of at least 8 letters")

        } else if (et_con_password_reg.text.toString().trim() != et_password_reg.text.toString().trim()) {
            toastLong("Password dont match.!")
        } else {
            if (isEdit) {
                val user = UserObject(user.userId, profilePicUrl.toString().trim(), et_name_reg.text.toString().trim(), et_email_reg.text.toString().trim(), socialId.toString(), et_password_reg.text.toString().trim())

                if (dbHelper.updateUser(user)) {
                    val user1 = dbHelper.getUserByEmail(et_email_reg.text.toString())
                    if (user1 != null) {
                        saveUser(user1.userId, user1.name, user1.email, user1.profile_pic, user1.socialId)
                    }
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finishAffinity()
                }
            } else {
                val user = UserObject(0, profilePicUrl.toString().trim(), et_name_reg.text.toString().trim(), et_email_reg.text.toString().trim(), socialId.toString(), et_password_reg.text.toString().trim())

                if (dbHelper.insertUser(user)) {
                    val user1 = dbHelper.getUserByEmail(et_email_reg.text.toString())
                    if (user1 != null) {
                        saveUser(user1.userId, user1.name, user1.email, user1.profile_pic, user1.socialId)
                    }
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profilePicUrl = result.uri.toString()
                Glide.with(this).load(result.uri).into(iv_profile_reg)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e("CROP_ERROR", error.toString())
            }
        }
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
