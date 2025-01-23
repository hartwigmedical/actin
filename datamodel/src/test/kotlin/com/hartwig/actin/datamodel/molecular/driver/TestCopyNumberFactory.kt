package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber

object TestCopyNumberFactory {

    fun createMinimal(): CopyNumber {
        return CopyNumber(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            isAssociatedWithDrugResistance = null,
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            canonicalImpact = createTranscriptCopyNumberImpact(),
            otherImpacts = emptySet()
        )
    }
}
