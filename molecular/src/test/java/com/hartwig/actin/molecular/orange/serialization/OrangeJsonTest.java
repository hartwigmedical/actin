package com.hartwig.actin.molecular.orange.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeJsonTest {

    private static final String MINIMALLY_EMPTY_ORANGE_JSON = Resources.getResource("serialization/minimally.empty.orange.json").getPath();
    private static final String MINIMALLY_POPULATED_ORANGE_JSON =
            Resources.getResource("serialization/minimally.populated.orange.json").getPath();
    private static final String REAL_ORANGE_JSON = Resources.getResource("serialization/real.orange.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadMinimallyEmptyOrangeRecordJson() throws IOException {
        assertNotNull(OrangeJson.read(MINIMALLY_EMPTY_ORANGE_JSON));
    }

    @Test
    public void canReadRealOrangeRecordJson() throws IOException {
        assertNotNull(OrangeJson.read(REAL_ORANGE_JSON));
    }

    @Test
    public void canReadMinimallyPopulatedOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(MINIMALLY_POPULATED_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(LocalDate.of(2022, 1, 20), record.experimentDate());

        assertPurple(record.purple());
        assertLinx(record.linx());
        assertPeach(record.peach());
        assertCuppa(record.cuppa());
        assertVirusInterpreter(record.virusInterpreter());
        assertLilac(record.lilac());
        assertChord(record.chord());
    }

    private static void assertPurple(@NotNull PurpleRecord purple) {
        assertTrue(purple.hasSufficientQuality());
        assertEquals(0.99, purple.purity(), EPSILON);
        assertEquals(3.1, purple.ploidy(), EPSILON);
        assertTrue(purple.containsTumorCells());
        assertEquals("MSS", purple.microsatelliteStabilityStatus());
        assertEquals(13.71, purple.tumorMutationalBurden(), EPSILON);
        assertEquals(185, purple.tumorMutationalLoad());
        assertEquals("HIGH", purple.tumorMutationalLoadStatus());

        assertEquals(2, purple.variants().size());
        PurpleVariant variant1 = findByGene(purple.variants(), "SF3B1");
        assertEquals("p.Pro718Leu", variant1.canonicalHgvsProteinImpact());
        assertEquals("c.2153C>T", variant1.canonicalHgvsCodingImpact());
        assertTrue(variant1.canonicalEffects().contains(PurpleVariantEffect.MISSENSE));
        assertEquals(2.03, variant1.alleleCopyNumber(), EPSILON);
        assertEquals(3.02, variant1.totalCopyNumber(), EPSILON);
        assertEquals(VariantHotspot.NON_HOTSPOT, variant1.hotspot());
        assertFalse(variant1.biallelic());
        assertEquals(0.15, variant1.driverLikelihood(), EPSILON);
        assertEquals(1.0, variant1.clonalLikelihood(), EPSILON);

        PurpleVariant variant2 = findByGene(purple.variants(), "BRCA1");
        assertEquals("p.?", variant2.canonicalHgvsProteinImpact());
        assertEquals("c.5340+1G>A", variant2.canonicalHgvsCodingImpact());
        assertTrue(variant2.canonicalEffects().contains(PurpleVariantEffect.SPLICE_DONOR));
        assertTrue(variant2.canonicalEffects().contains(PurpleVariantEffect.INTRONIC));
        assertEquals(1.0, variant2.alleleCopyNumber(), EPSILON);
        assertEquals(2.0, variant2.totalCopyNumber(), EPSILON);
        assertEquals(VariantHotspot.HOTSPOT, variant2.hotspot());
        assertFalse(variant2.biallelic());
        assertEquals(1.0, variant2.driverLikelihood(), EPSILON);
        assertEquals(1.0, variant2.clonalLikelihood(), EPSILON);

        assertEquals(1, purple.copyNumbers().size());
        PurpleCopyNumber copyNumber = purple.copyNumbers().iterator().next();
        assertEquals("SMAD4", copyNumber.gene());
        assertEquals(CopyNumberInterpretation.FULL_LOSS, copyNumber.interpretation());
        assertEquals(0, copyNumber.minCopies());
    }

    @NotNull
    private static PurpleVariant findByGene(@NotNull Iterable<PurpleVariant> variants, @NotNull String geneToFind) {
        for (PurpleVariant variant : variants) {
            if (variant.gene().equals(geneToFind)) {
                return variant;
            }
        }

        throw new IllegalStateException("Could not find variant for gene: " + geneToFind);
    }

    private static void assertLinx(@NotNull LinxRecord linx) {
        assertEquals(1, linx.fusions().size());
        LinxFusion fusion = linx.fusions().iterator().next();
        assertEquals(FusionType.KNOWN_PAIR, fusion.type());
        assertEquals("TMPRSS2", fusion.geneStart());
        assertEquals("ETV4", fusion.geneEnd());
        assertEquals(FusionDriverLikelihood.HIGH, fusion.driverLikelihood());

        assertEquals(1, linx.homozygousDisruptions().size());
        LinxHomozygousDisruption homozygousDisruption = linx.homozygousDisruptions().iterator().next();
        assertEquals("NF1", homozygousDisruption.gene());

        assertEquals(1, linx.disruptions().size());
        LinxDisruption disruption = linx.disruptions().iterator().next();
        assertEquals("NF1", disruption.gene());
    }

    private static void assertPeach(@NotNull PeachRecord peach) {
        assertEquals(1, peach.entries().size());
        PeachEntry entry = peach.entries().iterator().next();
        assertEquals("DPYD", entry.gene());
        assertEquals("*1_HOM", entry.haplotype());
    }

    private static void assertCuppa(@NotNull CuppaRecord cuppa) {
        assertEquals(1, cuppa.predictions().size());
        CuppaPrediction prediction = cuppa.predictions().iterator().next();
        assertEquals("Melanoma", prediction.cancerType());
        assertEquals(0.996, prediction.likelihood(), EPSILON);
    }

    private static void assertVirusInterpreter(@NotNull VirusInterpreterRecord virusInterpreter) {
        assertEquals(1, virusInterpreter.entries().size());
        VirusInterpreterEntry entry = virusInterpreter.entries().iterator().next();
        assertEquals("Human betaherpesvirus 6B", entry.name());
        assertEquals(VirusInterpretation.EBV, entry.interpretation());
        assertEquals(1, entry.integrations());
        assertEquals(VirusDriverLikelihood.HIGH, entry.driverLikelihood());
    }

    private static void assertLilac(@NotNull LilacRecord lilac) {
        assertEquals("PASS", lilac.qc());
        assertEquals(1, lilac.alleles().size());
        LilacHlaAllele allele = lilac.alleles().iterator().next();
        assertEquals("A*01:01", allele.allele());
        assertEquals(6.1, allele.tumorCopyNumber(), EPSILON);
        assertEquals(5.0, allele.somaticMissense(), EPSILON);
        assertEquals(4.0, allele.somaticNonsenseOrFrameshift(), EPSILON);
        assertEquals(3.0, allele.somaticSplice(), EPSILON);
        assertEquals(1.0, allele.somaticInframeIndel(), EPSILON);
    }

    private static void assertChord(@NotNull ChordRecord chord) {
        assertEquals("HR_PROFICIENT", chord.hrStatus());
    }
}