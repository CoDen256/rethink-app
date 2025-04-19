package io.github.coden256.wpl.guard

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuardClientService: Service() {
    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "Guard Client received a new connection: $intent")
        return object: GuardClient.Stub() {
            override fun onRulings(newRulings: List<Ruling>) {
                Log.i(TAG, "Receiving rulings: ${newRulings.map { it.pretty() }}")

                io {
                    updateDomainRules(newRulings.filterSubPath("/domains/"))
                }
            }
        }
    }

    private suspend fun updateDomainRules(domains: List<Ruling>){
        Log.i(TAG, "[domain] Removing all rules")
        DomainRulesManager.deleteAllRules()

        domains.forEach {
            Log.i(TAG, "[domain] Adding ${it.pretty()}")
            DomainRulesManager.addDomainRule(
                it.path,
                when(it.action) {
                    "BLOCK" -> DomainRulesManager.Status.BLOCK
                    "FORCE" -> DomainRulesManager.Status.TRUST
                    else -> DomainRulesManager.Status.NONE
                },
                DomainRulesManager.DomainType.WILDCARD,
                UID_EVERYBODY
            )
        }
    }




    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Guard Client unbounded: $intent")
        return super.onUnbind(intent)
    }

    companion object {
        const val TAG = "GuardClient"
    }

    private fun io(f: suspend () -> Unit) =
        CoroutineScope(CoroutineName("GuardClient") + Dispatchers.IO).launch { f() }


    private fun List<Ruling>.filterSubPath(subPath: String): List<Ruling>{
        return filter { it.path.startsWith(subPath) }
            .map {  Ruling().apply {path = it.path.removePrefix(subPath); action = it.action}}
            .filterNot { it.path.contains("/") }
            .toList()
    }

    private fun Ruling.pretty(): String{
        return "{$path: $action}"
    }
}