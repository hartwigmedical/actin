package com.hartwig.actin.molecular.filter;

import org.jetbrains.annotations.NotNull;

class AlwaysValidFilter implements GeneFilter {

    AlwaysValidFilter() {
    }

    @Override
    public boolean include(@NotNull final String gene) {
        return true;
    }
}
