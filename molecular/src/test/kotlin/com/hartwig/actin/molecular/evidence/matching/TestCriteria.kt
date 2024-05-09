package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType

val VARIANT_CRITERIA = VariantMatchCriteria(
    isReportable = true,
    ref = "A",
    alt = "T",
    gene = "gene 1",
    chromosome = "12",
    position = 13,
    codingEffect = CodingEffect.MISSENSE,
    type = VariantType.SNV
)