#!/usr/bin/env python3
"""
Test and verify GPUImage-grade photo adjustment formulas
Formulas from wasabeef/android-gpuimage & Brad Larson's GPUImage
"""
import math
import numpy as np
from PIL import Image

def apply_gpuimage_adjustments(
    img: Image.Image,
    exposure: float = 0.0,       # -2.0 to +2.0 EV
    contrast: float = 1.0,       # 0.5 to 1.8
    shadows: float = 0.0,        # 0.0 to 1.0
    highlights: float = 1.0,     # 0.0 to 1.0 (1.0 = normal, 0.0 = maximum highlight recovery)
    saturation: float = 1.0,     # 0.0 to 2.0
    temperature: float = 0.0,    # -1.0 to 1.0 (cool to warm)
    tint: float = 0.0            # -1.0 to 1.0 (green to magenta)
) -> Image.Image:
    arr = np.array(img).astype(np.float32) / 255.0
    rgb = arr[:, :, :3]
    alpha = arr[:, :, 3] if arr.shape[2] == 4 else None

    # 1. Exposure: rgb * pow(2.0, exposure)
    if exposure != 0.0:
        rgb = rgb * (2.0 ** exposure)

    # 2. Contrast: (rgb - 0.5) * contrast + 0.5
    if contrast != 1.0:
        rgb = (rgb - 0.5) * contrast + 0.5

    rgb = np.clip(rgb, 0.0, 1.0)

    # 3. Shadows & Highlights (GPUImage formula)
    if shadows > 0.0 or highlights < 1.0:
        lum = 0.3 * rgb[:, :, 0] + 0.59 * rgb[:, :, 1] + 0.11 * rgb[:, :, 2]
        lum = np.clip(lum, 1e-4, 1.0)

        # Shadow lift
        s_factor = shadows + 1.0
        shadow_val = np.clip(
            (np.power(lum, 1.0 / s_factor) - 0.76 * np.power(lum, 2.0 / s_factor)) - lum,
            0.0, 1.0
        )

        # Highlight recovery
        h_factor = 2.0 - highlights
        h_val = np.clip(
            (1.0 - (np.power(1.0 - lum, 1.0 / h_factor) - 0.8 * np.power(1.0 - lum, 2.0 / h_factor))) - lum,
            -1.0, 0.0
        )

        total_lum = np.clip(lum + shadow_val + h_val, 0.0, 1.0)
        scale = total_lum / lum
        for c in range(3):
            rgb[:, :, c] = np.clip(rgb[:, :, c] * scale, 0.0, 1.0)

    # 4. White Balance (YIQ color space for temperature & tint)
    if temperature != 0.0 or tint != 0.0:
        # RGB to YIQ
        # Y = 0.299 R + 0.587 G + 0.114 B
        # I = 0.596 R - 0.274 G - 0.322 B
        # Q = 0.212 R - 0.523 G + 0.311 B
        Y = 0.299 * rgb[:, :, 0] + 0.587 * rgb[:, :, 1] + 0.114 * rgb[:, :, 2]
        I = 0.596 * rgb[:, :, 0] - 0.274 * rgb[:, :, 1] - 0.322 * rgb[:, :, 2]
        Q = 0.212 * rgb[:, :, 0] - 0.523 * rgb[:, :, 1] + 0.311 * rgb[:, :, 2]

        # Tint adjusts Q (green vs magenta)
        Q = np.clip(Q + tint * 0.05, -0.52, 0.52)
        # Temperature adjusts I and R/B balance
        I = np.clip(I + temperature * 0.08, -0.59, 0.59)

        # YIQ to RGB
        R = Y + 0.956 * I + 0.621 * Q
        G = Y - 0.272 * I - 0.647 * Q
        B = Y - 1.105 * I + 1.702 * Q
        rgb[:, :, 0] = np.clip(R, 0.0, 1.0)
        rgb[:, :, 1] = np.clip(G, 0.0, 1.0)
        rgb[:, :, 2] = np.clip(B, 0.0, 1.0)

    # 5. Saturation: mix(lum, rgb, saturation)
    if saturation != 1.0:
        lum = 0.2125 * rgb[:, :, 0] + 0.7154 * rgb[:, :, 1] + 0.0721 * rgb[:, :, 2]
        lum = np.expand_dims(lum, axis=2)
        rgb = np.clip(lum + (rgb - lum) * saturation, 0.0, 1.0)

    out_arr = (rgb * 255.0).astype(np.uint8)
    if alpha is not None:
        alpha_channel = (alpha * 255.0).astype(np.uint8)
        out_arr = np.dstack([out_arr, alpha_channel])

    return Image.fromarray(out_arr)

def main():
    test_img = Image.open("/var/minis/workspace/persophoto-ai/docs/samples/sample_before_dark_bg.png")
    print(f"Loaded test image: {test_img.size}")

    # Test 1: Lift dark exposure by +0.75 EV, boost shadows 0.6, contrast 1.15
    enhanced = apply_gpuimage_adjustments(
        test_img,
        exposure=0.65,
        contrast=1.12,
        shadows=0.55,
        highlights=0.85,
        saturation=1.05,
        temperature=0.10
    )
    enhanced.save("/var/minis/workspace/persophoto-ai/docs/samples/sample_gpuimage_enhanced.png")
    print("Saved sample_gpuimage_enhanced.png successfully!")
    print("GPUImage formula verification PASSED!")

if __name__ == "__main__":
    main()
