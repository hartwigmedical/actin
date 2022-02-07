package com.hartwig.actin.serve.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ServeRecordTsvTest {

    private static final String EXAMPLE_TSV = Resources.getResource("output.example.tsv").getPath();

    @Test
    public void canReadExampleTsv() throws IOException {
        List<ServeRecord> records = ServeRecordTsv.read(EXAMPLE_TSV);

        assertEquals(3, records.size());
        ServeRecord record1 = findByGene(records, "X");
        assertEquals("TRIAL 1", record1.trial());
        assertEquals(EligibilityRule.AMPLIFICATION_OF_GENE_X, record1.rule());
        assertNull(record1.mutation());
        assertTrue(record1.isUsedAsInclusion());

        ServeRecord record2 = findByGene(records, "Y");
        assertEquals("TRIAL 1", record2.trial());
        assertEquals(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, record2.rule());
        assertEquals("some mutation", record2.mutation());
        assertFalse(record2.isUsedAsInclusion());

        ServeRecord record3 = findByMutation(records, "TMB >= 8");
        assertEquals("TRIAL 2", record3.trial());
        assertEquals(EligibilityRule.TMB_OF_AT_LEAST_X, record3.rule());
        assertNull(record3.gene());
        assertTrue(record3.isUsedAsInclusion());
    }

    @NotNull
    private static ServeRecord findByGene(@NotNull List<ServeRecord> records, @NotNull String gene) {
        for (ServeRecord record : records) {
            if (gene.equals(record.gene())) {
                return record;
            }
        }

        throw new IllegalStateException("No SERVE record found with gene: " + gene);
    }

    @NotNull
    private static ServeRecord findByMutation(@NotNull List<ServeRecord> records, @NotNull String mutation) {
        for (ServeRecord record : records) {
            if (mutation.equals(record.mutation())) {
                return record;
            }
        }

        throw new IllegalStateException("No SERVE record found with mutation: " + mutation);
    }
}