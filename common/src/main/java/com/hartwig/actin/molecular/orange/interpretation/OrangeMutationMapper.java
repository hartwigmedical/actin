package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.util.GenomicEventFormatter;

import org.jetbrains.annotations.NotNull;

public final class OrangeMutationMapper {

    static final Map<String, String> HOTSPOT_MAPPINGS = Maps.newHashMap();
    static final Map<RangeKey, String> CODON_MAPPINGS = Maps.newHashMap();
    static final Map<RangeKey, String> EXON_MAPPINGS = Maps.newHashMap();

    private OrangeMutationMapper() {
    }

    static {
        HOTSPOT_MAPPINGS.put("R678E", "Arg678Glu");

        CODON_MAPPINGS.put(new RangeKey("BRAF", 600), "V600X");
        CODON_MAPPINGS.put(new RangeKey("CDK4", 24), "R24X");

        EXON_MAPPINGS.put(new RangeKey("EGFR", 20), "EXON 20 INSERTION");
        EXON_MAPPINGS.put(new RangeKey("ERBB2", 20), "EXON 20 INSERTION");

        EXON_MAPPINGS.put(new RangeKey("NRAS", 2), "exon 2-4");
        EXON_MAPPINGS.put(new RangeKey("NRAS", 3), "exon 2-4");
        EXON_MAPPINGS.put(new RangeKey("NRAS", 4), "exon 2-4");

        EXON_MAPPINGS.put(new RangeKey("KRAS", 2), "exon 2-4");
        EXON_MAPPINGS.put(new RangeKey("KRAS", 3), "exon 2-4");
        EXON_MAPPINGS.put(new RangeKey("KRAS", 4), "exon 2-4");
    }

    @NotNull
    public static String map(@NotNull TreatmentEvidence evidence) {
        switch (evidence.type()) {
            case HOTSPOT_MUTATION: {
                String event = GenomicEventFormatter.format(evidence.event());
                return HOTSPOT_MAPPINGS.getOrDefault(event, event);
            }
            case CODON_MUTATION: {
                RangeKey key = new RangeKey(evidence.gene(), evidence.rangeRank());
                if (!CODON_MAPPINGS.containsKey(key)) {
                    throw new IllegalStateException("Cannot convert codon evidence to ACTIN evidence: " + evidence);
                }
                return CODON_MAPPINGS.get(key);
            }
            case EXON_MUTATION: {
                RangeKey key = new RangeKey(evidence.gene(), evidence.rangeRank());
                if (!EXON_MAPPINGS.containsKey(key)) {
                    throw new IllegalStateException("Cannot convert exon evidence to ACTIN evidence: " + evidence);
                }
                return EXON_MAPPINGS.get(key);
            }
            default: {
                throw new IllegalStateException("Should never have to map mutation for this evidence: " + evidence);
            }
        }
    }

    private static class RangeKey {

        @NotNull
        private final String gene;
        private final int rank;

        public RangeKey(@NotNull final String gene, final int rank) {
            this.gene = gene;
            this.rank = rank;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final RangeKey rangeKey = (RangeKey) o;
            return rank == rangeKey.rank && gene.equals(rangeKey.gene);
        }

        @Override
        public int hashCode() {
            return Objects.hash(gene, rank);
        }
    }
}
