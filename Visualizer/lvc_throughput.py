import argparse
import csv

from common import present, ScoreSet


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
        return ScoreSet(scores, errors), categories


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Create a bar chart from results.')
    parser.add_argument('-x', '--x_axis', type=str, help='the x-axis label')
    parser.add_argument('-y', '--y_axis', type=str, help='the y-axis label')
    parser.add_argument('-d', '--directory', type=str, help='the directory containing the scores')
    args = parser.parse_args()

    coroutine_set, category_labels = read_file(f"{args.directory}/coroutines.csv")
    hybrid_set, _ = read_file(f"{args.directory}/hybrid.csv")
    loom_set, _ = read_file(f"{args.directory}/loom.csv")

    present(args, coroutine_set, hybrid_set, loom_set, category_labels)
