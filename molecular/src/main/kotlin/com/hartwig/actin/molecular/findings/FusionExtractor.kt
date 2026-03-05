package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.DriverFindingList

fun domainsAsList(domains: String): List<String> {
    return domains.split(";").filter { it.isNotEmpty() }
}

class FusionExtractor(private val geneFilter: GeneFilter) {

    fun extract(fusions: DriverFindingList<com.hartwig.hmftools.finding.datamodel.Fusion>): List<Fusion> {
        return fusions.somaticOnly().findings.filter {
            MappingUtil.includedInGeneFilter(it, geneFilter)
        }.map { fusion ->
            Fusion(
                isReportable = fusion.isReported,
                event = fusion.event(),
                driverLikelihood = MappingUtil.determineDriverLikelihood(fusion),
                evidence = ExtractionUtil.noEvidence(),
                geneStart = fusion.geneStart(),
                geneEnd = fusion.geneEnd(),
                driverType = determineDriverType(fusion),
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                geneTranscriptStart = fusion.geneTranscriptStart(),
                geneTranscriptEnd = fusion.geneTranscriptEnd(),
                fusedExonUp = fusion.fusedExonUp(),
                fusedExonDown = fusion.fusedExonDown(),
                domainsKept = domainsAsList(fusion.domainsKept()),
                domainsLost = domainsAsList(fusion.domainsLost()),
            )
        }.sorted()
    }

    internal fun determineDriverType(fusion: com.hartwig.hmftools.finding.datamodel.Fusion): FusionDriverType {
        return when (fusion.reportedType()) {
            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.PROMISCUOUS_3 -> {
                FusionDriverType.PROMISCUOUS_3
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.PROMISCUOUS_5 -> {
                FusionDriverType.PROMISCUOUS_5
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.PROMISCUOUS_BOTH -> {
                FusionDriverType.PROMISCUOUS_BOTH
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.IG_PROMISCUOUS -> {
                FusionDriverType.PROMISCUOUS_IG
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.KNOWN_PAIR -> {
                FusionDriverType.KNOWN_PAIR
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.IG_KNOWN_PAIR -> {
                FusionDriverType.KNOWN_PAIR_IG
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.EXON_DEL_DUP -> {
                FusionDriverType.KNOWN_PAIR_DEL_DUP
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.PROMISCUOUS_ENHANCER_TARGET -> {
                FusionDriverType.PROMISCUOUS_ENHANCER_TARGET
            }

            com.hartwig.hmftools.finding.datamodel.Fusion.FusionType.NONE -> {
                FusionDriverType.NONE
            }

            else -> {
                throw IllegalStateException("Cannot determine driver type for fusion of type: " + fusion.reportedType())
            }
        }
    }
}