from collections import deque

from graph import *

# def refine(g: Graph, initial_coloring: List):
#     color = copy.deepcopy(initial_coloring)
#     new_color = copy.deepcopy(color)
#
#
#     while True:
#         partitions = {}
#         c = 0
#         for v in g.vertices:
#             nbh = {}
#             for n in v.neighbours:
#                 nbh[color[n.label]] = nbh.get(color[n.label], 0) + 1
#
#             key = color[v.label], tuple(sorted(nbh.items()))
#             if key not in partitions:
#                 partitions[key] = []
#             partitions[key].append(v.label)
#
#         for k, vs in partitions.items():
#             for label in vs:
#                 new_color[label] = c
#             c += 1
#
#         if color == new_color:
#             ans = {}
#             for i in range(len(color)):
#                 if color[i] not in ans:
#                     ans[color[i]] = []
#                 ans[color[i]].append(i)
#             return ans
#
#         color = copy.deepcopy(new_color)

def fast_refine(g: Graph, initial_coloring=None):
    if initial_coloring is None:
        color = [0] * len(g.vertices)
    else:
        color = copy.deepcopy(initial_coloring)
    lowest_free_color = max(color) + 1 if len(color) else 1
    q = deque(range(lowest_free_color))
    inq = [True]*lowest_free_color
    states_per_color: List[List] = [[] for i in range(lowest_free_color)]

    for i, c in enumerate(color):
        states_per_color[c].append(i)

    def refine(col: int):
        nonlocal color, lowest_free_color, q, inq, states_per_color

        def add_color(states_labels):
            nonlocal color, lowest_free_color, q, inq, states_per_color
            states_per_color.append(states_labels)
            for lab in states_labels:
                color[lab] = lowest_free_color
            q.append(lowest_free_color)
            inq.append(True)
            lowest_free_color += 1

        connected_to_col = {}
        color_i_states = {}
        transitions_per_color = {}
        # code divided into seperate functions for profiling purposes
        def first_part():
            nonlocal states_per_color, g
            for i in states_per_color[col]:
                for n_label in g.vertices_reference[i].neighbours_labels:
                    connected_to_col[n_label] = connected_to_col.get(n_label, 0) + 1

        def second_part():
            for lab, connected_count in connected_to_col.items():
                key = color[lab], connected_count
                if key not in color_i_states:
                    color_i_states[key] = []
                color_i_states[key].append(lab)

        def third_part():
            for (c, i), states_labels in color_i_states.items():
                if c not in transitions_per_color:
                    transitions_per_color[c] = []
                transitions_per_color[c].append((i, states_labels))

        def fourth_part():
            for c, transitions_from_c in transitions_per_color.items():
                remaining = set(states_per_color[c])
                max_size = 0

                for i, states_labels in transitions_from_c:
                    if len(states_labels) > max_size:
                        max_size = max(max_size, len(states_labels))
                        max_set = i, states_labels
                    remaining -= set(states_labels)

                if len(remaining) == 0 and len(transitions_from_c) == 1:
                    continue

                if inq[c]:
                    if len(remaining):
                        for i, states_labels in transitions_from_c:
                            add_color(states_labels)
                        states_per_color[c] = list(remaining)
                    else:
                        states_per_color[c] = transitions_from_c[0][1]
                        for i in range(1, len(transitions_from_c)):
                            add_color(transitions_from_c[i][1])
                else:
                    for i, states_labels in transitions_from_c:
                        if max_set[0] == i:
                            continue
                        add_color(states_labels)
                    if len(remaining):
                        add_color(list(remaining))
                    states_per_color[c] = max_set[1]
        first_part()
        second_part()
        third_part()
        fourth_part()

    while len(q) != 0:
        top = q.popleft()
        inq[top] = False
        refine(top)

    ans = {}
    for i,v in enumerate(states_per_color):
        ans[i] = v
    return ans



if __name__ == "__main__":
    from test import read
    L = read('SampleGraphSetBranching/', 'cubes3.grl')
    for l in L:
        print(fast_refine(l,[ 1, 0, 0, 0, 0, 0, 0, 0]))


