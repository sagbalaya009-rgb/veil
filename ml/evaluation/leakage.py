"""Local duplicate checks; hashes never replace rights or independent labels."""
from __future__ import annotations

import hashlib
from PIL import Image, ImageOps


def image_fingerprint(path):
    with Image.open(path) as source:
        image = ImageOps.exif_transpose(source).convert("RGB")
        decoded = hashlib.sha256(str(image.size).encode() + image.tobytes()).hexdigest()
        gray = image.convert("L").resize((9, 8), Image.Resampling.LANCZOS)
        pixels = list(gray.tobytes())
        dhash = sum((pixels[y*9+x] > pixels[y*9+x+1]) << (y*8+x) for y in range(8) for x in range(8))
        # Flat/low-information images collide trivially; decoded equality still applies.
        textured = max(pixels) - min(pixels) >= 16
    return decoded, dhash, textured


def reject_split_leakage(records, max_distance=3):
    """records=(path, split, label). Reject exact copies and near copies across splits.

    dHash is a conservative candidate screen; it can miss crops or mark unrelated
    images. Refusal requires independent review/removal, never an automatic relabel.
    """
    seen_bytes, seen_decoded, previous = {}, {}, []
    for path, split, label in records:
        content = hashlib.sha256(path.read_bytes()).hexdigest()
        decoded, visual, textured = image_fingerprint(path)
        for seen, key in ((seen_bytes, content), (seen_decoded, decoded)):
            if key in seen:
                prior_split, prior_label = seen[key]
                if prior_split != split or prior_label != label:
                    raise ValueError("Duplicate content crosses splits or has contradictory labels.")
                raise ValueError("Duplicate content would inflate sample counts.")
            seen[key] = split, label
        for prior_split, prior_visual, prior_textured in previous:
            if split != prior_split and textured and prior_textured and (visual ^ prior_visual).bit_count() <= max_distance:
                raise ValueError("Potential near-duplicate crosses splits; independent review required.")
        previous.append((split, visual, textured))
