package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType

object GeneConstants {
    val HR_GENES = setOf("BRCA1", "BRCA2", "RAD51C", "PALB2")
    val MMR_GENES = setOf("EPCAM", "MLH1", "MSH2", "MSH6", "PMS2")
    val DELETION = setOf(CopyNumberType.PARTIAL_DEL, CopyNumberType.FULL_DEL)
}