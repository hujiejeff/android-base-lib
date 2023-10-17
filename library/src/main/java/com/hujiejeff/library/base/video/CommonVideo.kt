package com.hujiejeff.library.base.video

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.EncodeUtils
import com.hujiejeff.library.base.image.GlideHelper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer



class CommonVideo: StandardGSYVideoPlayer {
    constructor(context: Context): super(context)
    constructor(context: Context, fullFlag: Boolean): super(context, fullFlag)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        //处理播放器生命周期
        (context as LifecycleOwner).lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)

            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onVideoPause()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onVideoResume(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                GSYVideoManager.releaseAllVideos();
            }
        })
    }

    /**
     * 复写Ui，保持Id一致
     */
    override fun getLayoutId() = com.shuyu.gsyvideoplayer.R.layout.video_layout_standard

    /**
     * 设置封面
     */
    fun setCover(url: String) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        GlideHelper.load(imageView, url)
        thumbImageView = imageView
    }

    /**
     * 初始化配置
     */
    fun setUp(coverUrl: String, videoUrl: String) {
        setCover(coverUrl)
        val gsyVideoOptionBuilder = GSYVideoOptionBuilder()
        val header: Map<String, String> = HashMap()
        gsyVideoOptionBuilder
            .setIsTouchWiget(false)
            .setUrl(videoUrl)
            .setCacheWithPlay(true)
            .setRotateViewAuto(true)
            .setDismissControlTime(3000)
            .setNeedOrientationUtils(true)
            .setNeedShowWifiTip(false)
            .setPlayTag(videoUrl)
            .setMapHeadData(header)
            .setShowFullAnimation(false)
            .setReleaseWhenLossAudio(true)
            .setNeedLockFull(false)
            .setAutoFullWithSize(false)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onAutoComplete(url: String, vararg objects: Any) {

                }

                override fun onClickStartError(url: String, vararg objects: Any) {}
                override fun onPrepared(url: String, vararg objects: Any) {
                    super.onPrepared(url, *objects)
                }

                override fun onComplete(url: String, vararg objects: Any) {
                    super.onComplete(url, *objects)
                }

                override fun onQuitFullscreen(url: String, vararg objects: Any) {
                    super.onQuitFullscreen(url, *objects)
                }

                override fun onEnterFullscreen(url: String, vararg objects: Any) {
                    super.onEnterFullscreen(url, *objects)
                }

                override fun onClickSeekbarFullscreen(url: String, vararg objects: Any) {
                }

                override fun onClickSeekbar(url: String, vararg objects: Any) {
                }

                override fun onClickResume(url: String, vararg objects: Any) {
                }

                override fun onClickStop(url: String, vararg objects: Any) {
                    super.onClickStop(url, *objects)
                }

                override fun onStartPrepared(url: String, vararg objects: Any) {
                    super.onStartPrepared(url, *objects)

                }
            }).build(this)
        fullscreenButton?.setOnClickListener {
            val width: Int = currentVideoWidth
            val height: Int = currentVideoHeight
            isLockLand = width > height
            startWindowFullscreen(context, true, true)
        }
    }

    fun play() {
        startPlayLogic()
    }

    fun pause() {
        onVideoPause()
    }

    fun resume(seek: Boolean) {
        onVideoResume(seek)
    }

    fun resetPlay() {
        onVideoReset()
        play()
    }
}