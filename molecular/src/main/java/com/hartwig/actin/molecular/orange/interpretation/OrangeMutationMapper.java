package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.util.EventFormatter;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

class OrangeMutationMapper implements MutationMapper {

    @NotNull
    private final List<ServeRecord> mutationRecords;

    @NotNull
    public static OrangeMutationMapper fromServeRecords(@NotNull List<ServeRecord> records) {
        List<ServeRecord> mutations = Lists.newArrayList();
        for (ServeRecord record : records) {
            if (record.rule() == EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y) {
                mutations.add(record);
            }
        }
        return new OrangeMutationMapper(mutations);
    }

    private OrangeMutationMapper(@NotNull final List<ServeRecord> mutationRecords) {
        this.mutationRecords = mutationRecords;
    }

    @Override
    @NotNull
    public Set<String> map(@NotNull ProtectEvidence evidence) {
        if (evidence.sources().size() != 1) {
            throw new IllegalStateException("Should never map mutations for evidence without single source: " + evidence);
        }

        ProtectSource source = evidence.sources().iterator().next();
        switch (source.type()) {
            case HOTSPOT_MUTATION: {
                return mapForHotspotMutation(mutationRecords, evidence.gene(), evidence.event());
            }
            case CODON_MUTATION: {
                return mapForCodonMutation(mutationRecords, evidence.gene(), source.rangeRank());
            }
            case EXON_MUTATION: {
                return mapForExonMutation(mutationRecords, evidence.gene(), source.rangeRank());
            }
            default: {
                throw new IllegalArgumentException("Should never have to map mutation for this evidence: " + evidence);
            }
        }
    }

    @NotNull
    private static Set<String> mapForHotspotMutation(@NotNull List<ServeRecord> records, @NotNull String gene, @NotNull String event) {
        Set<String> results = Sets.newHashSet();

        String formattedEvent = EventFormatter.format(event);
        for (ServeRecord record : records) {
            String formattedMutation = EventFormatter.format(record.mutation());
            if (gene.equals(record.gene()) && formattedEvent.equals(formattedMutation)) {
                results.add(record.mutation());
            }
        }

        if (results.isEmpty()) {
            throw new IllegalStateException("Could not find SERVE-bridge record for hotspot mapping of mutation: " + event);
        }

        return results;
    }

    @NotNull
    private static Set<String> mapForCodonMutation(@NotNull List<ServeRecord> records, @NotNull String gene, int codon) {
        Set<String> results = Sets.newHashSet();

        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && record.mutation().endsWith(codon + "X")) {
                results.add(record.mutation());
            }
        }

        if (results.isEmpty()) {
            throw new IllegalStateException(
                    "Could not find SERVE-bridge record for codon mapping of mutation: " + gene + " (" + codon + ")");
        }

        return results;
    }

    @NotNull
    private static Set<String> mapForExonMutation(@NotNull List<ServeRecord> records, @NotNull String gene, int exon) {
        Set<String> results = Sets.newHashSet();

        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && record.mutation().toLowerCase().startsWith("exon")) {
                // Assume format "exon x-y <something>"
                String exonRange = record.mutation().split(" ")[1];
                if (exonRange.contains("-")) {
                    String[] range = exonRange.split("-");
                    if (exon >= Integer.parseInt(range[0]) && exon <= Integer.parseInt(range[1])) {
                        results.add(record.mutation());
                    }
                } else if (Integer.parseInt(exonRange) == exon) {
                    results.add(record.mutation());
                }
            }
        }

        if (results.isEmpty()) {
            throw new IllegalStateException("Could not find SERVE-bridge record for exon mapping of mutation: " + gene + " (" + exon + ")");
        }

        return results;
    }
}
