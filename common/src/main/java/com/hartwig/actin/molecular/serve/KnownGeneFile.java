package com.hartwig.actin.molecular.serve;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class KnownGeneFile {

    private static final String DELIMITER = "\t";

    private KnownGeneFile() {
    }

    @NotNull
    public static List<KnownGene> read(@NotNull String knownGeneTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(knownGeneTsv).toPath());

        List<KnownGene> knownGenes = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            knownGenes.add(fromLine(line, fields));
        }
        return knownGenes;
    }

    @NotNull
    private static KnownGene fromLine(@NotNull String line, @NotNull Map<String, Integer> fields) {
        String[] values = line.split(DELIMITER);

        return ImmutableKnownGene.builder()
                .gene(values[fields.get("gene")])
                .geneRole(GeneRole.valueOf(values[fields.get("geneRole")]))
                .build();
    }
}
