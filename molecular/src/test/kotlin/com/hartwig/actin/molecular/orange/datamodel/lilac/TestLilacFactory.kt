package com.hartwig.actin.molecular.orange.datamodel.lilac

import com.hartwig.hmftools.datamodel.hla.ImmutableLilacAllele
import org.apache.logging.log4j.util.Strings

object TestLilacFactory {

    fun builder(): ImmutableLilacAllele.Builder {
        return ImmutableLilacAllele.builder()
            .allele(Strings.EMPTY)
            .tumorCopyNumber(0.0)
            .somaticMissense(0.0)
            .somaticNonsenseOrFrameshift(0.0)
            .somaticSplice(0.0)
            .somaticInframeIndel(0.0)
            .somaticSynonymous(0.0)
            .refFragments(0)
            .tumorFragments(0)
            .rnaFragments(0)
    }
}
