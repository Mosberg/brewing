# Guide to Using the Texture Generation Tools

## noise_map_generator.py

### Simple Perlin noise

```bash
python noise_map_generator.py --width 128 --height 128 --type perlin perlin_128.png
```

### Cloudy / organic fractal noise (great for liquids, foam bases)

```bash
python noise_map_generator.py --type fractal --scale 48 --octaves 5 cloudy_beer.png
```

### Coarse Worley for foam bubbles or yeast clusters

```bash
python noise_map_generator.py --type worley --points 24 --metric euclidean foam_cells.png
```

### High-frequency white noise for roughness/micro detail

```bash
python noise_map_generator.py --type white --width 64 --height 64 roughness_micro.png
```

### Reproducible output via seed

```bash
python noise_map_generator.py --type fractal --seed 42 barrel_wood_variation.png
```
