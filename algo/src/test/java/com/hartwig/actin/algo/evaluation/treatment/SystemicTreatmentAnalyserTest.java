package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class SystemicTreatmentAnalyserTest {

    @Test
    public void canCountSystemicTreatments() {
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEquals(0, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(0, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add one systemic treatment.
        treatments.add(TreatmentTestFactory.builder().name("treatment A").year(2022).month(5).isSystemic(true).build());
        assertEquals(1, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(1, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add one non-systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment B").year(2022).month(2).isSystemic(false).build());
        assertEquals(1, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(1, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add another systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment A").year(2021).month(10).isSystemic(true).build());
        assertEquals(2, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(2, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add another systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment A").year(2021).month(5).isSystemic(true).build());
        assertEquals(2, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(3, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add another systemic treatment without date
        treatments.add(TreatmentTestFactory.builder().name("treatment A").isSystemic(true).build());
        assertEquals(2, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(4, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add another systemic treatment with just year
        treatments.add(TreatmentTestFactory.builder().name("treatment A").year(2021).isSystemic(true).build());
        assertEquals(2, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(5, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Add different systemic treatment with just year
        treatments.add(TreatmentTestFactory.builder().name("treatment C").year(2021).isSystemic(true).build());
        assertEquals(3, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(6, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));

        // Make sure one older non-systemic doesnt screw up.
        treatments.add(TreatmentTestFactory.builder().name("treatment D").year(2019).month(5).isSystemic(false).build());
        assertEquals(3, SystemicTreatmentAnalyser.minSystemicTreatments(treatments));
        assertEquals(6, SystemicTreatmentAnalyser.maxSystemicTreatments(treatments));
    }
}