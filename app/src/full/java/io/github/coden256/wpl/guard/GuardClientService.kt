package io.github.coden256.wpl.guard

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.celzero.bravedns.data.AppConfig
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.util.Constants
import com.celzero.bravedns.util.Constants.Companion.UID_EVERYBODY
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class GuardClientService: Service() {

    val appConfig: AppConfig by inject<AppConfig>()

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "Guard Client received a new connection: $intent")
        return object: GuardClient.Stub() {
            override fun onRulings(newRulings: List<Ruling>) {
                Log.i(TAG, "Receiving rulings: ${newRulings.map { it.pretty() }}")
                io {
                    updateDomainRules(newRulings.filterSubPath("/domains/"))
                    updateDns(newRulings.filterSubPath("/dns/"))
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

    private suspend fun updateDns(dns: List<Ruling>){
        val first = dns.firstOrNull {it.action == "FORCE"}
        if (first == null){
            Log.i(TAG, "[dns] No dns provided, really don't know what to do here... Setting to empty")
        }

        val newDns = convertDns(first?.path)

        Log.i(TAG,"[dns] Updating DNS url to: $newDns")

        appConfig.updateRethinkEndpoint(
            Constants.RETHINK_DNS_PLUS,
            newDns,
            1
        )
        appConfig.enableRethinkDnsPlus()
    }

    private fun convertDns(dns: String?): String {
        val d = when{
            dns == null -> "max:"
            !dns.contains(":") -> "max:$dns"
            else -> dns
        }
        val (type, url) = d.split(":")
        return when(type){
            "sky" -> Constants.RETHINK_BASE_URL_SKY
            "max" -> Constants.RETHINK_BASE_URL_MAX
            else -> Constants.RETHINK_BASE_URL_MAX
        } + url
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