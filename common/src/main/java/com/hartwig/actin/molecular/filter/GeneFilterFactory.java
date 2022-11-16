package com.hartwig.actin.molecular.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class GeneFilterFactory {

    private GeneFilterFactory() {
    }

    @NotNull
    public static GeneFilter createAlwaysValid() {
        return new AlwaysValidFilter();
    }

    @NotNull
    public static GeneFilter createFromTsv(@NotNull String geneFilterTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(geneFilterTsv).toPath());

        Set<String> genes = Sets.newHashSet();
        genes.addAll(lines.subList(1, lines.size()));

        return new SpecificGenesFilter(genes);
    }
}
