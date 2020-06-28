package com.enviroclean.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowChatLeftListBinding
import com.enviroclean.databinding.RowChatRightListBinding
import com.enviroclean.utils.AppUtils
import com.twilio.chat.Message

/**
 * Created by imobdev on 24/2/20
 */
class MessageListAdapter(
    var context: Context,
    private val chatUserName: String,
    private val chatUserImage: String,
    private val userImage: String
) : BaseBindingAdapter<Message>() {
    private val MSG_TYPE_LEFT = 0
    private val MSG_TYPE_RIGHT = 2

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return if (viewType == MSG_TYPE_LEFT) {
            RowChatLeftListBinding.inflate(inflater, parent, false)
        } else {
            RowChatRightListBinding.inflate(inflater, parent, false)
        }
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        when (getItemViewType(position)) {
            MSG_TYPE_RIGHT -> {
                val binding: RowChatRightListBinding = holder.binding as RowChatRightListBinding
                val item = items[position]!!
                Log.e("DATE", "" + item.dateCreated)
                Log.e("DATE", "" + item.dateCreatedAsDate)
                binding.ivChatImage.tag = this
                binding.ivChatVideoRightThumb.tag = this
                binding.ivChatVideoRight.tag = this
                if (userImage.isNotEmpty()) {
                    AppUtils.loadImages(binding.root.context, binding.ivChatUser, userImage, true)
                }


                if (item.hasMedia()) {
                    binding.tvMessage.visibility = View.GONE
                    if (item.media.type == "image/*") {
                        if (AppUtils.fileIsSaveOrNot(item.media.fileName).isNotEmpty()) {
                            binding.clChatImage.visibility = View.VISIBLE
                            binding.clDownloadImgRight.visibility = View.GONE
                            binding.clChatVideoRight.visibility = View.GONE
                            setImage(
                                AppUtils.fileIsSaveOrNot(item.media.fileName),
                                binding.ivChatImage
                            )

                        } else {
                            binding.clChatImage.visibility = View.GONE
                            binding.clDownloadImgRight.visibility = View.VISIBLE
                            binding.clChatVideoRight.visibility = View.GONE
                            binding.tvMessage.visibility = View.GONE
                        }
                    }
                    if (item.media.type == "video/*") {
                        if (AppUtils.fileIsSaveOrNot(item.media.fileName).isNotEmpty()) {
                            val thumb = ThumbnailUtils.createVideoThumbnail(
                                AppUtils.fileIsSaveOrNot(item.media.fileName),
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                            val bmp = BitmapDrawable(context.resources, thumb)
                            binding.ivChatVideoRightThumb.background = bmp
                        }
                        binding.clChatVideoRight.visibility = View.VISIBLE
                        binding.clChatImage.visibility = View.GONE
                        binding.clDownloadImgRight.visibility = View.GONE
                        binding.tvMessage.visibility = View.GONE
                    }
                } else {
                    binding.tvMessage.text = item.messageBody
                    binding.tvMessage.visibility = View.VISIBLE
                    binding.clChatImage.visibility = View.GONE
                    binding.clDownloadImgRight.visibility = View.GONE
                    binding.clChatVideoRight.visibility = View.GONE
                }
            }
            MSG_TYPE_LEFT -> {
                val binding: RowChatLeftListBinding = holder.binding as RowChatLeftListBinding
                val item = items[position]!!
                Log.e("DATE", "" + item.dateCreated)
                Log.e("DATE", "" + item.dateCreatedAsDate)
                binding.ivChatImage.tag = this
                binding.ivChatVideoLeftThumb.tag = this
                binding.ivChatVideoLeft.tag = this
                if (chatUserImage.isNotEmpty()) {
                    AppUtils.loadImages(
                        binding.root.context,
                        binding.ivChatUser,
                        chatUserImage,
                        true
                    )
                }

                if (item.hasMedia()) {
                    binding.tvMessage.visibility = View.GONE
                    if (item.media.type == "image/*") {
                        if (AppUtils.fileIsSaveOrNot(item.media.fileName).isNotEmpty()) {
                            binding.clChatImage.visibility = View.VISIBLE
                            binding.clDownloadImgLeft.visibility = View.GONE
                            binding.clChatVideoLeft.visibility = View.GONE
                            setImage(
                                AppUtils.fileIsSaveOrNot(item.media.fileName),
                                binding.ivChatImage
                            )
                        } else {
                            binding.clChatImage.visibility = View.GONE
                            binding.clDownloadImgLeft.visibility = View.VISIBLE
                            binding.clChatVideoLeft.visibility = View.GONE
                        }
                    }
                    if (item.media.type == "video/*") {
                        if (AppUtils.fileIsSaveOrNot(item.media.fileName).isNotEmpty()) {
                            val thumb = ThumbnailUtils.createVideoThumbnail(
                                AppUtils.fileIsSaveOrNot(item.media.fileName),
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                            val bmp = BitmapDrawable(context.resources, thumb)
                            binding.ivChatVideoLeftThumb.background = bmp
                        }
                        binding.clChatVideoLeft.visibility = View.VISIBLE
                        binding.clChatImage.visibility = View.GONE
                        binding.clDownloadImgLeft.visibility = View.GONE
                        binding.tvMessage.visibility = View.GONE
                    }
                } else {
                    binding.tvMessage.text = item.messageBody
                    binding.tvMessage.visibility = View.VISIBLE
                    binding.clChatImage.visibility = View.GONE
                    binding.clDownloadImgLeft.visibility = View.GONE
                    binding.clChatVideoLeft.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position]!!.author == "system")
            MSG_TYPE_LEFT
        else if (items[position]!!.author == chatUserName) {
            MSG_TYPE_LEFT
        } else {
            MSG_TYPE_RIGHT
        }
    }

    private fun setImage(file: String, imageView: ImageView) {
        Glide.with(context)
            .load(file).thumbnail(0.5f)
            .fitCenter()
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).dontAnimate()
            )
            .into(imageView)
    }
}