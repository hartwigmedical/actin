package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.hmf.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.hmf.driver.Disruption
import com.hartwig.actin.molecular.datamodel.hmf.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.hmf.driver.RegionType
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxDriver
import com.hartwig.hmftools.datamodel.linx.LinxDriverType
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxSvAnnotation

internal class DisruptionExtractor(private val geneFilter: GeneFilter) {

    fun extractDisruptions(linx: LinxRecord, lostGenes: Set<String>, drivers: List<LinxDriver>): MutableSet<Disruption> {
        return linx.allSomaticBreakends().filter { breakend ->
            val geneIncluded = geneFilter.include(breakend.gene())
            if (!geneIncluded && breakend.reported()) {
                throw IllegalStateException(
                    "Filtered a reported breakend through gene filtering: '${DriverEventFactory.disruptionEvent(breakend)}'."
                            + " Please make sure '${breakend.gene()}' is configured as a known gene."
                )
            }
            geneIncluded && include(breakend, lostGenes)
        }
            .map { breakend ->
                Disruption(
                    gene = breakend.gene(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    isReportable = breakend.reported(),
                    event = DriverEventFactory.disruptionEvent(breakend),
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = ActionableEvidenceFactory.createNoEvidence(),
                    type = determineDisruptionType(breakend.type()),
                    junctionCopyNumber = ExtractionUtil.keep3Digits(breakend.junctionCopyNumber()),
                    undisruptedCopyNumber = ExtractionUtil.keep3Digits(correctUndisruptedCopyNumber(breakend, drivers)),
                    regionType = determineRegionType(breakend.regionType()),
                    codingContext = determineCodingContext(breakend.codingType()),
                    clusterGroup = lookupClusterId(breakend, linx.allSomaticStructuralVariants())
                )
            }
            .toSortedSet(DisruptionComparator())
    }

    companion object {
        private fun include(breakend: LinxBreakend, lostGenes: Set<String>): Boolean {
            return breakend.type() != LinxBreakendType.DEL || !lostGenes.contains(breakend.gene())
        }

        private fun lookupClusterId(breakend: LinxBreakend, structuralVariants: List<LinxSvAnnotation>): Int {
            return structuralVariants.find { it.svId() == breakend.svId() }?.clusterId()
                ?: throw IllegalStateException("Could not find structural variant with ID: " + breakend.svId())
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

                else -> {
                    throw IllegalStateException("Cannot determine disruption type for linx disruption type: $type")
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

        fun correctUndisruptedCopyNumber(breakend: LinxBreakend, drivers: List<LinxDriver>): Double {
            return if (breakend.type() == LinxBreakendType.DUP
                && drivers.any { driver -> driver.gene() == breakend.gene() && driver.type() == LinxDriverType.HOM_DUP_DISRUPTION }
            ) {
                (breakend.undisruptedCopyNumber() - breakend.junctionCopyNumber()).coerceAtLeast(0.0)
            } else {
                breakend.undisruptedCopyNumber()
            }
        }
    }
}
