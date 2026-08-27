# Local NSFW Classifier Training and Data Plan

## Status: evidence planning only

This document records Project Veil’s training-data decision process. **No adult-content images, video frames, model artifact, or user screen frame has been downloaded, bundled, or used for training in this milestone.** The first outcome is a source-approval gate, not a rushed model.

## Candidate-source observations

The reviewed public GantMan NSFW-model repository is unsuitable as a direct training-data source for Veil because its own documentation says the training data is not easily distributable; it also describes a 299 × 299 model family rather than Veil’s 224 × 224 model contract.[1] The Falconsai model page identifies an Apache-2.0 model, but it is distributed as a PyTorch/Transformers/Safetensors model card, not an evaluated Android TensorFlow Lite release or a documented training-data package.[2] Neither candidate has been accepted, downloaded, or used.

Google’s LiteRT image-classification guidance supports a local training/evaluation/export workflow, including separate evaluation data and TensorFlow Lite export. It does not solve the legal provenance of adult-content data, so Veil treats source evidence as a separate, mandatory gate.[3]

The Figshare adult-content-detection dataset record describes 30,800 images but also says the third-party material may remain copyrighted, is limited to non-profit research, and is distributed on a controlled/fair-use basis. It cannot be used for Project Veil’s distributable consumer-app training program, despite the record’s MIT metadata label.[4] A prospective licensed source, Defined.ai, publicly describes a 60.4K-image NSFW content-moderation dataset and offers a sample/quote path, but the precise license, adult-only assurance, permitted model distribution, dataset card, and evaluation terms are not public on the listing. It is therefore a **possible procurement lead**, not an approved dataset.[5]

Free Hugging Face hosting does not resolve Veil’s approval requirements. The DeepGHS `nsfw_detect` listing displays an MIT label but the dataset card is access-restricted, preventing verification of provenance, adult-only assurance, detailed use conditions, and label methodology without authenticated access.[6] The DarkyMan `nsfw-image-classification` listing exposes only a non-specific `cc` label and its sensitive dataset card is not publicly available in the unauthenticated review view; the Creative Commons variant, provenance, commercial rights, and adult-only assurance are unresolved.[7] **Neither free Hugging Face dataset is approved or downloaded.**

The Hugging Face `yuwan0/UnsafeSceneDetection` dataset is explicitly unavailable for Veil: its published access terms limit use to non-commercial research/education, prohibit redistribution, require case-by-case access, and describe a text-to-image scene-graph objective rather than Veil’s Android screen-image classifier target.[8]

## Dataset source-approval gate

| Requirement | Evidence required before download or use |
|---|---|
| Adult-only content | Written source statement that all depicted persons are adults or that the source’s collection policy reliably enforces this. |
| Right to train and redistribute | A specific dataset license plus terms that permit Veil’s intended model training and model distribution. |
| Provenance | Publisher, collection method, label taxonomy, release version/date, and an immutable source reference. |
| No private or user data | No personal accounts, scraped private material, leaked material, or any Veil user content. |
| Label quality | Annotation definitions, annotator process if known, ambiguity policy, and a documented error-review process. |
| Distribution controls | Access method and storage controls that avoid adding raw adult media to the Android source repository or mobile application. |
| Independent evaluation | A held-out, separately sourced and licensed evaluation set, including false-positive challenge categories. Its source URL and reviewed minimum class counts are mandatory manifest fields. |

## Training design after data approval

Veil will use a binary image-classification candidate with explicit and non-explicit classes. The non-explicit side must deliberately cover medical imagery, anatomy diagrams, art, swimwear, fitness, family photos, education content, animation, low brightness, crops, thumbnails, split screens, and rotation. This gives the model a way to be evaluated against the common false-positive categories documented in Veil’s AI architecture.

The candidate must be trained and evaluated locally in a controlled workspace, converted to the `224 × 224` RGB float 0–1 TensorFlow Lite contract, and accepted only if it passes the existing review-manifest gate. The Android APK will contain only the reviewed `.tflite` model and its review manifest—not raw training data, evaluation data, datasets, or captured user media.

## Hard boundaries

The future model remains local-only. It does not authorize cloud inference, background model downloading, capturing user screenshots for training, uploading confidence scores, storing frames, OCR, browsing-history collection, or hidden monitoring. Real foreground detection, screen capture, overlay enforcement, and app-specific blocking remain separate Android milestones that require their own explicit consent, degraded-state experience, and Galaxy M20 validation.

## References

[1]: https://github.com/GantMan/nsfw_model "GantMan/nsfw_model repository"
[2]: https://huggingface.co/Falconsai/nsfw_image_detection "Falconsai NSFW image-detection model card"
[3]: https://developers.google.com/edge/litert/libraries/modify/image_classification "Google AI Edge — LiteRT image classification guidance"
[4]: https://api.figshare.com/v2/articles/14495367 "Figshare adult-content detection dataset metadata and usage disclaimer"
[5]: https://defined.ai/datasets/nsfw-adult-content-1 "Defined.ai NSFW Adult Content dataset listing"
[6]: https://huggingface.co/datasets/deepghs/nsfw_detect "DeepGHS NSFW dataset listing"
[7]: https://huggingface.co/datasets/DarkyMan/nsfw-image-classification "DarkyMan NSFW image-classification dataset listing"
[8]: https://huggingface.co/datasets/yuwan0/UnsafeSceneDetection "UnsafeSceneDetection dataset card and access conditions"
