package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestDisruptionFactory {
    @JvmStatic
    fun builder(): ImmutableDisruption.Builder {
        return ImmutableDisruption.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .gene(Strings.EMPTY)
            .geneRole(GeneRole.UNKNOWN)
            .proteinEffect(ProteinEffect.UNKNOWN)
            .type(DisruptionType.BND)
            .junctionCopyNumber(0)
            .undisruptedCopyNumber(0)
            .regionType(RegionType.INTRONIC)
            .codingContext(CodingContext.NON_CODING)
            .clusterGroup(0)
    }
}
