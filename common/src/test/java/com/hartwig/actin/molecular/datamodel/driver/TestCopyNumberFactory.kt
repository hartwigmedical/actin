package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestCopyNumberFactory {
    @JvmStatic
    fun builder(): ImmutableCopyNumber.Builder {
        return ImmutableCopyNumber.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .gene(Strings.EMPTY)
            .geneRole(GeneRole.UNKNOWN)
            .proteinEffect(ProteinEffect.UNKNOWN)
            .type(CopyNumberType.NONE)
            .minCopies(0)
            .maxCopies(0)
    }
}
