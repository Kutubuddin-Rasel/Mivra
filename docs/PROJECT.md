# PROJECT.md

# Medicine Scanner — Product Definition

> **Working title:** Medicine Scanner  
> **Market focus:** Bangladesh-first  
> **Initial platform:** Android  
> **Document purpose:** Define what the product is, why it exists, who it serves, and the boundaries that must not drift during implementation.

---

## 1. Product thesis

People often have a medicine package in front of them but do not clearly know:

- what the medicine is;
- what its generic/active ingredient is;
- why it is commonly prescribed or used;
- common side effects;
- important warnings or precautions;
- what general safe-use advice applies.

Existing drug-reference experiences usually begin with **typing or searching**. Our hypothesis is that a camera-first workflow can remove enough friction to make medicine information easier to access, particularly for people who struggle with medicine names, spelling, small package text, or technical medical language.

The product thesis is:

> **Point the Android camera at a supported medicine package, identify likely medicine products safely, require confirmation when appropriate, and show trustworthy Bangla/English medicine information with clear provenance.**

The value is not “AI knows every pill.”  
The value is **low-friction identification + trustworthy explanation**.

---

## 2. Problem statement

A medicine user, caregiver, or family member may encounter:

- an unfamiliar blister strip;
- a box or bottle received earlier;
- multiple medicines with difficult brand names;
- packaging containing small or technical text;
- a product whose name is hard to type correctly.

The current workaround may involve:

- asking another person;
- typing the brand into Google;
- searching a medicine reference app;
- contacting a pharmacist or doctor;
- guessing from the package.

These approaches introduce friction and, in the worst case, unsafe assumptions.

We want to reduce the discovery friction **without pretending that the app replaces a doctor, pharmacist, official label, or prescription.**

---

## 3. Target users

### Primary users

1. **Ordinary medicine users**
   - want to understand a medicine already in front of them;
   - may know the brand but not the generic or purpose.

2. **Older users**
   - may have difficulty typing medicine names;
   - may benefit from camera-first interaction, Bangla, larger text, and later audio support.

3. **Family caregivers**
   - help parents or relatives understand and organize medicines;
   - may encounter several products at once.

### Secondary users

4. **Students / health-information seekers**
   - want a fast reference view of product identity and generic information.

5. **Pharmacists or healthcare workflows**
   - potentially useful later as a recognition/search utility, but not the first consumer MVP target.

---

## 4. Jobs to be done

### Core job

> “I have this medicine in front of me. Help me identify it and understand what it is.”

### Supporting jobs

> “Tell me the generic/ingredient and strength.”

> “Explain what this medicine is commonly used for in plain language.”

> “Show common side effects and important warnings.”

> “Let me read the explanation in Bangla or English.”

### Future jobs, not MVP commitments

> “Help me remember which medicines my family uses.”

> “Remind me about a medicine schedule or expiry.”

> “Help a caregiver manage a family medicine list.”

These future jobs must not distract from proving identification first.

---

## 5. Value proposition

### User-facing promise

> **Scan your medicine. Confirm what it is. Understand it safely.**

### What we are deliberately not promising

- “Scan any pill.”
- “Know with 100% certainty what a medicine is.”
- “AI doctor.”
- “Know what medicine you should take.”
- “Diagnose your condition.”
- “Verify if a medicine is fake.”
- “Replace your doctor or pharmacist.”

---

## 6. Core product workflow

```text
Medicine package
      ↓
Camera preview
      ↓
Capture / image analysis
      ↓
OCR + barcode signals
      ↓
Text normalization / field extraction
      ↓
Candidate matching
      ↓
Confidence + ambiguity rules
      ↓
Candidate result(s)
      ↓
User confirmation
      ↓
Medicine identity
      ↓
Reviewed medicine information
```

The system must support a safe failure path:

```text
Insufficient / conflicting evidence
      ↓
ABSTAIN
      ↓
Ask for a clearer photo, another side of the package,
barcode scan, or manual search
```

---

## 7. MVP identification scope

### Supported target packaging

- medicine box/carton;
- bottle or container label;
- printed blister/strip packaging;
- barcode/QR/Data Matrix where available and useful.

### Not initially supported as a confident identification promise

- arbitrary loose tablets;
- arbitrary loose capsules;
- medicine identification from color/shape alone;
- damaged/unreadable fragments where identity cannot be established;
- counterfeit/authenticity verification.

Loose-pill recognition may be researched later only if a reliable, authoritative identification dataset and validation method exist.

---

## 8. MVP information scope

After a medicine is confirmed, the app should aim to show:

1. **Identity**
   - brand/product name;
   - generic/active ingredient;
   - strength;
   - dosage form;
   - manufacturer;
   - official/canonical identifier where available.

2. **What is this?**
   - short plain-language description.

3. **Why is it commonly used?**
   - established indications/general uses;
   - wording must avoid implying that the user has any particular disease.

4. **Common side effects**
   - concise, understandable list.

5. **Important warnings / precautions**
   - clinically significant cautions;
   - clear escalation language where reviewed content supports it.

6. **General safe-use advice**
   - non-personalized, label-consistent advice;
   - prescription medicines should reinforce following clinician/pharmacist instructions.

7. **Provenance**
   - information source;
   - content/version identifier where practical;
   - review/last-updated date.

8. **Language**
   - English;
   - Bangla as a first-class product experience, not an afterthought.

---

## 9. Explicit MVP non-goals

The first product must **not** include:

- symptom diagnosis;
- disease prediction;
- prescription generation;
- “what medicine should I take?” recommendations;
- personalized dosing;
- automatic dose changes;
- automatic medicine substitution recommendations;
- drug purchasing as a core feature;
- manufacturer-sponsored result ranking;
- counterfeit/fake-medicine claims;
- live LLM-generated medical facts shown directly as trusted medicine advice;
- complex family medication management;
- social features;
- microservices or infrastructure added only for scale we do not yet have.

---

## 10. Safety principles

### 10.1 Identification safety

**False confident identification is the primary technical safety risk.**

Therefore:

- ambiguity must remain visible;
- confidence thresholds must be evidence-based;
- the app must abstain when evidence is inadequate;
- multiple candidates are preferable to a confident guess;
- strength and formulation differences are safety-relevant;
- same-brand variants must not be collapsed casually;
- user confirmation is part of the MVP workflow.

### 10.2 Information safety

- medicine facts must come from traceable sources;
- clinical content must be versioned;
- simplified language must preserve clinical meaning;
- LLMs may assist internal drafting/reformatting only under a review workflow;
- the final app must not rely on a live generative answer as its sole clinical source;
- product identity data and clinical knowledge data are separate concerns.

### 10.3 User-expectation safety

The UI must communicate uncertainty honestly.

Preferred language:

- “Possible match”
- “We found 3 likely medicines”
- “Please confirm the strength”
- “We could not identify this safely”

Avoid:

- “This is definitely…”
- “AI confirms…”
- “Safe for you”
- “You should take…”

unless a future regulated/validated workflow explicitly supports such claims.

---

## 11. Product principles

### P1 — Trust before feature count
A smaller trustworthy product is better than a broad unsafe product.

### P2 — Camera-first, search-second
The differentiator is recognition, while manual search remains a fallback.

### P3 — Local relevance
Bangladesh product identities, local brands/manufacturers, Bangla language, and local packaging matter.

### P4 — Accessible by design
Large readable results, low typing burden, clear language, and future audio support should be considered from the beginning.

### P5 — Privacy by default
Avoid uploading/storing images unless the feature genuinely requires it.

### P6 — Evidence before optimization
Measure recognition failure modes before adding image-processing or custom ML complexity.

### P7 — Explanation, not diagnosis
The product helps users understand a medicine already present; it does not decide what they should take.

---

## 12. Why Android first

Android is the first platform because:

- the product is Bangladesh-first;
- the human engineer already has native Android experience;
- native Android provides direct access to CameraX and on-device ML tooling;
- the project is intended to deepen camera, OCR, image-processing, lifecycle, performance, and security knowledge;
- starting native reduces cross-platform abstraction while the recognition problem is still uncertain.

iOS or cross-platform clients can be reconsidered only after the product hypothesis is proven.

---

## 13. Product hypotheses to validate

### H1 — Recognition value
Users find camera-based identification meaningfully easier than typing/searching medicine names.

### H2 — Technical feasibility
Real Bangladeshi medicine packaging contains enough readable signals for high-precision product candidate matching on common Android devices.

### H3 — Safe uncertainty
Users accept retry/candidate-confirmation flows when the system is uncertain.

### H4 — Local-language value
Bangla explanations materially improve comprehension for a meaningful segment of users.

### H5 — Data feasibility
A legitimate, maintainable Bangladesh medicine identity catalog and traceable clinical-information pipeline can be built without depending on unauthorized scraping.

These hypotheses must be validated independently. A good OCR demo alone does not prove the product.

---

## 14. Success metrics

### Recognition metrics

- top-1 candidate accuracy;
- top-3 recall;
- high-confidence precision;
- false-confident-match rate;
- abstention rate;
- first-photo success rate;
- recapture rate;
- field extraction accuracy (brand / strength / generic / manufacturer);
- barcode usefulness rate;
- end-to-end identification latency.

### Product metrics for later beta

- scan → confirmed medicine rate;
- time to confirmed identity;
- manual-search fallback rate;
- correction rate after suggested match;
- repeat scanning;
- language choice;
- user-reported comprehension;
- trust/safety feedback.

Download count is not the primary early success metric.

---

## 15. Data strategy principles

We need two distinct information systems:

### A. Medicine product identity

Examples:

- brand;
- generic;
- strength;
- manufacturer;
- dosage form;
- canonical/registration identifier;
- package aliases;
- barcode/GTIN mappings;
- registration status.

### B. Clinical medicine knowledge

Examples:

- indications;
- common side effects;
- warnings;
- contraindications/precautions;
- general safe-use information;
- source;
- source version;
- review status.

Do not merge these into a single untraceable “medicine description” record.

---

## 16. Monetization position

Monetization is **not an MVP requirement**.

Trust must be established first.

Potential future models may include:

- consumer premium medication-management features;
- family/caregiver features;
- B2B recognition API/SDK;
- healthcare/pharmacy integrations;
- enterprise data/recognition services.

Any monetization must not distort medicine identity, warnings, or safety-critical information.

---

## 17. Primary risks

| Risk | Why it matters | Current response |
|---|---|---|
| False confident identification | Could mislead a user about a medicine | Abstention, multiple signals, confirmation |
| OCR failure on reflective strips | Common real packaging is difficult | Baseline benchmark, then targeted preprocessing |
| Similar brand variants | Strength/formulation can differ | Field-aware matching, not name-only search |
| Incomplete/poor data provenance | Medical content becomes untrustworthy | Separate identity/clinical layers + sources |
| Bangla OCR limitations | Some packaging may rely on Bengali script | Validate actual package mix; investigate fallback only if needed |
| Privacy leakage | Medication history is sensitive | Local-first/minimal collection |
| Scope creep into diagnosis | Raises safety and regulatory complexity | Explicit non-goals |
| Premature custom ML | Can waste months before baseline is understood | ML Kit/OCR baseline first |
| Dependence on scraped proprietary data | Legal/maintenance risk | Official/open/licensed sources only |

---

## 18. Current project stage

We are **not building the full MVP yet**.

Current stage:

> **Recognition Feasibility / Milestone 0**

The first major question is:

> **Can a normal Android phone extract enough trustworthy evidence from real Bangladeshi medicine packaging to rank the correct product safely?**

Until this is measured, avoid spending significant time on:

- authentication;
- polished onboarding;
- family accounts;
- backend scaling;
- complex security layers;
- advanced clinical content screens;
- custom ML models;
- production monetization.

---

## 19. Product decision rule

A proposed feature should answer all four questions:

1. Does it solve a real user problem?
2. Is it inside the current milestone?
3. Can we implement it safely?
4. Is there evidence that the added complexity is justified?

If not, defer it.

---

## 20. Version-sensitive references to re-check before implementation/release

These sources are useful anchors but must be revalidated because APIs, policies, and regulatory guidance change:

- Bangladesh Core FHIR / DGDA Registered Drugs value set:  
  https://fhir.dghs.gov.bd/core/ValueSet-dgda-registered-drugs.html
- ML Kit Text Recognition for Android:  
  https://developers.google.com/ml-kit/vision/text-recognition/v2/android
- ML Kit Barcode Scanning for Android:  
  https://developers.google.com/ml-kit/vision/barcode-scanning/android
- CameraX documentation:  
  https://developer.android.com/media/camera/camerax
- Google Play health-app policy:  
  https://support.google.com/googleplay/android-developer/answer/16679511
- DGDA Bangladesh:  
  https://dgda.gov.bd/

---

## 21. One-sentence project definition

> **A Bangladesh-first Android application that uses camera-derived evidence to safely identify supported medicine packaging and provide trustworthy, understandable Bangla/English medicine information without pretending to diagnose or prescribe.**
