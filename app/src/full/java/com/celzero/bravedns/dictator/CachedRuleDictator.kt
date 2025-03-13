package com.celzero.bravedns.dictator

import android.util.Log
import com.celzero.bravedns.database.CustomDomain
import com.celzero.bravedns.database.RethinkDnsEndpoint
import com.celzero.bravedns.service.DomainRulesManager
import com.celzero.bravedns.util.Constants
import java.util.Calendar

class CachedRuleDictator : Dictator {
    // "https://max.rethinkdns.com/1-kdyacbarqaeaiaeiaaeaaaq" mine
    // " https://max.rethinkdns.com/1-abqacaaiaa"   a
    // 1-cbqbcaaiacaaa
    val defaultUrl = "https://max.rethinkdns.com/1-bdaacaaaeaaia" // none: https://max.rethinkdns.com/
    var currentUrl = defaultUrl

    val baseEndpoint = RethinkDnsEndpoint(
        blocklistCount = 10,
        desc = "Custom configurable endpoint.", isActive = true, isCustom = true, latency = 0,
        modifiedDataTime = 1731774271565L, name = "RDNS Plus", uid = -2000,
        url = defaultUrl
    )

    override fun getDnsEndpoint(): RethinkDnsEndpoint {
        return baseEndpoint.also { it.url = currentUrl }.also {
            Log.i("DICTATOR", "current url: ${it.url}")
        }
    }

    val domains = listOf(
//        domain("*.production.jet-external.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
//        domain("res.cloudinary.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.DOMAIN),
//        domain("*.cloudinary.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
//        domain("res.cloudinary.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.DOMAIN),
//        domain("*.wolt.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
//        domain("wolt.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.DOMAIN),
//        domain("*.uber.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
//        domain("uber.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.DOMAIN),
        domain("*.tiktokcdn.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.youtube.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.googlevideo.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.tiktokv.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.fbcdn.net", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.cdninstagram.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.instagram.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.twimg.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.x.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.twitter.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.redd.it", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.reddit.com", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
        domain("*.web.telegram.org", DomainRulesManager.Status.BLOCK, DomainRulesManager.DomainType.WILDCARD),
    )

    override fun getCustomDomainRules(): List<CustomDomain> {
        return domains.also {
            Log.i("DICTATOR", "current blocked domains: ${it.map { it.domain }}")
        }
    }

    private fun domain(domain: String, status: DomainRulesManager.Status, type: DomainRulesManager.DomainType): CustomDomain {
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

    override fun update(url: String) {
        currentUrl = url
    }
}