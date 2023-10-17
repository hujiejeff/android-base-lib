package com.hujiejeff.library.base.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.hujiejeff.library.base.base.getApp
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

import java.io.File


object GlideHelper {

    private val crossFadeTransition = DrawableTransitionOptions.withCrossFade()
    private val crossFadeBitmapTransition = BitmapTransitionOptions.withCrossFade()
    private val globalContext = getApp()
    private var placeHolder: Int = 0
    private var error: Int = 0;
    private val basicRequestOptions: RequestOptions =
        RequestOptions()
            .centerCrop()
            .placeholder(placeHolder)
            .error(error)
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

    private val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

    fun setDefaultConfig(placeHolder: Int, error: Int) {
        GlideHelper.placeHolder = placeHolder
        GlideHelper.error = error
    }

    fun loadOnlyFromCache(iv: ImageView, url: String, onSuccessCallback: () -> Unit) {
        Glide.with(iv.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions().onlyRetrieveFromCache(true))
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    iv.postDelayed({
                        iv.setImageDrawable(resource)
                    }, 1000)
                    onSuccessCallback.invoke()
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    @JvmStatic
    fun getFile(context: Context, data: Any): File? {
        try {
            return Glide.with(context).downloadOnly().load(data).submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun loadAsBitmap(
        ctx: Context,
        data: String?,
        target: Target<Bitmap>,
        requestBuildAction: (RequestBuilder<Bitmap>) -> RequestBuilder<Bitmap> = { it }
    ) {
        basicLoadAsBitmap(ctx, data, requestBuildAction, target)
    }


    @JvmStatic
    fun simpleLoad(iv: ImageView, data: Any?) {
        basicSimpleLoad(iv, data)
    }


    @JvmStatic
    fun load(iv: ImageView, data: Any?) {
        basicLoad(iv = iv, data = data)
    }

    @JvmStatic
    fun loadWithOverride(iv: ImageView, data: Any, w: Int, h: Int) {
        basicLoad(iv = iv, data = data, requestBuildAction = {
            it.override(w, h)
        })
    }

    @JvmStatic
    fun load(
        iv: ImageView,
        data: Any,
        requestBuildAction: (RequestBuilder<Drawable>) -> RequestBuilder<Drawable> = { it }
    ) {
        basicLoad(iv = iv, data = data, requestBuildAction = requestBuildAction)
    }

    @JvmStatic
    fun load(iv: ImageView, data: Any?, placeholderRes: Int, errorRes: Int) {
        basicLoad(iv = iv, data = data, requestBuildAction = {
            it.placeholder(placeholderRes)
                .error(errorRes)
        })
    }

    @JvmStatic
    fun load(iv: ImageView, data: Any?, placeholderAndErrorRes: Int) {
        load(iv, data, placeholderAndErrorRes, placeholderAndErrorRes)
    }

    @JvmStatic
    fun cancelLoad(iv: ImageView) {
        Glide.with(iv.context).clear(iv)
    }

    @JvmStatic
    fun clear() {
        Glide.get(globalContext).clearDiskCache();
    }


    private fun basicLoad(
        iv: ImageView,
        data: Any?,
        requestBuildAction: (RequestBuilder<Drawable>) -> RequestBuilder<Drawable> = { it }
    ) {
        Glide.with(iv.context)
            .load(data)
            .apply(basicRequestOptions)
            .transition(crossFadeTransition)
            .let {
                requestBuildAction(it)
            }
            .into(iv)
    }

    private fun basicLoadAsBitmap(
        ctx: Context,
        data: Any?,
        requestBuildAction: (RequestBuilder<Bitmap>) -> RequestBuilder<Bitmap> = { it },
        target: com.bumptech.glide.request.target.Target<Bitmap>
    ) {
        Glide.with(ctx)
            .asBitmap()
            .load(data)
            .apply(basicRequestOptions)
            .transition(crossFadeBitmapTransition)
            .let {
                requestBuildAction(it)
            }
            .into(target)
    }

    private fun basicSimpleLoad(
        iv: ImageView,
        data: Any?,
        requestBuildAction: (RequestBuilder<Drawable>) -> RequestBuilder<Drawable> = { it }
    ) {
        Glide.with(iv.context)
            .load(data)
            .let {
                requestBuildAction(it)
            }
            .into(iv)
    }
}