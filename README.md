# ACTIN

(in development)

ACTIN is a system that determines available treatment options for cancer patients based on the following inputs:
 - Comprehensive clinical record of the patient
 - Comprehensive molecular analysis of the tumor
 - Set of all treatment options available (standard-of-care (SOC) and experimental).

![ACTIN System](actin-system/src/main/resources/actin_system.png)
 
More details on the following modules are available from the links below:

Module  | Description
---|---
[ACTIN Clinical](actin-clinical) | Ingestion and curation of an external electronic health record (EHR) clinical data stream.
[ACTIN Treatment](actin-treatment) | Generation of available treatment options (standard-of-care and experimental).
[ACTIN Algo](actin-algo) | Matching all data from a patient to available treatment options.
[ACTIN Report](actin-report) | Writes the output of ACTIN Algo to a PDF report.
  