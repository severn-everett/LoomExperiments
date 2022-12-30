import argparse
import csv

import matplotlib.pyplot as plt
import numpy as np


def read_file(file_name):
    with open(file_name) as csv_file:
        line_count = 1
        line_reader = csv.reader(csv_file)
        categories = []
        scores = []
        errors = []
        for row in line_reader:
            if line_count > 1:
                categories.append(f"{row[0]} X {row[1]}")
                scores.append(float(row[2]))
                errors.append(float(row[3]))
            line_count += 1

        scores.reverse()
        errors.reverse()
        categories.reverse()
        return scores, errors, categories


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Create a bar chart from results.')
    parser.add_argument('-x', '--x_axis', type=str, help='the x-axis label')
    parser.add_argument('-y', '--y_axis', type=str, help='the y-axis label')
    parser.add_argument('-d', '--directory', type=str, help='the directory containing the scores')
    args = parser.parse_args()

    coroutine_scores, coroutine_errors, category_labels = read_file(f"{args.directory}/coroutines.csv")
    hybrid_scores, hybrid_errors, _ = read_file(f"{args.directory}/hybrid.csv")
    loom_scores, loom_errors, _ = read_file(f"{args.directory}/loom.csv")

    y = np.arange(len(category_labels))

    fig, ax = plt.subplots()

    ax.set_xlabel(args.x_axis)
    ax.set_ylabel(args.y_axis)
    ax.set_yticks(y, category_labels)

    width = 0.2
    capsize = 3

    coroutines_plot = ax.barh(
        y + 0.3, coroutine_scores, width, label='Coroutines', xerr=coroutine_errors, capsize=capsize
    )
    hybrid_plot = ax.barh(
        y, hybrid_scores, width, label='Hybrid', xerr=hybrid_errors, capsize=capsize
    )
    loom_plot = ax.barh(
        y - 0.3, loom_scores, width, label='Loom', xerr=loom_errors, capsize=capsize
    )

    ax.legend()
    fig.tight_layout()

    plt.gca().get_xaxis().get_major_formatter().set_scientific(False)
    plt.show()
