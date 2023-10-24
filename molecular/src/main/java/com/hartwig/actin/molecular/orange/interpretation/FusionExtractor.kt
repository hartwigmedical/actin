package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.sort.driver.FusionComparator
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.hmftools.datamodel.linx.LinxRecord

internal class FusionExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {
    fun extract(linx: LinxRecord): MutableSet<Fusion?> {
        val fusions: MutableSet<Fusion?>? = Sets.newTreeSet(FusionComparator())
        for (fusion in linx.allSomaticFusions()) {
            val fusionEvent = DriverEventFactory.fusionEvent(fusion)
            if (geneFilter.include(fusion.geneStart()) || geneFilter.include(fusion.geneEnd())) {
                val knownFusion = evidenceDatabase.lookupKnownFusion(fusion)
                fusions.add(ImmutableFusion.builder()
                    .isReportable(fusion.reported())
                    .event(fusionEvent)
                    .driverLikelihood(determineDriverLikelihood(fusion))
                    .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(fusion)))
                    .geneStart(fusion.geneStart())
                    .geneTranscriptStart(fusion.geneTranscriptStart())
                    .fusedExonUp(fusion.fusedExonUp())
                    .geneEnd(fusion.geneEnd())
                    .geneTranscriptEnd(fusion.geneTranscriptEnd())
                    .fusedExonDown(fusion.fusedExonDown())
                    .proteinEffect(if (knownFusion != null) GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect()) else ProteinEffect.UNKNOWN)
                    .isAssociatedWithDrugResistance(knownFusion?.associatedWithDrugResistance())
                    .driverType(determineDriverType(fusion))
                    .build())
            } else check(!fusion.reported()) {
                ("Filtered a reported fusion through gene filtering: '" + fusionEvent + "'. Please make sure either '"
                        + fusion.geneStart() + "' or '" + fusion.geneEnd() + "' is configured as a known gene.")
            }
        }
        return fusions
    }

    companion object {
        @VisibleForTesting
        fun determineDriverType(fusion: LinxFusion): FusionDriverType {
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

        @VisibleForTesting
        fun determineDriverLikelihood(fusion: LinxFusion): DriverLikelihood? {
            return when (fusion.likelihood()) {
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
                    throw IllegalStateException("Cannot determine driver likelihood for fusion driver likelihood: " + fusion.likelihood())
                }
            }
        }
    }
}
