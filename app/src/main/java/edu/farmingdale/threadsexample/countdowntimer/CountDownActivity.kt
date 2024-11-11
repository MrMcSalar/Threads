package edu.farmingdale.threadsexample.countdowntimer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import edu.farmingdale.threadsexample.ui.theme.ThreadsExampleTheme

class CountDownActivity : ComponentActivity() {

    private lateinit var timerViewModel: TimerViewModel

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val message = if (isGranted) "Permission granted" else "Permission NOT granted"
            Log.i("MainActivity", message)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        timerViewModel = ViewModelProvider(
            this,
            TimerViewModelFactory(applicationContext) // Pass the application context to the ViewModelFactory
        ).get(TimerViewModel::class.java)

        setContent {
            ThreadsExampleTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(timerViewModel = timerViewModel) // Pass ViewModel to the composable
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (timerViewModel.isRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    startWorker(timerViewModel.remainingMillis)
                }
            } else {
                startWorker(timerViewModel.remainingMillis)
            }
        }
    }

    private fun startWorker(millisRemain: Long) {
        val timerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInputData(
                workDataOf(
                    KEY_MILLIS_REMAINING.toString() to millisRemain
                )
            ).build()

        WorkManager.getInstance(applicationContext).enqueue(timerWorkRequest)
    }
}
class TimerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Ensure the ViewModel type is TimerViewModel and pass the context to it
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            return TimerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}