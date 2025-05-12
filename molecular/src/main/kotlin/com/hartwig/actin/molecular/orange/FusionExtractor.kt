package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.hmftools.datamodel.linx.LinxRecord

class FusionExtractor(private val geneFilter: GeneFilter) {

    fun extract(linx: LinxRecord): List<Fusion> {
        return linx.allSomaticFusions().filter { fusion ->
            val included = geneFilter.include(fusion.geneStart()) || geneFilter.include(fusion.geneEnd())
            if (!included && fusion.reported()) {
                throw IllegalStateException(
                    "Filtered a reported fusion through gene filtering: '${DriverEventFactory.fusionEvent(fusion)}'."
                            + " Please make sure either '${fusion.geneStart()}' or '${fusion.geneEnd()}' is configured as a known gene."
                )
            }
            included
        }.map { fusion ->
            Fusion(
                isReportable = fusion.reported(),
                event = DriverEventFactory.fusionEvent(fusion),
                driverLikelihood = determineDriverLikelihood(fusion),
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

    internal fun determineDriverType(fusion: LinxFusion): FusionDriverType {
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

    internal fun determineDriverLikelihood(fusion: LinxFusion): DriverLikelihood? {
        return when (fusion.driverLikelihood()) {
            FusionLikelihoodType.HIGH -> {
                DriverLikelihood.HIGH
            }

            FusionLikelihoodType.LOW -> {
                DriverLikelihood.LOW
            }

            FusionLikelihoodType.NA -> {
                null
            }

            else -> {
                throw IllegalStateException(
                    "Cannot determine driver likelihood for fusion driver likelihood: " +
                            fusion.driverLikelihood()
                )
            }
        }
    }
}