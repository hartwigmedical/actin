package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.evidence.actionability.MatchesForActionable
import com.hartwig.actin.molecular.util.ExtractionUtil

class EvidenceAnnotator(
    private val clinicalEvidenceFactory: ClinicalEvidenceFactory,
    val actionabilityMatcher: ActionabilityMatcher,
) : MolecularAnnotator<MolecularTest, MolecularTest> {
    override fun annotate(input: MolecularTest): MolecularTest {

        val matchesForActionable = actionabilityMatcher.match(input)

        when (input) {
            is MolecularRecord -> {
                return input.copy(
                    drivers = annotateDriversWithEvidence(input.drivers, matchesForActionable),
                    characteristics = annotateCharacteristicsWithEvidence(input.characteristics, matchesForActionable)
                )
            }

            is PanelRecord -> {
                return input.copy(
                    drivers = annotateDriversWithEvidence(input.drivers, matchesForActionable),
                    characteristics = annotateCharacteristicsWithEvidence(input.characteristics, matchesForActionable)
                )
            }

            else -> {
                throw IllegalArgumentException("Unsupported MolecularTest type: ${input::class.java}")
            }
        }
    }

    private fun annotateDriversWithEvidence(
        drivers: Drivers,
        matchesForActionable: MatchesForActionable
    ): Drivers {
        return drivers.copy(
            variants = drivers.variants.map { variant -> variant.copy(evidence = matchEvidence(variant, matchesForActionable)) },
            copyNumbers = drivers.copyNumbers.map { copyNumber ->
                copyNumber.copy(
                    evidence = matchEvidence(
                        copyNumber,
                        matchesForActionable
                    )
                )
            },
            homozygousDisruptions = drivers.homozygousDisruptions.map { homozygousDisruption ->
                homozygousDisruption.copy(evidence = matchEvidence(homozygousDisruption, matchesForActionable))
            },
            disruptions = drivers.disruptions.map { disruption ->
                disruption.copy(
                    evidence = matchEvidence(
                        disruption,
                        matchesForActionable
                    )
                )
            },
            fusions = drivers.fusions.map { fusion -> fusion.copy(evidence = matchEvidence(fusion, matchesForActionable)) },
            viruses = drivers.viruses.map { virus -> virus.copy(evidence = matchEvidence(virus, matchesForActionable)) }
        )
    }

    private fun annotateCharacteristicsWithEvidence(
        characteristics: MolecularCharacteristics,
        matchesForActionable: MatchesForActionable
    ): MolecularCharacteristics {
        return characteristics.copy(
            microsatelliteStability = characteristics.microsatelliteStability?.let { msi ->
                msi.copy(evidence = matchEvidence(msi, matchesForActionable))
            },
            homologousRecombination = characteristics.homologousRecombination?.let { hr ->
                hr.copy(evidence = matchEvidence(hr, matchesForActionable))
            },
            tumorMutationalBurden = characteristics.tumorMutationalBurden?.let { tmb ->
                tmb.copy(evidence = matchEvidence(tmb, matchesForActionable))
            },
            tumorMutationalLoad = characteristics.tumorMutationalLoad?.let { tml ->
                tml.copy(
                    evidence = matchEvidence(
                        tml,
                        matchesForActionable
                    )
                )
            }
        )
    }

    private fun matchEvidence(variant: Actionable, matchesForActionable: MatchesForActionable): ClinicalEvidence {
        val actionabilityMatch = matchesForActionable[variant]
        val clinicalEvidence = actionabilityMatch?.let { clinicalEvidenceFactory.create(it) }
        return clinicalEvidence ?: ExtractionUtil.noEvidence()
    }
}