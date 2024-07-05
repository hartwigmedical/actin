package com.hartwig.actin.clinical.feed.mcgi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.io.Files
import java.io.File
import java.time.LocalDate
import org.junit.Test

class McgiFeedAdapterTest {

    @Test
    fun `Create and serialize patient record`() {
        val patientRecord = McgiFeedAdapter().convert(
            McgiPatient(
                caseNumber = 24,
                age = 65,
                tumorStage = "IV",
                diagnosisDate = LocalDate.of(2022, 5, 1),
                gender = "Female",
                tumorLocation = "Lung",
                tumorType = "carcinoma",
                tumorGradeDifferentiation = "Poorly differentiated",
                lesionSite = "PET-CT shows extensive supraclavicular, mediastinal hypermetabolic adenopathy w/ multiple malignant nodules",
                clinical = McgiClinical(
                    listOf(
                        "History of continued smoking",
                        "6/2022: Referred for palliative RT, PET-CT shows extensive supraclavicular, mediastinal hypermetabolic adenopathy w/ multiple malignant nodules",
                        "6/2022-7/2022: Carbo/Taxol/Pembrolizumab",
                        "7/2022-present: Maintenance Pembrolizumab",
                        "10/2022: Significant treatment response",
                        "3/2023: Disease progression"
                    )
                ),
                molecular = McgiMolecular(
                    variants = listOf(
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "CHEK2",
                            hgvsCodingEffect = "c.W93fs*15",
                            vaf = 0.37
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "FANCL",
                            hgvsCodingEffect = "c.Q18*",
                            vaf = 0.39
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "KRAS",
                            hgvsCodingEffect = "c.G13F",
                            vaf = 4.1
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "STK11",
                            hgvsCodingEffect = "c.K64fs*90",
                            vaf = 5.3
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "ASXL1",
                            hgvsCodingEffect = "c.E635fs*15",
                            vaf = 1.1
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "TP53",
                            hgvsCodingEffect = "c.G244S",
                            vaf = 6.8
                        ),
                        McgiVariant(
                            resultDate = LocalDate.of(2023, 4, 1),
                            testType = "Liquid CDx",
                            gene = "TP53",
                            hgvsCodingEffect = "c.M246I",
                            vaf = 2.0
                        ),

                        ), amplications = listOf(
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "BRAF",
                            chromosome = "7"
                        ),
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "FGF10",
                            chromosome = "5"
                        ),
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "KEL",
                            chromosome = "7"
                        ),
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "MCL1",
                            chromosome = "1"
                        ),
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "MYC",
                            chromosome = "8"
                        ),
                        McgiAmplification(
                            resultDate = LocalDate.of(2022, 7, 1),
                            testType = "FoundationOne CDx",
                            gene = "NTRK1",
                            chromosome = "1"
                        )
                    ), isMSI = false, tmb = 11.0
                )
            )
        )
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
        }
        Files.write(objectMapper.writeValueAsString(patientRecord).toByteArray(), File("case24.json"))
    }

}