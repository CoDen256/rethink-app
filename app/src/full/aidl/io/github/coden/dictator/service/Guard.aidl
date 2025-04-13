package io.github.coden.dictator.service;
import io.github.coden.dictator.service.DNSRuling;
import io.github.coden.dictator.service.DomainRuling;

interface Guard {
    List<DNSRuling> dnsRulings();
    List<DomainRuling> domainRulings();
}