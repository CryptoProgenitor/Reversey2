package com.quokkalabs.thehunt.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.quokkalabs.thehunt.R

class SoundPlayer(private val context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val snapId = soundPool.load(context, R.raw.snap, 1)
    private var fanfarePlayer: MediaPlayer? = null

    fun snap() {
        soundPool.play(snapId, 1f, 1f, 1, 0, 1f)
    }

    fun fanfare() {
        fanfarePlayer?.release()
        fanfarePlayer = MediaPlayer.create(context, R.raw.fanfare)?.apply {
            setOnCompletionListener { player ->
                player.release()
                if (fanfarePlayer === player) fanfarePlayer = null
            }
            start()
        }
    }

    fun release() {
        soundPool.release()
        fanfarePlayer?.release()
        fanfarePlayer = null
    }
}
