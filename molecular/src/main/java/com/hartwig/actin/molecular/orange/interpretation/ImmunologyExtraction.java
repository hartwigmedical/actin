package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;

import org.jetbrains.annotations.NotNull;

final class ImmunologyExtraction {

    static final String LILAC_QC_PASS = "PASS";

    private ImmunologyExtraction() {
    }

    @NotNull
    public static MolecularImmunology extract(@NotNull OrangeRecord record) {
        LilacRecord lilac = record.lilac();
        return ImmutableMolecularImmunology.builder().isReliable(isQCPass(lilac)).hlaAlleles(toHlaAlleles(lilac.alleles())).build();
    }

    private static boolean isQCPass(@NotNull LilacRecord lilac) {
        return lilac.qc().equals(LILAC_QC_PASS);
    }

    @NotNull
    private static Set<HlaAllele> toHlaAlleles(@NotNull Set<LilacHlaAllele> alleles) {
        Set<HlaAllele> hlaAlleles = Sets.newHashSet();
        for (LilacHlaAllele allele : alleles) {
            boolean hasSomaticVariants =
                    allele.somaticMissense() > 0 || allele.somaticNonsenseOrFrameshift() > 0 || allele.somaticSplice() > 0
                            || allele.somaticInframeIndel() > 0;

            hlaAlleles.add(ImmutableHlaAllele.builder()
                    .name(allele.name())
                    .tumorCopyNumber(allele.tumorCopyNumber())
                    .hasSomaticMutations(hasSomaticVariants)
                    .build());
        }
        return hlaAlleles;
    }
}
