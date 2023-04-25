package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;

final class MolecularConstants {

    public static final Set<String> HRD_GENES = Sets.newHashSet();
    public static final Set<String> MSI_GENES = Sets.newHashSet();

    static {
        HRD_GENES.add("BRCA1");
        HRD_GENES.add("BRCA2");
        HRD_GENES.add("RAD51C");
        HRD_GENES.add("PALB2");

        MSI_GENES.add("MLH1");
        MSI_GENES.add("MSH2");
        MSI_GENES.add("MSH6");
        MSI_GENES.add("PMS2");
        MSI_GENES.add("EPCAM");
    }

    private MolecularConstants() {
    }
}
