package com.example.darkknight.click

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.example.darkknight.click.POJOs.ImageObject
import kotlinx.android.synthetic.main.activity_add_pic.*
import com.google.android.gms.location.places.ui.PlacePicker
import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import android.location.Geocoder
import android.view.Menu
import com.example.darkknight.click.Extentions.dbHelper
import com.example.darkknight.click.Extentions.toastShort
import java.io.IOException

class AddPicActivity : AppCompatActivity() {

    val PLACE_PICKER_REQUEST = 1

    var isEdit: Boolean = false

    private lateinit var imageData: ImageObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pic)
        setSupportActionBar(toolbar_add_pic)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //imageData = ImageObject(0, "no uri", "0", "0", "0", "0")

        imageData = intent.getParcelableExtra("imageData") as ImageObject

        if (imageData.id != -1) {
            isEdit = true
            btn_add_pic.text = "Edit"
            tv_location_image.text = imageData.locationName
            et_caption_image.setText(imageData.caption)
            et_caption_image.setSelection(imageData.caption.length)
        }

        Glide.with(this).load(imageData.imageUri).into(iv_image)

        tv_location_image.setOnClickListener {

            val builder = PlacePicker.IntentBuilder()
            val bounders = LatLngBounds.Builder()
            if (imageData.latitude != "") {
                bounders.include(LatLng(imageData.latitude.toDouble(), imageData.longitude.toDouble()))
                builder.setLatLngBounds(bounders.build())
            }
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        }

        btn_add_pic.setOnClickListener {

            imageData.caption = et_caption_image.text.toString()

            if (isEdit) {
                if (dbHelper.updateImage(imageData)) {
                    finish()
                }
            } else {
                if (dbHelper.insertImage(imageData)) {
                    finish()
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                if (!isEdit) {
                    finish()
                } else {
                    if (dbHelper.deleteImage(imageData) > 0) {
                        finish()
                    }
                }
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                val place = PlacePicker.getPlace(this, data)

                val placeName: String

                placeName = if (place.name.contains("\"N", true) ||
                        place.name.contains("\"S", true) ||
                        place.name.contains("\"E", true) ||
                        place.name.contains("\"W", true)) {
                    place.address!!.split(",")[0]

                } else {
                    place.name.toString()
                }

                val geoCoder = Geocoder(this)

                try {
                    val addresses = geoCoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)
                    val city = addresses[0].subAdminArea
                    tv_location_image.text = "$placeName, $city"

                } catch (e: IOException) {
                    e.printStackTrace()
                    tv_location_image.text = placeName

                }

                imageData.locationName = tv_location_image.text.toString()
                imageData.latitude = place.latLng.latitude.toString()
                imageData.longitude = place.latLng.longitude.toString()

            }
        }
    }
}
