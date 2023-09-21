package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterData;
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus;
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType;
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation;
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus;
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VirusExtractorTest {

    @Test
    public void canExtractViruses() {
        AnnotatedVirus virusEntry1 = TestVirusInterpreterFactory.builder()
                .reported(true)
                .name("virus 1")
                .qcStatus(VirusExtractor.QC_PASS_STATUS)
                .interpretation(VirusInterpretation.HPV)
                .integrations(2)
                .virusDriverLikelihoodType(VirusLikelihoodType.HIGH)
                .build();

        AnnotatedVirus virusEntry2 = TestVirusInterpreterFactory.builder()
                .reported(false)
                .name("virus 2")
                .qcStatus(VirusBreakendQCStatus.LOW_VIRAL_COVERAGE)
                .interpretation(null)
                .integrations(0)
                .virusDriverLikelihoodType(VirusLikelihoodType.LOW)
                .build();

        VirusInterpreterData virusInterpreter = ImmutableVirusInterpreterData.builder()
                .addAllViruses(virusEntry1, virusEntry2)
                .build();

        VirusExtractor virusExtractor = new VirusExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Virus> viruses = virusExtractor.extract(virusInterpreter);
        assertEquals(2, viruses.size());

        Virus virus1 = findByName(viruses, "virus 1");
        assertTrue(virus1.isReportable());
        assertEquals(DriverLikelihood.HIGH, virus1.driverLikelihood());
        assertEquals(VirusType.HUMAN_PAPILLOMA_VIRUS, virus1.type());
        assertTrue(virus1.isReliable());
        assertEquals(2, virus1.integrations());

        Virus virus2 = findByName(viruses, "virus 2");
        assertFalse(virus2.isReportable());
        assertEquals(DriverLikelihood.LOW, virus2.driverLikelihood());
        assertEquals(VirusType.OTHER, virus2.type());
        assertFalse(virus2.isReliable());
        assertEquals(0, virus2.integrations());
    }

    @Test
    public void canDetermineDriverLikelihoodForAllVirusDriverLikelihoods() {
        Map<VirusLikelihoodType, DriverLikelihood> expectedDriverLikelihoodLookup = new HashMap<>();
        expectedDriverLikelihoodLookup.put(VirusLikelihoodType.LOW, DriverLikelihood.LOW);
        expectedDriverLikelihoodLookup.put(VirusLikelihoodType.HIGH, DriverLikelihood.HIGH);
        expectedDriverLikelihoodLookup.put(VirusLikelihoodType.UNKNOWN, null);

        for (VirusLikelihoodType virusDriverLikelihood : VirusLikelihoodType.values()) {
            assertEquals(expectedDriverLikelihoodLookup.get(virusDriverLikelihood),
                    VirusExtractor.determineDriverLikelihood(virusDriverLikelihood));
        }
    }

    @Test
    public void canDetermineTypeForAllInterpretations() {
        assertEquals(VirusType.OTHER, VirusExtractor.determineType(null));

        for (VirusInterpretation interpretation : VirusInterpretation.values()) {
            assertNotNull(VirusExtractor.determineType(interpretation));
        }
    }

    @NotNull
    private static Virus findByName(@NotNull Set<Virus> viruses, @NotNull String nameToFind) {
        for (Virus virus : viruses) {
            if (virus.name().equals(nameToFind)) {
                return virus;
            }
        }

        throw new IllegalStateException("Could not find virus with name: " + nameToFind);
    }
}