package com.hartwig.actin.molecular.orange.evidence.matching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantType;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.serve.datamodel.MutationType;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MutationTypeMatchingTest {

    @Test
    public void worksForEveryCodingEffect() {
        PurpleVariant nonCoding = withCodingEffect(PurpleCodingEffect.NONE).build();
        for (MutationType type : MutationType.values()) {
            assertFalse(MutationTypeMatching.matches(type, nonCoding));
        }
    }

    @Test
    public void canMatchMutationTypes() {
        PurpleVariant nonsenseOrFrameshift = withCodingEffect(PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT).build();
        assertTrue(MutationTypeMatching.matches(MutationType.NONSENSE_OR_FRAMESHIFT, nonsenseOrFrameshift));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, nonsenseOrFrameshift));

        PurpleVariant splice = withCodingEffect(PurpleCodingEffect.SPLICE).build();
        assertTrue(MutationTypeMatching.matches(MutationType.SPLICE, splice));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice));

        PurpleVariant inframe = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("AAG").alt("TTG").build();
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframe));
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframe));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframe));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframe));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice));

        PurpleVariant inframeDeletion =
                withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("ATGATG").alt("TTT").build();
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeDeletion));
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeDeletion));
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeDeletion));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeDeletion));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeDeletion));

        PurpleVariant inframeInsertion =
                withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("TTT").alt("ATGATG").build();
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeInsertion));
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeInsertion));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeInsertion));
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeInsertion));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeInsertion));

        PurpleVariant missense = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.SNP).build();
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, missense));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME, missense));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, missense));
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, missense));
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, missense));
    }

    @NotNull
    private static ImmutablePurpleVariant.Builder withCodingEffect(@NotNull PurpleCodingEffect codingEffect) {
        return TestPurpleFactory.variantBuilder()
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build());
    }
}