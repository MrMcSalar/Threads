package edu.farmingdale.threadsexample.countdowntimer

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.farmingdale.threadsexample.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel(private val context: Context) : ViewModel() {
    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    var selectedHour by mutableIntStateOf(0)
        private set
    var selectedMinute by mutableIntStateOf(0)
        private set
    var selectedSecond by mutableIntStateOf(0)
        private set

    var totalMillis by mutableLongStateOf(0L)
        private set

    var remainingMillis by mutableLongStateOf(0L)
        private set

    var isRunning by mutableStateOf(false)
        private set

    fun selectTime(hour: Int, min: Int, sec: Int) {
        selectedHour = hour
        selectedMinute = min
        selectedSecond = sec
    }

    fun startTimer() {
        // Convert hours, minutes, and seconds to milliseconds
        totalMillis = (selectedHour * 60 * 60 + selectedMinute * 60 + selectedSecond) * 1000L


        if (totalMillis > 0) {
            isRunning = true
            remainingMillis = totalMillis

            timerJob = viewModelScope.launch {
                while (remainingMillis > 0) {
                    delay(1000)
                    remainingMillis -= 1000
                }


                isRunning = false
                playAlarmSound()
            }
        }
    }

    fun cancelTimer() {
        if (isRunning) {
            timerJob?.cancel()
            isRunning = false
            remainingMillis = 0
        }
    }

    fun resetTimer() {
        remainingMillis = (selectedHour * 3600000L) + (selectedMinute * 60000L) + (selectedSecond * 1000L)
        isRunning = false
    }

    private fun playAlarmSound() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mediaPlayer?.release()
    }
}