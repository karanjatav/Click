package com.example.darkknight.click

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.darkknight.click.Extentions.dbHelper
import com.example.darkknight.click.Extentions.prefs
import com.example.darkknight.click.Extentions.removeUser
import com.example.darkknight.click.POJOs.ImageObject
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var images: ArrayList<ImageObject>

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var itemDecoration: GridSpacingItemDecoration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_menu)

        gridLayoutManager = GridLayoutManager(this, 3)

        itemDecoration = GridSpacingItemDecoration(3, 10, false)

        linearLayoutManager = LinearLayoutManager(this)

        rv_images.layoutManager = linearLayoutManager

        val myRotation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate)


        fb_click.setOnClickListener {
            iv_add_pic.startAnimation(myRotation)
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if (prefs.getString(USER_NAME, null) != null || prefs.getString(USER_NAME, null) != "") {
            val headerView: View = nav_view.getHeaderView(0)

            Glide.with(this).load(prefs.getString(USER_PROFILE_PIC, null)).apply(RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform()).into(headerView.iv_profile_nav)

            headerView.tv_name_nav.text = prefs.getString(USER_NAME, null)
            headerView.tv_email_nav.text = prefs.getString(USER_EMAIL, null)
        }

    }


    override fun onResume() {
        super.onResume()
        images = dbHelper.getImages(prefs.getInt(USER_ID, 0))

        for (image in images) {
            Log.d("IMAGE===", "${image.id}---${image.caption}---${image.imageUri}")
        }

        if (rv_images.layoutManager == linearLayoutManager) {
            rv_images.adapter = ImagesAdapter(this, images, false)
        } else {
            rv_images.adapter = ImagesAdapter(this, images, true)
        }

    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                if (rv_images.layoutManager == linearLayoutManager) {
                    rv_images.layoutManager = gridLayoutManager
                    item.setIcon(R.drawable.ic_list)
                    rv_images.adapter = ImagesAdapter(this, images, true)
                    rv_images.addItemDecoration(itemDecoration)

                } else {
                    rv_images.layoutManager = linearLayoutManager
                    item.setIcon(R.drawable.ic_grid)
                    rv_images.adapter = ImagesAdapter(this, images, false)
                    rv_images.removeItemDecoration(itemDecoration)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
            R.id.nav_settings -> {
            }
            R.id.nav_logout -> {

                FirebaseAuth.getInstance().signOut()

                LoginManager.getInstance().logOut()

                removeUser()

                startActivity(Intent(this, LoginActivity::class.java))
                supportFinishAfterTransition()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                val imageObject = ImageObject(-1, prefs.getInt(USER_ID, 0), "", "", "", "", getString(R.string.add_location))
                imageObject.imageUri = result.uri.toString()

                startActivity(Intent(this, AddPicActivity::class.java).apply { putExtra("imageData", imageObject) })
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e("CROP_ERROR", error.toString())
            }
        }
    }
}
