from graph import *


def discrete(coloring: tuple):
    for count in coloring:
        if count[1] != 1:
            return False

    return True


def get_possible_isomorphisms(graphs: List[Graph]):
    maybe_iso = {}
    for i, G in enumerate(graphs):
        coloring = refine(G)
        maybe_iso[coloring] = maybe_iso.get(coloring, []) + [i]

    return maybe_iso.values()


def refine(g: Graph):
    color = [0] * len(g.vertices)
    new_color = [0] * len(g.vertices)
    for v in g.vertices:
        color[v.label] = v.degree

    while True:
        partitions = {}
        c = 0
        for v in g.vertices:
            nbh = {}
            for n in v.neighbors:
                nbh[color[n.label]] = nbh.get(color[n.label], 0) + 1

            key = color[v.label], tuple(sorted(nbh.items()))
            if key not in partitions:
                partitions[key] = []
            partitions[key].append(v.label)

        for k, vs in sorted(partitions.items()):
            for label in vs:
                new_color[label] = c
            c += 1

        if color == new_color:
            for v in g.vertices:
                v.colornum = color[v.label]
            for k, vs in partitions.items():
                partitions[k] = len(vs)
            return tuple(sorted(partitions.items()))

        color = copy.deepcopy(new_color)
