package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DisruptionType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.Breakend

class DisruptionExtractor(private val geneFilter: GeneFilter) {

    fun extractDisruptions(
        disruptions: List<com.hartwig.hmftools.finding.datamodel.Disruption>,
        lostGenes: Set<String>
    ): List<Disruption> {
        return disruptions
            .filter { disruption ->
                MappingUtil.includedInGeneFilter(disruption, geneFilter) { include(it, lostGenes) }
            }.map { disruption ->
                val breakend = disruption.breakendStart() ?: disruption.breakendEnd()!!
                Disruption(
                    gene = disruption.gene(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    isReportable = disruption.isReported,
                    event = DriverEventFactory.event(disruption),
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = ExtractionUtil.noEvidence(),
                    type = determineDisruptionType(disruption.breakendType()),
                    junctionCopyNumber = ExtractionUtil.keep3Digits(breakend.junctionCopyNumber()),
                    undisruptedCopyNumber = ExtractionUtil.keep3Digits(correctUndisruptedCopyNumber(disruption, breakend)),
                    regionType = determineRegionType(breakend.regionType()),
                    codingContext = determineCodingContext(breakend.codingType()),
                    clusterGroup = lookupClusterId(disruption)
                )
            }.sorted()
    }

    private fun include(disruption: com.hartwig.hmftools.finding.datamodel.Disruption, lostGenes: Set<String>): Boolean {
        return disruption.breakendType() != Breakend.Type.DEL || !lostGenes.contains(disruption.gene())
    }

    private fun lookupClusterId(disruption: com.hartwig.hmftools.finding.datamodel.Disruption): Int {
        // TODO: Can't report id here
        return disruption.clusterId()
            ?: throw IllegalStateException("Could not find structural variant")
    }

    internal fun determineDisruptionType(type: Breakend.Type): DisruptionType {
        return when (type) {
            Breakend.Type.BND -> {
                DisruptionType.BND
            }

            Breakend.Type.DEL -> {
                DisruptionType.DEL
            }

            Breakend.Type.DUP -> {
                DisruptionType.DUP
            }

            Breakend.Type.INF -> {
                DisruptionType.INF
            }

            Breakend.Type.INS -> {
                DisruptionType.INS
            }

            Breakend.Type.INV -> {
                DisruptionType.INV
            }

            Breakend.Type.SGL -> {
                DisruptionType.SGL
            }
        }
    }

    internal fun determineRegionType(regionType: Breakend.TranscriptRegionType): RegionType {
        return when (regionType) {
            Breakend.TranscriptRegionType.UPSTREAM -> {
                RegionType.UPSTREAM
            }

            Breakend.TranscriptRegionType.EXONIC -> {
                RegionType.EXONIC
            }

            Breakend.TranscriptRegionType.INTRONIC -> {
                RegionType.INTRONIC
            }

            Breakend.TranscriptRegionType.IG -> {
                RegionType.IG
            }

            Breakend.TranscriptRegionType.DOWNSTREAM -> {
                RegionType.DOWNSTREAM
            }

            else -> {
                throw IllegalStateException("Cannot determine region type for linx region type: $regionType")
            }
        }
    }

    internal fun determineCodingContext(codingType: Breakend.TranscriptCodingType): CodingContext {
        return when (codingType) {
            Breakend.TranscriptCodingType.CODING -> {
                CodingContext.CODING
            }

            Breakend.TranscriptCodingType.UTR_5P -> {
                CodingContext.UTR_5P
            }

            Breakend.TranscriptCodingType.UTR_3P -> {
                CodingContext.UTR_3P
            }

            Breakend.TranscriptCodingType.NON_CODING -> {
                CodingContext.NON_CODING
            }

            Breakend.TranscriptCodingType.ENHANCER -> {
                CodingContext.ENHANCER
            }

            else -> {
                throw IllegalStateException("Cannot determine coding context for linx coding type: $codingType")
            }
        }
    }

    private fun correctUndisruptedCopyNumber(disruption: com.hartwig.hmftools.finding.datamodel.Disruption, breakend: Breakend): Double {
        return if (breakend.type() == Breakend.Type.DUP
            && disruption.gene == breakend.gene() && disruption.type == com.hartwig.hmftools.finding.datamodel.Disruption.Type.SOMATIC_HOM_DUP_DISRUPTION
        ) {
            (breakend.undisruptedCopyNumber() - breakend.junctionCopyNumber()).coerceAtLeast(0.0)
        } else {
            breakend.undisruptedCopyNumber()
        }
    }
}