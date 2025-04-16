package io.github.coden256.wpl

import Logger
import Logger.LOG_TAG_SCHEDULER
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.database.RefreshDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GuardRulingUpdater(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {
    private val refreshDatabase by inject<RefreshDatabase>()
    private val appConfig by inject<AppConfig>()

    override suspend fun doWork(): Result {
        updateRulings()
        return Result.success()
    }

    private suspend fun updateRulings() {
        if (!RethinkGuardController.isConnected()){
            Logger.i(LOG_TAG_SCHEDULER, "Guard disconnected, attempting connection")
            RethinkGuardController.init(context)
        }
        Logger.i(LOG_TAG_SCHEDULER, "Updating Domain Rulings")
        refreshDatabase.refresh(RefreshDatabase.ACTION_REFRESH_AUTO) // updates domains

        Logger.i(LOG_TAG_SCHEDULER, "Updating DNS Rulings")
        RethinkGuardController.getDnsEndpoint() // triggers update of DNS rulings, if they changed
            ?.let { appConfig.handleRethinkChanges(it) }

    }
}