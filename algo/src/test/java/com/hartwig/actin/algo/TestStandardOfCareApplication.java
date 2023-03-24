package com.hartwig.actin.algo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.PatientRecordFactory;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.soc.EvaluatedTreatmentInterpreter;
import com.hartwig.actin.algo.soc.RecommendationEngine;
import com.hartwig.actin.algo.soc.TreatmentDB;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.DoidModelFactory;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.serialization.DoidJson;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.util.MolecularPrinter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TestStandardOfCareApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestStandardOfCareApplication.class);
    private static final String DOID_JSON_PATH =
            String.join(File.separator, System.getProperty("user.home"), "hmf", "repos", "common-resources-public", "doid.json");

    public static void main(@NotNull String... args) throws IOException {
        new TestStandardOfCareApplication().run();
    }

    public void run() throws IOException {
        PatientRecord patient = patient();
        LOGGER.info("Running ACTIN Test SOC Application with clinical record");
        ClinicalPrinter.printRecord(patient.clinical());
        LOGGER.info("and molecular record");
        MolecularPrinter.printRecord(patient.molecular());

        LOGGER.info("Loading DOID tree from {}", DOID_JSON_PATH);
        DoidEntry doidEntry = DoidJson.readDoidOwlEntry(DOID_JSON_PATH);
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size());
        DoidModel doidModel = DoidModelFactory.createFromDoidEntry(doidEntry);

        RecommendationEngine recommendationEngine =
                new RecommendationEngine(doidModel, ReferenceDateProviderTestFactory.createCurrentDateProvider());
        EvaluatedTreatmentInterpreter recommendationInterpreter =
                recommendationEngine.provideRecommendations(patient, TreatmentDB.loadTreatments());

        LOGGER.info(recommendationInterpreter.summarize());
        LOGGER.info(recommendationInterpreter.csv());
    }

    private static PatientRecord patient() {
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .tumor(tumorDetails)
                .priorTumorTreatments(priorTreatmentStreamFromNames(List.of("CAPOX"), TreatmentCategory.CHEMOTHERAPY))
                .build();
        MolecularRecord molecularRecord = TestMolecularFactory.createProperTestMolecularRecord();

        return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord);
    }

    private static Set<ImmutablePriorTumorTreatment> priorTreatmentStreamFromNames(List<String> names, TreatmentCategory category) {
        return names.stream()
                .map(treatmentName -> ImmutablePriorTumorTreatment.builder()
                        .name(treatmentName)
                        .isSystemic(true)
                        .startYear(LocalDate.now().getYear())
                        .addCategories(category)
                        .build())
                .collect(Collectors.toSet());
    }
}
