package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()

const val GENE = "gene"
const val HGVS_CODING = "c.1A>T"
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