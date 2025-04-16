package io.github.coden256.wpl

import android.content.Context
import android.util.Log
import com.celzero.bravedns.database.CustomDomain
import com.celzero.bravedns.database.RethinkDnsEndpoint
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.util.Constants
import io.github.coden.dictator.service.DNSRuling
import io.github.coden.dictator.service.DomainRuling
import io.github.coden.dictator.service.Guard
import java.util.Calendar

object RethinkGuardController {
    private val connector = GuardConnector()
    private val guard: Guard? by connector

    fun init(context: Context) {
        Log.i("Guard", "Attempting Guard Connection")
        connector.connect(context)
    }

    fun isConnected(): Boolean {
        return guard != null
    }

    fun customDomainRules(): List<CustomDomain> {
        return (guard
            ?.domainRulings()
            ?.also { Log.i("Guard", "[domain] got from guard: ${it.map { it.toPrettyString() }}") }
            ?.map { it.asCustomDomain() }
            ?: emptyList())
            .also { Log.i("Guard", "[domain] set domains: ${it.map { it.domain + "->" + DomainRulesManager.Status.getStatus(it.status) }}") }
    }

    fun getDnsEndpoint(): RethinkDnsEndpoint? {
        return guard
            ?.dnsRulings()
            ?.also { Log.i("Guard", "[dns] got from guard: ${it.map { it.toPrettyString() }}") }
            ?.firstOrNull { it.action == "FORCE" }
            ?.asRethinkDnsEndpoint()
            .also {  }
    }

    fun onDnsUpdate(url: String?){
        Log.i("Guard", "[dns] set url: $url")
    }

    fun onDomainUpdate(total: Long?){
        Log.i("Guard", "[domain] updated: $total domain rules")
    }

    private fun DNSRuling.asRethinkDnsEndpoint(): RethinkDnsEndpoint {
        return RethinkDnsEndpoint(
            blocklistCount = 10,
            desc = "Custom configurable endpoint.", isActive = true, isCustom = true, latency = 0,
            modifiedDataTime = 1731774271565L, name = "RDNS Plus", uid = -2000,
            url = "https://max.rethinkdns.com/$dns"
        )
    }

    private fun DNSRuling.toPrettyString(): String{
        return "dns{$dns: $action}"
    }

    private fun DomainRuling.toPrettyString(): String{
        return "dns{$domain: $action}"
    }

    private fun DomainRuling.asCustomDomain(): CustomDomain {
        return domain(
            this.domain, when (this.action) {
                "FORCE" -> DomainRulesManager.Status.TRUST
                "BLOCK" -> DomainRulesManager.Status.BLOCK
                "ALLOW" -> DomainRulesManager.Status.NONE
                else -> {
                    Log.w(
                        "RethinkGuardService",
                        "Got invalid action: '$action' -> using Status.NONE"
                    )
                    DomainRulesManager.Status.NONE
                }
            }, DomainRulesManager.DomainType.WILDCARD
        )
    }

    private fun domain(
        domain: String,
        status: DomainRulesManager.Status,
        type: DomainRulesManager.DomainType
    ): CustomDomain {
        return CustomDomain(
            domain,
            Constants.UID_EVERYBODY,
            "",
            type.id,
            status.id,
            Calendar.getInstance().timeInMillis,
            Constants.INIT_TIME_MS,
            CustomDomain.getCurrentVersion()
        )
    }
}