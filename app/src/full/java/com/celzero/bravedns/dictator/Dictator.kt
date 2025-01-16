package com.celzero.bravedns.dictator

import com.celzero.bravedns.database.CustomDomain
import com.celzero.bravedns.database.RethinkDnsEndpoint

interface Dictator {
    fun getDnsEndpoint(): RethinkDnsEndpoint?
    fun getCustomDomainRules(): List<CustomDomain>

    fun update(url: String)
}