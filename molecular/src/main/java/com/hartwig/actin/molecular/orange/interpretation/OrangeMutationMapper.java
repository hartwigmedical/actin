package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.util.GenomicEventFormatter;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class OrangeMutationMapper implements MutationMapper {

    @NotNull
    private final List<ServeRecord> mutations;

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

    private OrangeMutationMapper(@NotNull final List<ServeRecord> mutations) {
        this.mutations = mutations;
    }

    @Override
    @NotNull
    public String map(@NotNull TreatmentEvidence evidence) {
        switch (evidence.type()) {
            case HOTSPOT_MUTATION: {
                return mapForHotspotMutation(mutations, evidence.gene(), evidence.event());
            }
            case CODON_MUTATION: {
                return mapForCodonMutation(mutations, evidence.gene(), evidence.rangeRank());
            }
            case EXON_MUTATION: {
                return mapForExonMutation(mutations, evidence.gene(), evidence.rangeRank());
            }
            default: {
                throw new IllegalArgumentException("Should never have to map mutation for this evidence: " + evidence);
            }
        }
    }

    @NotNull
    private static String mapForHotspotMutation(@NotNull List<ServeRecord> records, @NotNull String gene, @NotNull String event) {
        String formattedEvent = GenomicEventFormatter.format(event);
        for (ServeRecord record : records) {
            String formattedMutation = GenomicEventFormatter.format(record.mutation());
            if (gene.equals(record.gene()) && formattedEvent.equals(formattedMutation)) {
                return record.mutation();
            }
        }

        throw new IllegalStateException("Could not find SERVE record for hotspot mapping of mutation: " + event);
    }

    @NotNull
    private static String mapForCodonMutation(@NotNull List<ServeRecord> records, @NotNull String gene, int codon) {
        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && record.mutation().endsWith(codon + "X")) {
                return record.mutation();
            }
        }

        throw new IllegalStateException("Could not find SERVE record for codon mapping of mutation: " + gene + " (" + codon + ")");
    }

    @NotNull
    private static String mapForExonMutation(@NotNull List<ServeRecord> records, @NotNull String gene, int exon) {
        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && record.mutation().toLowerCase().startsWith("exon")) {
                // Assume format "exon x-y <something>"
                String exonRange = record.mutation().split(" ")[1];
                if (exonRange.contains("-")) {
                    String[] range = exonRange.split("-");
                    if (exon >= Integer.parseInt(range[0]) && exon <= Integer.parseInt(range[1])) {
                        return record.mutation();
                    }
                } else if (Integer.parseInt(exonRange) == exon) {
                    return record.mutation();
                }
            }
        }

        throw new IllegalStateException("Could not find SERVE record for exon mapping of mutation: " + gene + " (" + exon + ")");
    }
}
