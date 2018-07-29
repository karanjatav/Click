package com.example.darkknight.click

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.darkknight.click.POJOs.ImageObject
import kotlinx.android.synthetic.main.item_image_list.view.*

class ImagesAdapter(private val context: Context, private val images: ArrayList<ImageObject>, private val isGrid: Boolean) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (isGrid) {
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_grid, parent, false))
        } else {
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_list, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context).load(images[position].imageUri).into(holder.ivImageItem)
        holder.tvLocationItem.text = images[position].locationName
        holder.tvCaptionItem.text = images[position].caption

        if (!isGrid) {

            if (!TextUtils.isEmpty(holder.tvLocationItem.text) && holder.tvLocationItem.text != context.getString(R.string.add_location)) {
                holder.tvLocationItem.visibility = View.VISIBLE
                holder.llDetailsImage.visibility = View.VISIBLE
            } else {
                holder.tvLocationItem.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(holder.tvCaptionItem.text)) {
                holder.tvCaptionItem.visibility = View.VISIBLE
                holder.llDetailsImage.visibility = View.VISIBLE
            } else {
                holder.tvCaptionItem.visibility = View.GONE
            }
        }


        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, AddPicActivity::class.java).apply { putExtra("imageData", images[position]) })
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivImageItem = view.iv_image_item
        val tvLocationItem = view.tv_location_item
        val tvCaptionItem = view.tv_caption_item
        val llDetailsImage = view.ll_details_image


    }
}