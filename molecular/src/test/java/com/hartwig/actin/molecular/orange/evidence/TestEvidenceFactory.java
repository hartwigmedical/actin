package com.hartwig.actin.molecular.orange.evidence;


import com.hartwig.serve.datamodel.MutationType;
import com.hartwig.serve.datamodel.gene.GeneAnnotation;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.hotspot.VariantHotspot;
import com.hartwig.serve.datamodel.range.RangeAnnotation;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestEvidenceFactory {

    private TestEvidenceFactory() {
    }

    @NotNull
    public static VariantHotspot createEmptyHotspot() {
        return new VariantHotspot() {
            @NotNull
            @Override
            public String gene() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public String ref() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public String alt() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public String chromosome() {
                return Strings.EMPTY;
            }

            @Override
            public int position() {
                return 0;
            }
        };
    }

    @NotNull
    public static RangeAnnotation createEmptyRangeAnnotation() {
        return new RangeAnnotation() {
            @NotNull
            @Override
            public String gene() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public MutationType applicableMutationType() {
                return MutationType.ANY;
            }

            @NotNull
            @Override
            public String chromosome() {
                return Strings.EMPTY;
            }

            @Override
            public int start() {
                return 0;
            }

            @Override
            public int end() {
                return 0;
            }
        };
    }

    @NotNull
    public static GeneAnnotation createEmptyGeneAnnotation() {
        return new GeneAnnotation() {
            @NotNull
            @Override
            public String gene() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public GeneEvent event() {
                return GeneEvent.ANY_MUTATION;
            }
        };
    }
}
