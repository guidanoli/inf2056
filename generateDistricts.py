import argparse
import math

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-n',
        dest='n',
        type=int,
        required=True,
        help='number of nodes')
    args = parser.parse_args()
    sqrt_n = math.sqrt(args.n)
    assert sqrt_n == int(sqrt_n), f'{args.n} is not a perfect square'
    sqrt_n = int(sqrt_n)
    districts = []
    for node in range(args.n):
        district = set()
        i = node // sqrt_n
        j = node % sqrt_n
        for alt_i in range(sqrt_n):
            neighbour = alt_i * sqrt_n + j + 1
            district.add(str(neighbour))
        for alt_j in range(sqrt_n):
            neighbour = i * sqrt_n + alt_j + 1
            district.add(str(neighbour))
        districts.append(
            's{}="{}"'.format(
                node + 1,
                ','.join(
                    sorted(district))))
    print('<Sanders {} />'.format(' '.join(districts)))
