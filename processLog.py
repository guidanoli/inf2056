import argparse
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection

STATES = "states"
LAST_TS = "last_timestamp"
RELINQUISH_COUNT = "relinquish_count"
PLOT_GRAPH = "plot_graph"
COLOR_MAP = {
    "NotInCS": "k",  # black
    "Waiting": "y",  # yellow
    "InCS": "g",     # green
}


def new_context():
    ctx = {}
    ctx[STATES] = {}
    ctx[LAST_TS] = None
    ctx[RELINQUISH_COUNT] = 0
    ctx[PLOT_GRAPH] = False
    return ctx


def new_v(ts1, ts2, node_id):
    return [(ts1, node_id-.4),
            (ts1, node_id+.4),
            (ts2, node_id+.4),
            (ts2, node_id-.4)]


def plot_graph(ctx):
    verts = []
    colors = []
    last_state_update = {}
    for node_id, node_states in ctx[STATES].items():
        for ts, node_state in node_states.items():
            if node_id in last_state_update:
                last_ts, last_state = last_state_update[node_id]
                v = new_v(last_ts, ts, node_id)
                verts.append(v)
                colors.append(COLOR_MAP[last_state])
            last_state_update[node_id] = (ts, node_state)

    for node_id in ctx[STATES]:
        if node_id in last_state_update:
            last_ts, last_state = last_state_update[node_id]
            v = new_v(last_ts, ctx[LAST_TS], node_id)
            verts.append(v)
            colors.append(COLOR_MAP[last_state])

    bars = PolyCollection(verts, facecolors=colors)
    fig, ax = plt.subplots()
    ax.add_collection(bars)
    ax.autoscale()
    ax.set_yticks(sorted(ctx[STATES]), minor=True)
    plt.title('Timeline of Sanders87 node states')
    plt.xlabel('Time')
    plt.ylabel('Nodes')
    markers = [
        plt.Line2D(
            [0, 0],
            [0, 0],
            color=color, marker='o', linestyle='')
        for color in COLOR_MAP.values()]
    plt.legend(markers, COLOR_MAP.keys(), loc='upper left')
    plt.grid(axis='y', which='both')
    plt.show()


def should_process_event(args, ts):
    return ((args.min_ts == -1 or args.min_ts <= ts) and
            (args.max_ts == -1 or ts <= args.max_ts))


def handle_node_state_change_event(args, ctx, ts, node_id, node_state):
    ts = int(ts)
    node_id = int(node_id)

    if not should_process_event(args, ts):
        return

    ctx[PLOT_GRAPH] = True

    if ctx[LAST_TS] is None or ctx[LAST_TS] < ts:
        ctx[LAST_TS] = ts

    if node_id not in ctx[STATES]:
        ctx[STATES][node_id] = {}

    ctx[STATES][node_id][ts] = node_state


def handle_relinquish_event(args, ctx, ts, node_id):
    ts = int(ts)

    if not should_process_event(args, ts):
        return

    ctx[RELINQUISH_COUNT] += 1


if __name__ == '__main__':
    # Create argument parser
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--log',
        dest='log',
        required=True,
        help='log file')
    parser.add_argument(
        '--after',
        dest='min_ts',
        type=int,
        default=-1,
        help='only process events after this time stamp (default: -1)')
    parser.add_argument(
        '--before',
        dest='max_ts',
        type=int,
        default=-1,
        help='only process events before this time stamp (default: -1)')

    # Register event handlers
    event_handlers = {
        'NODE_STATE_CHANGE': handle_node_state_change_event,
        'RELINQUISH': handle_relinquish_event,
    }

    # Parse arguments
    args = parser.parse_args()

    # Create context
    ctx = new_context()

    # Parse log file
    with open(args.log) as fp:
        for lineno, line in enumerate(fp, 1):
            event, *event_args = line.strip().split(';')
            event_handler = event_handlers[event]
            event_handler(args, ctx, *event_args)

    # Print number or relinquishes
    print('# of RELINQUISHes = {}'.format(ctx[RELINQUISH_COUNT]))

    # Plot graph
    if ctx[PLOT_GRAPH]:
        plot_graph(ctx)
