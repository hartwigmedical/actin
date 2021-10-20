package com.hartwig.actin.common;

import com.google.common.base.Strings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DatamodelPrinter {

    private static final Logger LOGGER = LogManager.getLogger(DatamodelPrinter.class);
    private static final int DEFAULT_INDENTATION = 1;

    private final int indentation;

    @NotNull
    public static DatamodelPrinter withDefaultIndentation() {
        return new DatamodelPrinter(DEFAULT_INDENTATION);
    }

    public DatamodelPrinter(final int indentation) {
        this.indentation = indentation;
    }

    public void print(@NotNull String line) {
        LOGGER.info("{}{}", Strings.repeat(" ", indentation), line);
    }
}
