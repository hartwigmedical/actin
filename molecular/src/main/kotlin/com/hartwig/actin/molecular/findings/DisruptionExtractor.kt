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
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxDriver
import com.hartwig.hmftools.datamodel.linx.LinxDriverType
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
                    event = disruption.event(),
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = ExtractionUtil.noEvidence(),
                    type = determineDisruptionType(disruption.breakendType()),
                    junctionCopyNumber = ExtractionUtil.keep3Digits(breakend.junctionCopyNumber()),
                    undisruptedCopyNumber = ExtractionUtil.keep3Digits(correctUndisruptedCopyNumber(breakend, listOf())),
                    regionType = determineRegionType(breakend.regionType()),
                    codingContext = determineCodingContext(breakend.codingType()),
                    clusterGroup = lookupClusterId(disruption)
                )
            }.sorted()
    }

    private fun include(disruption: com.hartwig.hmftools.finding.datamodel.Disruption, lostGenes: Set<String>): Boolean {
        return disruption.breakendType() != LinxBreakendType.DEL || !lostGenes.contains(disruption.gene())
    }

    private fun lookupClusterId(disruption: com.hartwig.hmftools.finding.datamodel.Disruption): Int {
        // TODO: Can't report id here
        return disruption.clusterId()
            ?: throw IllegalStateException("Could not find structural variant")
    }

    internal fun determineDisruptionType(type: LinxBreakendType): DisruptionType {
        return when (type) {
            LinxBreakendType.BND -> {
                DisruptionType.BND
            }

            LinxBreakendType.DEL -> {
                DisruptionType.DEL
            }

            LinxBreakendType.DUP -> {
                DisruptionType.DUP
            }

            LinxBreakendType.INF -> {
                DisruptionType.INF
            }

            LinxBreakendType.INS -> {
                DisruptionType.INS
            }

            LinxBreakendType.INV -> {
                DisruptionType.INV
            }

            LinxBreakendType.SGL -> {
                DisruptionType.SGL
            }
        }
    }

    internal fun determineRegionType(regionType: TranscriptRegionType): RegionType {
        return when (regionType) {
            TranscriptRegionType.UPSTREAM -> {
                RegionType.UPSTREAM
            }

            TranscriptRegionType.EXONIC -> {
                RegionType.EXONIC
            }

            TranscriptRegionType.INTRONIC -> {
                RegionType.INTRONIC
            }

            TranscriptRegionType.IG -> {
                RegionType.IG
            }

            TranscriptRegionType.DOWNSTREAM -> {
                RegionType.DOWNSTREAM
            }

            else -> {
                throw IllegalStateException("Cannot determine region type for linx region type: $regionType")
            }
        }
    }

    internal fun determineCodingContext(codingType: TranscriptCodingType): CodingContext {
        return when (codingType) {
            TranscriptCodingType.CODING -> {
                CodingContext.CODING
            }

            TranscriptCodingType.UTR_5P -> {
                CodingContext.UTR_5P
            }

            TranscriptCodingType.UTR_3P -> {
                CodingContext.UTR_3P
            }

            TranscriptCodingType.NON_CODING -> {
                CodingContext.NON_CODING
            }

            TranscriptCodingType.ENHANCER -> {
                CodingContext.ENHANCER
            }

            else -> {
                throw IllegalStateException("Cannot determine coding context for linx coding type: $codingType")
            }
        }
    }

    private fun correctUndisruptedCopyNumber(breakend: Breakend, drivers: List<LinxDriver>): Double {
        return if (breakend.type() == LinxBreakendType.DUP
            && drivers.any { driver -> driver.gene() == breakend.gene() && driver.type() == LinxDriverType.HOM_DUP_DISRUPTION }
        ) {
            (breakend.undisruptedCopyNumber() - breakend.junctionCopyNumber()).coerceAtLeast(0.0)
        } else {
            breakend.undisruptedCopyNumber()
        }
    }
}