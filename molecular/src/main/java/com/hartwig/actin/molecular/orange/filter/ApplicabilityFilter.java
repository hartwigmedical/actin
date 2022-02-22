package com.hartwig.actin.molecular.orange.filter;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.interpretation.OrangeUtil;

import org.jetbrains.annotations.NotNull;

public final class ApplicabilityFilter {

    static final Set<String> NON_APPLICABLE_GENES = Sets.newHashSet();
    static final Set<String> NON_APPLICABLE_EVENTS = Sets.newHashSet();
    static final Set<ApplicabilityFilterKey> NON_APPLICABLE_KEYS = Sets.newHashSet();

    static {
        NON_APPLICABLE_GENES.add("CDKN2A");

        NON_APPLICABLE_EVENTS.add("VEGFA amp");

        NON_APPLICABLE_KEYS.add(ImmutableApplicabilityFilterKey.builder()
                .gene("KRAS")
                .level(EvidenceLevel.A)
                .treatment("Trametinib")
                .build());

        NON_APPLICABLE_KEYS.add(ImmutableApplicabilityFilterKey.builder()
                .gene("KRAS")
                .level(EvidenceLevel.A)
                .treatment("Cobimetinib")
                .build());
    }

    private ApplicabilityFilter() {
    }

    public static boolean isPotentiallyApplicable(@NotNull TreatmentEvidence evidence) {
        String gene = evidence.gene();
        for (String nonApplicableGene : NON_APPLICABLE_GENES) {
            if (gene != null && gene.equals(nonApplicableGene)) {
                return false;
            }
        }

        String event = OrangeUtil.toEvent(evidence);
        for (String nonApplicableEvent : NON_APPLICABLE_EVENTS) {
            if (event.equals(nonApplicableEvent)) {
                return false;
            }
        }

        for (ApplicabilityFilterKey key : NON_APPLICABLE_KEYS) {
            if (gene != null && gene.equals(key.gene()) && evidence.level() == key.level() && evidence.treatment()
                    .equals(key.treatment())) {
                return false;
            }
        }

        return true;
    }
}
