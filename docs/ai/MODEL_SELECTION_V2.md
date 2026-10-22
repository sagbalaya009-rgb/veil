# V2 model decision — 2026-10-22

Keep the existing pinned GantMan MobileNetV2 1.40 / 224 TensorFlow Lite model. No independent licensed semantic benchmark was available for this upgrade, so replacing it or reducing thresholds would not constitute an evidence-backed accuracy improvement. V2 improves observability, feedback independence and ML tooling; it does not claim a newly trained or more accurate classifier.

The shipped artifact remains 17,355,548 bytes, SHA256 `380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40`, FLOAT32 `[1,224,224,3]` RGB/255 with nearest resize and `[1,5]` output in `drawings,hentai,neutral,porn,sexy` order. `hentai + porn` is a product score, not a calibrated harm probability. Suggestive output is separate. MIT notice and upstream/distributor provenance remain documented in [MODEL_SELECTION.md](MODEL_SELECTION.md). Training-image rights, adult/consent assurances and original conversion parity remain unresolved. No data scraper or private screenshot collector was run.

## Alternatives researched

| Candidate | Evidence checked | Decision and missing evidence |
|---|---|---|
| Current GantMan MobileNetV2 TFLite | Existing pinned bytes, original model repository, executed native contract tests | Preserve as experimental baseline; actual mobile semantic accuracy unavailable |
| NSFWJS MobileNetV2 / Mid | [Upstream](https://github.com/infinitered/nsfwjs) offers multiple TFJS variants under MIT; five-category image classifier | Useful smaller candidate family, but not a verified drop-in Android export. Reproduce conversion, pin license/weights, establish numerical equivalence, then compare on the same held-out data |
| Falconsai NSFW detector | [Model card](https://huggingface.co/Falconsai/nsfw_image_detection) identifies a ViT/PyTorch model and Apache-2.0 license | Different binary semantics and framework; mobile export, tensor adapter, latency/memory and independent image-rights review required. Repository popularity is not benchmark evidence |
| MobileNetV3Small supervised candidate | [Keras model contract](https://keras.io/api/applications/mobilenet/mobilenet_models/) provides an explicit preprocessing option and lightweight backbone | Repaired local workshop supports controlled adaptation. No approved checkpoint/dataset was supplied and no fine-tuning run occurred |
| EfficientNet / OpenNSFW families | Architecture alone does not specify weights rights, screenshot-domain quality or an Android artifact | Research candidates only; no evidence supporting a switch in this release |

Research was checked on 2026-10-22. Upstream examples and performance tables are not Veil/TECNO measurements. No numerical cross-model ranking is reported.

## Policy and regions

The existing operating point remains experimental. There is no validated Balanced/Strict accuracy distinction, so V2 does not invent a second production sensitivity mode. The benchmark can compare predeclared thresholds, whole-frame processing and whole-frame plus center-crop maximum. A crop may recover small media while increasing false positives and cost; it remains an offline experiment until measured. A binary candidate cannot supply calibrated safe/suggestive/explicit probabilities. The currently shipped five-output adapter and hash gate must be deliberately migrated and retested before a scalar candidate can replace it.
