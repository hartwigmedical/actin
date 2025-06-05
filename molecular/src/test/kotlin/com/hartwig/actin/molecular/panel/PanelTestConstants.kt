package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory

val ON_LABEL_MATCH = TestClinicalEvidenceFactory.withEvidence(
    TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        evidenceLevel = EvidenceLevel.A,
        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
        cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE
    )
)

val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()

const val ALT = "T"
const val REF = "G"
const val CHROMOSOME = "1"
const val POSITION = 1

val VARIANT = TestMolecularFactory.createMinimalVariant().copy(
    chromosome = CHROMOSOME,
    position = POSITION,
    ref = REF,
    alt = ALT,
    type = VariantType.SNV
)