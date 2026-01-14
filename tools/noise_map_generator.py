#!/usr/bin/env python3
"""
Advanced noise map generator with multiple algorithms.

Supports: white noise, Perlin, fractal (fBm), and Worley/cellular noise.
Useful for terrain generation, texture synthesis, and procedural art.
"""

import argparse
import math
import random
from dataclasses import dataclass
from typing import Callable, Dict, Optional, Tuple

import numpy as np
from PIL import Image

# ------
# Utility functions
# ------

def clamp01(x: np.ndarray) -> np.ndarray:
    """Clamp array values to [0, 1] range."""
    return np.clip(x, 0.0, 1.0)

def to_uint8(img: np.ndarray) -> np.ndarray:
    """Convert float array [0, 1] to uint8 [0, 255]."""
    return (clamp01(img) * 255).astype(np.uint8)

# ------
# White noise
# ------

def generate_white_noise(width: int, height: int, seed: Optional[int] = None) -> np.ndarray:
    """
    Generate white noise (uniform random values).

    Args:
        width: Image width
        height: Image height
        seed: Random seed

    Returns:
        Noise array in range [0, 1]
    """
    rng = np.random.default_rng(seed)
    return rng.random((height, width), dtype=np.float32)

# ------
# Perlin noise (2D)
# ------

# Gradients for Perlin
_GRADIENTS_2D = np.array([
    [1, 0], [-1, 0], [0, 1], [0, -1],
    [1, 1], [-1, 1], [1, -1], [-1, -1]
], dtype=np.float32)


def _fade(t: np.ndarray) -> np.ndarray:
    """Smooth fade curve for interpolation (6t^5 - 15t^4 + 10t^3)."""
    return t * t * t * (t * (t * 6 - 15) + 10)


def _lerp(a: np.ndarray, b: np.ndarray, t: np.ndarray) -> np.ndarray:
    """Linear interpolation."""
    return a + t * (b - a)

class Perlin2D:
    """2D Perlin noise generator."""

    def __init__(self, seed: Optional[int] = None):
        if seed is None:
            seed = random.randint(0, 2**31 - 1)
        rng = np.random.default_rng(seed)
        p = np.arange(256, dtype=int)
        rng.shuffle(p)
        self.perm = np.concatenate([p, p])

    def _grad(self, hash_vals: np.ndarray, x: np.ndarray, y: np.ndarray) -> np.ndarray:
        """Compute dot product with gradient."""
        g = _GRADIENTS_2D[hash_vals % len(_GRADIENTS_2D)]
        return g[..., 0] * x + g[..., 1] * y

    def noise(self, width: int, height: int, scale: float = 32.0) -> np.ndarray:
        """
        Generate Perlin noise.

        Args:
            width: Image width
            height: Image height
            scale: Feature scale (higher = larger features)

        Returns:
            Noise array in range [0, 1]

        Raises:
            ValueError: If scale <= 0
        """
        if scale <= 0:
            raise ValueError("scale must be > 0")

        # Create grid of coordinates
        xs = np.linspace(0, width / scale, width, endpoint=False, dtype=np.float32)
        ys = np.linspace(0, height / scale, height, endpoint=False, dtype=np.float32)
        x, y = np.meshgrid(xs, ys)

        # Integer lattice points
        x0 = np.floor(x).astype(int)
        x1 = x0 + 1
        y0 = np.floor(y).astype(int)
        y1 = y0 + 1

        # Relative position in cell
        xf = x - x0
        yf = y - y0

        # Wrap to 0–255
        xi0 = x0 & 255
        xi1 = x1 & 255
        yi0 = y0 & 255
        yi1 = y1 & 255

        # Hash for each corner
        aa = self.perm[self.perm[xi0] + yi0]
        ab = self.perm[self.perm[xi0] + yi1]
        ba = self.perm[self.perm[xi1] + yi0]
        bb = self.perm[self.perm[xi1] + yi1]

        # Gradients dot products
        x_rel = xf
        y_rel = yf
        dot_aa = self._grad(aa, x_rel, y_rel)
        dot_ba = self._grad(ba, x_rel - 1, y_rel)
        dot_ab = self._grad(ab, x_rel, y_rel - 1)
        dot_bb = self._grad(bb, x_rel - 1, y_rel - 1)

        # Interpolation
        u = _fade(xf)
        v = _fade(yf)

        x1_interp = _lerp(dot_aa, dot_ba, u)
        x2_interp = _lerp(dot_ab, dot_bb, u)
        value = _lerp(x1_interp, x2_interp, v)

        # Normalize from [-N, N] to [0,1]. Empirical factor ~0.707.
        return ((value * 0.5 + 0.5).astype(np.float32))

# ------
# Fractal noise (fBm using Perlin)
# ------

def generate_fractal_noise_perlin(
    width: int,
    height: int,
    octaves: int = 4,
    persistence: float = 0.5,
    lacunarity: float = 2.0,
    base_scale: float = 32.0,
    seed: Optional[int] = None,
) -> np.ndarray:
    """
    Fractal Brownian Motion using multiple layers of Perlin noise.

    Args:
        width: Image width
        height: Image height
        octaves: Number of layers to combine
        persistence: Amplitude multiplier per octave
        lacunarity: Frequency multiplier per octave
        base_scale: Starting feature scale
        seed: Random seed

    Returns:
        Noise array in range [0, 1]
    """
    perlin = Perlin2D(seed=seed)
    total = np.zeros((height, width), dtype=np.float32)
    amplitude = 1.0
    frequency = 1.0
    max_amplitude = 0.0

    for _ in range(octaves):
        scale = base_scale / frequency
        layer = perlin.noise(width, height, scale=scale)
        total += layer * amplitude
        max_amplitude += amplitude
        amplitude *= persistence
        frequency *= lacunarity

    return clamp01(total / max_amplitude if max_amplitude > 0 else total)

# ------
# Worley / Cellular noise (2D)
# ------

def generate_worley_noise(
    width: int,
    height: int,
    num_points: int = 32,
    seed: Optional[int] = None,
    metric: str = "euclidean",
) -> np.ndarray:
    """
    Worley/cellular noise based on distance to random feature points.

    Args:
        width: Image width
        height: Image height
        num_points: Number of random feature points
        seed: Random seed
        metric: 'euclidean' or 'manhattan' distance metric

    Returns:
        Noise array in range [0, 1] (normalized distances)

    Raises:
        ValueError: If metric not recognized
    """
    if metric not in ("euclidean", "manhattan"):
        raise ValueError(f"Unknown metric: {metric}. Use 'euclidean' or 'manhattan'")

    rng = np.random.default_rng(seed)

    # Random feature points in normalized space [0,1] x [0,1]
    points = rng.random((num_points, 2), dtype=np.float32)

    # Grid of sample positions in [0,1]
    xs = np.linspace(0, 1, width, endpoint=False, dtype=np.float32)
    ys = np.linspace(0, 1, height, endpoint=False, dtype=np.float32)
    x, y = np.meshgrid(xs, ys)
    grid = np.stack([x, y], axis=-1)  # (H, W, 2)

    # Compute distance to all points
    diff = grid[..., None, :] - points[None, None, :, :]  # (H, W, P, 2)

    if metric == "manhattan":
        dist = np.abs(diff).sum(axis=-1)  # (H, W, P)
    else:
        # Euclidean
        dist = np.sqrt(np.sum(diff**2, axis=-1))

    # Take distance to nearest point
    nearest = dist.min(axis=-1)

    # Normalize distances to [0,1]
    # Max possible distance in [0,1]^2 is sqrt(2)
    nearest /= math.sqrt(2.0)
    return clamp01(nearest.astype(np.float32))

# ------
# Dispatcher / configuration
# ------

@dataclass
class NoiseConfig:
    """Configuration for noise generation."""
    width: int
    height: int
    noise_type: str
    seed: Optional[int] = None
    scale: float = 32.0
    octaves: int = 4
    persistence: float = 0.5
    lacunarity: float = 2.0
    num_points: int = 32
    worley_metric: str = "euclidean"


def generate_noise(config: NoiseConfig) -> np.ndarray:
    """
    Generate noise based on configuration.

    Args:
        config: NoiseConfig with parameters

    Returns:
        Noise array in range [0, 1]

    Raises:
        ValueError: If noise_type not recognized
    """
    noise_type = config.noise_type.lower()

    if noise_type == "white":
        return generate_white_noise(config.width, config.height, config.seed)

    elif noise_type == "perlin":
        perlin = Perlin2D(seed=config.seed)
        return perlin.noise(config.width, config.height, scale=config.scale)

    elif noise_type in ("fractal", "fbm", "fractal_perlin", "fbm_perlin"):
        return generate_fractal_noise_perlin(
            config.width,
            config.height,
            octaves=config.octaves,
            persistence=config.persistence,
            lacunarity=config.lacunarity,
            base_scale=config.scale,
            seed=config.seed,
        )

    elif noise_type in ("worley", "cellular"):
        return generate_worley_noise(
            config.width,
            config.height,
            num_points=config.num_points,
            seed=config.seed,
            metric=config.worley_metric,
        )

    else:
        raise ValueError(
            f"Unknown noise type: {config.noise_type}. "
            f"Valid: white, perlin, fractal, fbm, worley, cellular"
        )

# ------
# Saving
# ------

def save_noise_as_png(noise: np.ndarray, path: str) -> None:
    """Save noise array as grayscale PNG image."""
    img_uint8 = to_uint8(noise)
    img = Image.fromarray(img_uint8, mode="L")
    img.save(path)


# ------
# CLI
# ------

def build_arg_parser() -> argparse.ArgumentParser:
    """Build command-line argument parser."""
    parser = argparse.ArgumentParser(
        description="Noise map generator",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="Example: python noise_map_generator.py --type perlin --scale 32 output.png",
    )
    parser.add_argument(
        "--width",
        type=int,
        default=256,
        help="Image width in pixels (default: 256)",
    )
    parser.add_argument(
        "--height",
        type=int,
        default=256,
        help="Image height in pixels (default: 256)",
    )
    parser.add_argument(
        "--type",
        type=str,
        default="perlin",
        choices=["white", "perlin", "fractal", "fbm", "fractal_perlin", "fbm_perlin", "worley", "cellular"],
        help="Type of noise to generate (default: perlin)",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Random seed for reproducibility",
    )

    # Perlin / fractal parameters
    parser.add_argument(
        "--scale",
        type=float,
        default=32.0,
        help="Base scale for Perlin/fractal (higher = larger features) (default: 32.0)",
    )
    parser.add_argument(
        "--octaves",
        type=int,
        default=4,
        help="Number of octaves for fractal noise (default: 4)",
    )
    parser.add_argument(
        "--persistence",
        type=float,
        default=0.5,
        help="Amplitude multiplier per octave (default: 0.5)",
    )
    parser.add_argument(
        "--lacunarity",
        type=float,
        default=2.0,
        help="Frequency multiplier per octave (default: 2.0)",
    )

    # Worley parameters
    parser.add_argument(
        "--points",
        type=int,
        default=32,
        help="Number of feature points for Worley noise (default: 32)",
    )
    parser.add_argument(
        "--metric",
        type=str,
        default="euclidean",
        choices=["euclidean", "manhattan"],
        help="Distance metric for Worley noise (default: euclidean)",
    )

    parser.add_argument(
        "output",
        type=str,
        help="Output PNG file path",
    )
    return parser


def main() -> None:
    """Generate noise map from CLI arguments."""
    parser = build_arg_parser()
    args = parser.parse_args()

    try:
        config = NoiseConfig(
            width=args.width,
            height=args.height,
            noise_type=args.type,
            seed=args.seed,
            scale=args.scale,
            octaves=args.octaves,
            persistence=args.persistence,
            lacunarity=args.lacunarity,
            num_points=args.points,
            worley_metric=args.metric,
        )

        noise = generate_noise(config)
        save_noise_as_png(noise, args.output)
        print(f"✓ Saved {config.noise_type} noise ({config.width}x{config.height}) to {args.output}")
    except ValueError as e:
        print(f"✗ Error: {e}")
        exit(1)
    except Exception as e:
        print(f"✗ Unexpected error: {e}")
        exit(1)


if __name__ == "__main__":
    main()
