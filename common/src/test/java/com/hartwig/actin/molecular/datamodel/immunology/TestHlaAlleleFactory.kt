package com.hartwig.actin.molecular.datamodel.immunology

import org.apache.logging.log4j.util.Strings

object TestHlaAlleleFactory {
    @JvmStatic
    fun builder(): ImmutableHlaAllele.Builder {
        return ImmutableHlaAllele.builder().name(Strings.EMPTY).tumorCopyNumber(0.0).hasSomaticMutations(false)
    }
}
