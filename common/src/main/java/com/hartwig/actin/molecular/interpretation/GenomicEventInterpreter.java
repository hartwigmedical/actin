package com.hartwig.actin.molecular.interpretation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.GenomicTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.util.GenomicEventFormatter;

import org.jetbrains.annotations.NotNull;

public final class GenomicEventInterpreter {

    private static final Set<String> NON_APPLICABLE_START_KEYWORDS = Sets.newHashSet();

    static {
        NON_APPLICABLE_START_KEYWORDS.add("CDKN2A");
    }

    private GenomicEventInterpreter() {
    }

    @NotNull
    public static Set<String> responsiveEvents(@NotNull MolecularRecord record) {
        Set<String> events = Sets.newTreeSet();
        for (GenomicTreatmentEvidence evidence : record.genomicTreatmentEvidences()) {
            if (isPotentiallyApplicable(evidence) && evidence.direction().isResponsive()) {
                events.add(GenomicEventFormatter.format(evidence.genomicEvent()));
            }
        }
        return events;
    }

    @NotNull
    public static Set<String> resistanceEvents(@NotNull MolecularRecord record) {
        Multimap<String, String> treatmentsPerEvent = ArrayListMultimap.create();
        for (GenomicTreatmentEvidence evidence : record.genomicTreatmentEvidences()) {
            if (isPotentiallyApplicable(evidence) && !evidence.direction().isResponsive()) {
                treatmentsPerEvent.put(GenomicEventFormatter.format(evidence.genomicEvent()), evidence.treatment());
            }
        }

        Set<String> events = Sets.newHashSet();
        for (Map.Entry<String, Collection<String>> entry : treatmentsPerEvent.asMap().entrySet()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String treatment : entry.getValue()) {
                joiner.add(treatment);
            }
            events.add(entry.getKey() + " (" + joiner + ")");
        }
        return events;
    }

    private static boolean isPotentiallyApplicable(@NotNull GenomicTreatmentEvidence evidence) {
        if ((evidence.level() == EvidenceLevel.C || evidence.level() == EvidenceLevel.D) && !evidence.onLabel()) {
            return false;
        }

        for (String nonApplicableStartKeyword : NON_APPLICABLE_START_KEYWORDS) {
            if (evidence.genomicEvent().startsWith(nonApplicableStartKeyword)) {
                return false;
            }
        }
        return true;
    }
}
