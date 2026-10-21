# Model selection — 2026-10-21

## Selected experimental artifact

Veil bundles a genuine GantMan `nsfw_model` MobileNetV2 (width 1.40, 224px) TensorFlow Lite classifier. It is a practical CPU/offline baseline, not an established best-performing model for screen captures. There is no remote inference or runtime model download.

- Upstream: https://github.com/GantMan/nsfw_model
- TFLite distributor: https://github.com/getknit/knit/blob/main/app/src/main/assets/moderation/README.md
- Downloaded bytes: https://raw.githubusercontent.com/getknit/knit/2c5a0c440acfcae59cae1ccd2557581b8d1a57a4/app/src/main/assets/moderation/nsfw.tflite
- Bundled path: `app/src/main/assets/models/veil_explicit_v1.tflite`
- Byte length: **17,355,548** (16.55 MiB).
- SHA-256: `380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40`.
- Integration ID: `gantman-mobilenet-v2-140-224-tflite-v1`. This is Veil's integration ID, not a claimed upstream release tag. The distributor does not establish which release produced the converted bytes. The byte hash pins this exact artifact.
- Runtime: Android `org.tensorflow:tensorflow-lite:2.16.1`, two CPU threads, no GPU delegate.

## License and provenance

The upstream [license](https://github.com/GantMan/nsfw_model/blob/master/LICENSE.md) grants MIT rights subject to retained notice, and starts with a third-party-material qualifier. The distributor explicitly identifies this model as MIT and includes its notice. The upstream author's [weights licensing response](https://github.com/GantMan/nsfw_model/issues/173) describes the weights as open source and points to MIT NSFWJS models. The complete upstream notice is included in the model asset directory. Commercial use is permitted under the stated MIT terms; no new agreement was accepted.

This evidence does **not** establish individual training-image rights, adult-only verification, consent, complete dataset composition, or a reproducible conversion chain for the distributor's TFLite export. Upstream credits `nsfw_data_scraper` and describes approximately 60GB of training material. Treat these as unresolved provenance limitations; do not claim an ethically audited dataset or production qualification. No training/evaluation images were downloaded for this integration.

## Actual contract

One input `[1,224,224,3]` FLOAT32 RGB. Resize the whole image to 224x224 using nearest interpolation; divide each channel by 255. This follows upstream [predict.py](https://github.com/GantMan/nsfw_model/blob/master/nsfw_detector/predict.py), which uses Keras `load_img` default nearest interpolation and `image /= 255`. No crop, ImageNet mean subtraction, BGR reordering, or `[-1,1]` normalization.

One output `[1,5]` FLOAT32 softmax in this exact order:

| Index | Upstream category | Veil policy |
| --- | --- | --- |
| 0 | drawings | Not included in explicit score |
| 1 | hentai | Included in explicit score |
| 2 | neutral | Not included in explicit score |
| 3 | porn | Included in explicit score |
| 4 | sexy | Separate suggestive score, does not trigger explicit policy by itself |

`explicitConfidence = hentai + porn` is an explicit product policy grouping, **not** a validated probability of harm, medical judgment, or calibrated three-class classifier. The original five labels remain documented. A 0.90 detection threshold and temporal confirmation are conservative provisional policy choices, not measured operating points.

The adapter rejects non-finite/out-of-range outputs or invalid softmax totals, serializes interpreter use/closure, reuses bounded input storage, clears temporary pixel/tensor buffers, and never recycles the source bitmap. The initializer verifies both the manifest hash and the hash pinned in code, exact tensor dimensions, FLOAT32 dtype, input/output counts, and class order. Missing or invalid assets report unavailable protection. Rollback is to disable visual classification; there was no previously bundled model.

## Alternatives examined

| Candidate | Availability / license evidence | Decision |
| --- | --- | --- |
| GantMan MobileNetV2 / NSFWJS | Public Keras/TFJS releases; MIT source and distributor model notice; actual mobile artifact available | Select distributor TFLite export, pin bytes, disclose conversion/provenance limits |
| NSFWJS MobileNetV2 default / mid | [MIT project](https://github.com/infinitered/nsfwjs), bundled JS graphs | Smaller deployment candidates; require independently reproduced TFLite conversion and parity checks. No claimed latency or superiority |
| Falconsai/nsfw_image_detection | [Apache-2.0 model card](https://huggingface.co/Falconsai/nsfw_image_detection), ViT PyTorch/Safetensors | No directly verified compatible mobile artifact in this session; substantially heavier architecture. Not selected |
| Yahoo OpenNSFW derivatives | [BSD-3-Clause upstream](https://github.com/yahoo/open_nsfw); binary visual NSFW model | Different preprocessing, older architecture, and conversion chain; no reason established to prefer over available MobileNet |

No cross-model accuracy benchmark was run. Quantization, GPU latency, battery, thermal response, demographic bias, recall, precision, and false-positive rates remain unmeasured for this Veil build. A model being executable is distinct from it being accurate or suitable for production filtering.

