package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.DriverFindingList
import com.hartwig.hmftools.datamodel.linx.LinxFusionType

class FusionExtractor(private val geneFilter: GeneFilter) {

    fun extract(fusions: DriverFindingList<com.hartwig.hmftools.datamodel.finding.Fusion>): List<Fusion> {
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
                fusedExonDown = fusion.fusedExonDown()
            )
        }.sorted()
    }

    internal fun determineDriverType(fusion: com.hartwig.hmftools.datamodel.finding.Fusion): FusionDriverType {
        return when (fusion.reportedType()) {
            LinxFusionType.PROMISCUOUS_3 -> {
                FusionDriverType.PROMISCUOUS_3
            }

            LinxFusionType.PROMISCUOUS_5 -> {
                FusionDriverType.PROMISCUOUS_5
            }

            LinxFusionType.PROMISCUOUS_BOTH -> {
                FusionDriverType.PROMISCUOUS_BOTH
            }

            LinxFusionType.IG_PROMISCUOUS -> {
                FusionDriverType.PROMISCUOUS_IG
            }

            LinxFusionType.KNOWN_PAIR -> {
                FusionDriverType.KNOWN_PAIR
            }

            LinxFusionType.IG_KNOWN_PAIR -> {
                FusionDriverType.KNOWN_PAIR_IG
            }

            LinxFusionType.EXON_DEL_DUP -> {
                FusionDriverType.KNOWN_PAIR_DEL_DUP
            }

            LinxFusionType.PROMISCUOUS_ENHANCER_TARGET -> {
                FusionDriverType.PROMISCUOUS_ENHANCER_TARGET
            }

            LinxFusionType.NONE -> {
                FusionDriverType.NONE
            }

            else -> {
                throw IllegalStateException("Cannot determine driver type for fusion of type: " + fusion.reportedType())
            }
        }
    }
}