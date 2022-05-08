import argparse
import math

if __name__ == '__main__':
    # Create argument parser
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-n',
        dest='n',
        type=int,
        required=True,
        help='number of nodes')

    # Parse and validate arguments
    args = parser.parse_args()
    n = args.n
    sqrt_n = math.sqrt(n)
    assert sqrt_n == int(sqrt_n), f'{n} is not a perfect square'
    sqrt_n = int(sqrt_n)

    # List all rows
    rows = []
    for row in range(sqrt_n):
        rows.append(set(row * sqrt_n + column + 1 for column in range(sqrt_n)))

    # List all columns
    columns = []
    for column in range(sqrt_n):
        columns.append(set(row * sqrt_n + column + 1 for row in range(sqrt_n)))

    # Print XML tag
    print('<Sanders ', end='')
    for node in range(n):
        row = node // sqrt_n
        column = node % sqrt_n
        district = sorted(rows[row] | columns[column])
        print('s{}="'.format(node + 1), end='')
        print(*district, sep=',', end='" ')
    print('/>')
