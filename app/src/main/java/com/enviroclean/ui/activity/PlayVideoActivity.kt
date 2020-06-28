package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityPlayVideoBinding
import com.enviroclean.utils.AppConstants
import com.enviroclean.viewmodel.PlayVideoViewModel
import com.enviroclean.viewmodel.SplashViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

class PlayVideoActivity : BaseActivity<PlayVideoViewModel>(PlayVideoActivity::class.java.simpleName),
    Player.EventListener {

    private lateinit var binding:ActivityPlayVideoBinding
    lateinit var player: SimpleExoPlayer
    private lateinit var videoSource: ProgressiveMediaSource

    private val mViewModel: PlayVideoViewModel by lazy {
        ViewModelProviders.of(this).get(PlayVideoViewModel::class.java)
    }

    /*Overriden method to get the viewModel in BaseActivity*/
    override fun getViewModel(): PlayVideoViewModel {
        return mViewModel
    }
    private val fileName: String by lazy {
        intent.getStringExtra("FileName")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this
                ,R.layout.activity_play_video)
    }
    companion object {
        fun newInstance(
            context: Context,
            fileName: String): Intent {
            val intent = Intent(context, PlayVideoActivity::class.java)
            intent.putExtra("FileName", fileName)
            return intent
        }
    }

    override fun onResume() {
        super.onResume()
        playerInit()
    }

    private fun playerInit() {
        player = ExoPlayerFactory.newSimpleInstance(
          this,
            DefaultTrackSelector(), DefaultLoadControl()
        )
        player.addListener(this)
        binding.videoView.useController=true
        binding.videoView.player = player
        val uri =
            Uri.parse(fileName)
        videoPlayer(uri)

    }


    private fun videoPlayer(uri: Uri?) {
        val dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "EnviroClean")
        )
// This is the MediaSource representing the media to be played.
        videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
// Prepare the player with the source.
        player.prepare(videoSource)
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                // status = "LOADING"
            }
            Player.STATE_ENDED -> {
                //binding.videoView.useController = false
                // binding.ivPlay.visibility = View.VISIBLE
                //status = "STOPPED"
            }
            Player.STATE_IDLE -> {
                // status = "IDLE"
            }
            Player.STATE_READY -> {
                //status = if (playWhenReady) "PLAYING" else "PAUSED"
            }
            else -> {
                //status = "IDLE"
            }
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Log.e("TAG", "----------------->1")
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        Log.e("TAG", "----------------->2")

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Log.e("TAG", "----------------->3")

    }


    override fun onLoadingChanged(isLoading: Boolean) {
        Log.e("TAG", "----------------->4$isLoading")

    }


    override fun onRepeatModeChanged(repeatMode: Int) {
        Log.e("TAG", "----------------->5")

    }
}
