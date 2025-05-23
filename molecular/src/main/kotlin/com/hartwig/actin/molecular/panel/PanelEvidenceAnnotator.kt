package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase

typealias PanelRecordWithEvidence = PanelRecord

class PanelEvidenceAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
) : MolecularAnnotator<PanelRecordWithDriverAttributes, PanelRecordWithEvidence> {

    override fun annotate(input: PanelRecordWithDriverAttributes): PanelRecordWithEvidence {
        return input.copy(
            drivers = input.drivers.copy(
                variants = input.drivers.variants.map { annotateVariantWithClinicalEvidence(it) },
                copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithClinicalEvidence(it) },
                fusions = input.drivers.fusions.map { annotateFusionWithClinicalEvidence(it) },
            ),
            characteristics = annotateCharacteristicsWithClinicalEvidence(input.characteristics)
        )
    }

    private fun annotateCharacteristicsWithClinicalEvidence(characteristics: MolecularCharacteristics): MolecularCharacteristics {
        val microsatelliteStability = characteristics.microsatelliteStability?.let {
            it.copy(evidence = evidenceDatabase.evidenceForMicrosatelliteStatus(it.isUnstable))
        }

        val tumorMutationalBurden = characteristics.tumorMutationalBurden?.let {
            it.copy(evidence = evidenceDatabase.evidenceForTumorMutationalBurdenStatus(it.isHigh))
        }

        return characteristics.copy(
            microsatelliteStability = microsatelliteStability,
            tumorMutationalBurden = tumorMutationalBurden
        )
    }

    private fun annotateVariantWithClinicalEvidence(variant: Variant): Variant {
        val evidence = evidenceDatabase.evidenceForVariant(variant)
        return variant.copy(evidence = evidence)
    }

    private fun annotatedCopyNumberWithClinicalEvidence(copyNumber: CopyNumber): CopyNumber {
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumber)
        return copyNumber.copy(evidence = evidence)
    }

    private fun annotateFusionWithClinicalEvidence(fusion: Fusion): Fusion {
        val evidence = evidenceDatabase.evidenceForFusion(fusion)
        return fusion.copy(evidence = evidence)
    }
}