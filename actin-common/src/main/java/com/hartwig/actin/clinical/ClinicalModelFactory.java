package com.hartwig.actin.clinical;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.feed.ClinicalFeed;
import com.hartwig.actin.clinical.feed.ClinicalFeedFactory;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class ClinicalModelFactory {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalModelFactory.class);

    private ClinicalModelFactory() {
    }

    @NotNull
    public static ClinicalModel loadFromClinicalFeedDirectory(@NotNull String clinicalFeedDirectory) throws IOException {
        ClinicalFeed clinicalFeed = ClinicalFeedFactory.read(clinicalFeedDirectory);

        LOGGER.info("Creating clinical datamodel");
        List<ClinicalRecord> records = Lists.newArrayList();
        for (PatientEntry patient : clinicalFeed.patientEntries()) {
            String sampleId = toSampleId(patient.subject());

            LOGGER.info(" Adding data for sample '{}'", sampleId);
            records.add(ImmutableClinicalRecord.builder()
                    .sampleId(sampleId)
                    .patient(createPatientDetails(patient))
                    .tumor(createTumorDetails())
                    .clinicalStatus(createClinicalStatus())
                    .priorTumorTreatments(extractPriorTumorTreatments(clinicalFeed, patient.subject()))
                    .build());
        }

        return new ClinicalModel(records);
    }

    @NotNull
    private static List<PriorTumorTreatment> extractPriorTumorTreatments(@NotNull ClinicalFeed clinicalFeed, @NotNull String subject) {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();

        return priorTumorTreatments;
    }

    @NotNull
    private static PatientDetails createPatientDetails(@NotNull PatientEntry patient) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return ImmutablePatientDetails.builder()
                .sex(Sex.MALE)
                .birthYear(0)
                .registrationDate(LocalDate.parse("01-01-2020", format))
                .questionnaireDate(LocalDate.parse("01-01-2020", format))
                .build();
    }

    @NotNull
    private static TumorDetails createTumorDetails() {
        return ImmutableTumorDetails.builder()
                .primaryTumorLocation(Strings.EMPTY)
                .primaryTumorSubLocation(Strings.EMPTY)
                .primaryTumorType(Strings.EMPTY)
                .primaryTumorSubType(Strings.EMPTY)
                .stage(Strings.EMPTY)
                .hasMeasurableLesionRecist(false)
                .hasBrainLesions(false)
                .hasActiveBrainLesions(false)
                .hasSymptomaticBrainLesions(false)
                .hasCnsLesions(false)
                .hasActiveCnsLesions(false)
                .hasSymptomaticCnsLesions(false)
                .hasBoneLesions(false)
                .hasLiverLesions(false)
                .hasOtherLesions(false)
                .build();
    }

    @NotNull
    private static ClinicalStatus createClinicalStatus() {
        return ImmutableClinicalStatus.builder()
                .who(0)
                .hasCurrentInfection(false)
                .hasSigAberrationLatestEcg(false)
                .cancerRelatedComplication(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static String toSampleId(@NotNull String subject) {
        // Assume a single sample per patient
        return subject.replaceAll("-", "") + "T";
    }
}
