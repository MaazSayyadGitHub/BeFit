package com.maaz.befit.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.maaz.befit.R

class SoundManager(private val context : Context) {

    companion object {
        private const val TAG = "SoundManager"
        private const val MAX_STREAMS = 5
    }

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()

    init {
        initializeSoundPool()
        loadSounds()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun loadSounds() {
        try {
            // Load sound effects from raw resources
            // Note: You'll need to add actual sound files to res/raw/ directory
            soundMap["water"] = soundPool?.load(context, R.raw.water_drop, 1) ?: 0
            soundMap["step"] = soundPool?.load(context, R.raw.step_sound, 1) ?: 0
            soundMap["sleep"] = soundPool?.load(context, R.raw.sleep_chime, 1) ?: 0
            soundMap["success"] = soundPool?.load(context, R.raw.success_chime, 1) ?: 0
            soundMap["notification"] = soundPool?.load(context, R.raw.notification_sound, 1) ?: 0

            // Load meditation sounds (using existing sounds as placeholders)
            // TODO: Replace with actual meditation sound files when available
            soundMap["rain"] = soundPool?.load(context, R.raw.water_drop, 1) ?: 0
            soundMap["ocean"] = soundPool?.load(context, R.raw.sleep_chime, 1) ?: 0
            soundMap["forest"] = soundPool?.load(context, R.raw.success_chime, 1) ?: 0
            soundMap["white_noise"] = soundPool?.load(context, R.raw.notification_sound, 1) ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Could not load sound files: ${e.message}")
            // Sound files not found - this is expected until you add them
        }
    }

    fun playWaterSound() {
        playSound("water")
    }

    fun playStepSound() {
        playSound("step")
    }

    fun playSleepSound() {
        playSound("sleep")
    }

    fun playSuccessSound() {
        playSound("success")
    }

    fun playNotificationSound() {
        playSound("notification")
    }

    // Meditation sounds
    fun playRainSound() {
        playSound("rain")
    }

    fun playOceanSound() {
        playSound("ocean")
    }

    fun playForestSound() {
        playSound("forest")
    }

    fun playWhiteNoiseSound() {
        playSound("white_noise")
    }

    private fun playSound(soundKey: String) {
        try {
            val soundId = soundMap[soundKey]
            if (soundId != null && soundId > 0) {
                soundPool?.play(
                    soundId,
                    0.7f, // left volume
                    0.7f, // right volume
                    1, // priority
                    0, // loop (0 = no loop)
                    1f // rate
                )
            } else {
                Log.d(TAG, "Sound '$soundKey' not loaded or not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound '$soundKey': ${e.message}")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}