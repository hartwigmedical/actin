package com.hartwig.actin.molecular.orange.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
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
        assertEquals(LocalDate.of(2022, 1, 20), record.reportDate());

        assertPurple(record.purple());
        assertLinx(record.linx());
        assertPeach(record.peach());
        assertCuppa(record.cuppa());
        assertVirusInterpreter(record.virusInterpreter());
        assertChord(record.chord());
        assertProtect(record.protect());
    }

    private static void assertPurple(@NotNull PurpleRecord purple) {
        assertTrue(purple.hasReliableQuality());
        assertEquals(0.99, purple.purity(), EPSILON);
        assertTrue(purple.hasReliablePurity());
        assertEquals("MSS", purple.microsatelliteStabilityStatus());
        assertEquals(13.71, purple.tumorMutationalBurden(), EPSILON);
        assertEquals(185, purple.tumorMutationalLoad());

        assertEquals(2, purple.variants().size());
        PurpleVariant variant1 = findByGene(purple.variants(), "SF3B1");
        assertEquals("p.Pro718Leu", variant1.hgvsProteinImpact());
        assertEquals("c.2153C>T", variant1.hgvsCodingImpact());
        assertEquals("missense_variant", variant1.effect());
        assertEquals(2.03, variant1.alleleCopyNumber(), EPSILON);
        assertEquals(3.02, variant1.totalCopyNumber(), EPSILON);
        assertEquals(VariantHotspot.NON_HOTSPOT, variant1.hotspot());
        assertFalse(variant1.biallelic());
        assertEquals(0.15, variant1.driverLikelihood(), EPSILON);
        assertEquals(1.0, variant1.clonalLikelihood(), EPSILON);

        PurpleVariant variant2 = findByGene(purple.variants(), "BRCA1");
        assertEquals("p.?", variant2.hgvsProteinImpact());
        assertEquals("c.5340+1G>A", variant2.hgvsCodingImpact());
        assertEquals("splice_donor_variant&intron_variant", variant2.effect());
        assertEquals(1.0, variant2.alleleCopyNumber(), EPSILON);
        assertEquals(2.0, variant2.totalCopyNumber(), EPSILON);
        assertEquals(VariantHotspot.HOTSPOT, variant2.hotspot());
        assertFalse(variant2.biallelic());
        assertEquals(1.0, variant2.driverLikelihood(), EPSILON);
        assertEquals(1.0, variant2.clonalLikelihood(), EPSILON);

        assertEquals(1, purple.gainsLosses().size());
        PurpleGainLoss gainLoss = purple.gainsLosses().iterator().next();
        assertEquals("SMAD4", gainLoss.gene());
        assertEquals(GainLossInterpretation.FULL_LOSS, gainLoss.interpretation());
        assertEquals(0, gainLoss.minCopies());
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
        assertEquals("Exon 1", fusion.geneContextStart());
        assertEquals("ETV4", fusion.geneEnd());
        assertEquals("Promoter Region", fusion.geneContextEnd());
        assertEquals(FusionDriverLikelihood.HIGH, fusion.driverLikelihood());

        assertEquals(1, linx.homozygousDisruptedGenes().size());
        assertEquals("NF1", linx.homozygousDisruptedGenes().iterator().next());

        assertEquals(1, linx.disruptions().size());
        LinxDisruption disruption = linx.disruptions().iterator().next();
        assertEquals("NF1", disruption.gene());
        assertEquals("Intron 5 -> Intron 8", disruption.range());
    }

    private static void assertPeach(@NotNull PeachRecord peach) {
        assertEquals(1, peach.entries().size());
        PeachEntry entry = peach.entries().iterator().next();
        assertEquals("DPYD", entry.gene());
        assertEquals("*1_HOM", entry.haplotype());
    }

    private static void assertCuppa(@NotNull CuppaRecord cuppa) {
        assertEquals("Melanoma", cuppa.predictedCancerType());
        assertEquals(0.996, cuppa.bestPredictionLikelihood(), EPSILON);
    }

    private static void assertVirusInterpreter(@NotNull VirusInterpreterRecord virusInterpreter) {
        assertEquals(1, virusInterpreter.entries().size());
        VirusInterpreterEntry entry = virusInterpreter.entries().iterator().next();
        assertEquals("Human betaherpesvirus 6B", entry.name());
        assertEquals("EBV", entry.interpretation());
        assertEquals(1, entry.integrations());
        assertEquals(VirusDriverLikelihood.HIGH, entry.driverLikelihood());
    }

    private static void assertChord(@NotNull ChordRecord chord) {
        assertEquals("HR_PROFICIENT", chord.hrStatus());
    }

    private static void assertProtect(@NotNull ProtectRecord protect) {
        assertEquals(1, protect.evidences().size());
        ProtectEvidence evidence = protect.evidences().iterator().next();
        assertTrue(evidence.reported());
        assertEquals("BRAF", evidence.gene());
        assertEquals("p.Val600Glu", evidence.event());
        assertEquals("Cobimetinib + Vemurafenib", evidence.treatment());
        assertTrue(evidence.onLabel());
        assertEquals(EvidenceLevel.A, evidence.level());
        assertEquals(EvidenceDirection.RESPONSIVE, evidence.direction());

        assertEquals(1, evidence.sources().size());
        ProtectSource source = evidence.sources().iterator().next();
        assertEquals("VICC_CGI", source.name());
        assertEquals("hotspot", source.event());
        assertEquals(EvidenceType.HOTSPOT_MUTATION, source.type());
        assertNull(source.rangeRank());
    }
}