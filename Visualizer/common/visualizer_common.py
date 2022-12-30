from dataclasses import dataclass

import matplotlib.pyplot as plt
import numpy as np
from numpy import ndarray


@dataclass
class ScoreSet:
    scores: list[float]
    error_ranges: list[float]


width = 0.2
capsize = 3


def create_hbar(ax, position: ndarray, score_set: ScoreSet, set_name: str):
    set_args = {'label': set_name}
    if score_set.error_ranges is not None:
        set_args['xerr'] = score_set.error_ranges
        set_args['capsize'] = capsize
    ax.barh(position, score_set.scores, width, **set_args)


def present(
        args,
        coroutine_set: ScoreSet,
        hybrid_set: ScoreSet,
        loom_set: ScoreSet,
        category_labels: list[str]
):
    y = np.arange(len(category_labels))

    fig, ax = plt.subplots()

    ax.set_xlabel(args.x_axis)
    ax.set_ylabel(args.y_axis)
    ax.set_yticks(y, category_labels)

    create_hbar(ax, y + 0.3, coroutine_set, 'Coroutines')
    create_hbar(ax, y, hybrid_set, 'Hybrid')
    create_hbar(ax, y - 0.3, loom_set, 'Loom')

    ax.legend()
    fig.tight_layout()

    plt.gca().get_xaxis().get_major_formatter().set_scientific(False)
    plt.show()