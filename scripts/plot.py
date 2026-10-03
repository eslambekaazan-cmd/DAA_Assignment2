"""Draws the charts for REPORT.md from results/results.csv.
Usage: python3 scripts/plot.py [results/results.csv] [results/plots]
"""
import csv
import os
import sys
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

src = sys.argv[1] if len(sys.argv) > 1 else "results/results.csv"
out = sys.argv[2] if len(sys.argv) > 2 else "results/plots"
os.makedirs(out, exist_ok=True)

TITLES = {
    "W1": "W1 Random access: 10 000 get(i)",
    "W2": "W2 Search: 1 000 contains(x)",
    "W3": "W3 Insert & remove: 1 000 + 1 000 operations",
    "W4": "W4 Priority processing: n inserts + n extractMin",
    "W5": "W5 (bonus) buildHeap vs n inserts",
}

# data[workload][variant][structure] = list of (n, time_ms, steps, moves, comparisons)
data = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))
with open(src, newline="") as f:
    for r in csv.DictReader(f):
        data[r["workload"]][r["variant"]][r["structure"]].append((
            int(r["n"]), float(r["time_ms"]), int(r["steps"]),
            int(r["moves"]), int(r["comparisons"])))

for w, variants in sorted(data.items()):
    vs = sorted(variants)
    # chart 1: time vs n
    fig, axes = plt.subplots(1, len(vs), figsize=(6 * len(vs), 4.5), squeeze=False)
    for ax, v in zip(axes[0], vs):
        for s, rows in sorted(variants[v].items()):
            rows.sort()
            ax.plot([r[0] for r in rows], [r[1] for r in rows], marker="o", label=s)
        ax.set_xscale("log"); ax.set_yscale("log")
        ax.set_xlabel("n (number of elements)"); ax.set_ylabel("median time (ms)")
        ax.set_title(TITLES.get(w, w) + ("" if v == "-" else f" [{v}]"), fontsize=9)
        ax.grid(True, which="both", alpha=0.3); ax.legend()
    fig.tight_layout(); fig.savefig(os.path.join(out, f"{w}_time.png"), dpi=130); plt.close(fig)

    # chart 2: steps / moves / comparisons vs n (one row per variant)
    fig, axes = plt.subplots(len(vs), 3, figsize=(15, 4 * len(vs)), squeeze=False)
    for i, v in enumerate(vs):
        for j, (name, col) in enumerate((("steps", 2), ("moves", 3), ("comparisons", 4))):
            ax = axes[i][j]
            for s, rows in sorted(variants[v].items()):
                rows.sort()
                ax.plot([r[0] for r in rows], [r[col] for r in rows], marker="o", label=s)
            ax.set_xscale("log"); ax.set_yscale("symlog", linthresh=1)
            ax.set_xlabel("n (number of elements)"); ax.set_ylabel(f"{name} (count)")
            ax.set_title(f"{w} {name}" + ("" if v == "-" else f" [{v}]"), fontsize=9)
            ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=8)
    fig.tight_layout(); fig.savefig(os.path.join(out, f"{w}_ops.png"), dpi=130); plt.close(fig)
print("plots written to", out)
