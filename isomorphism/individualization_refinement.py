import threading
from enum import Enum

from graph import *
from fast_color_refinement import fast_refine


def refine(g: Graph, initial_coloring: List):
    color = copy.deepcopy(initial_coloring)
    new_color = copy.deepcopy(color)

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

        for k, vs in partitions.items():
            for label in vs:
                new_color[label] = c
            c += 1

        if color == new_color:
            ans = {}
            for i in range(len(color)):
                if color[i] not in ans:
                    ans[color[i]] = []
                ans[color[i]].append(i)
            return ans

        color = copy.deepcopy(new_color)


def unbalanced(partitions, cutoff):
    # cutoff is where the labels of the first graphs end and where the labels of the second graph begin
    cnt = 0
    for p, labels in partitions.items():
        for lab in labels:
            cnt += 1 if lab < cutoff else -1
        if cnt != 0:
            return True
    return False


def discrete_coloring(partitions):
    for labels in partitions.values():
        if len(labels) != 2 and len(labels):
            return False
    return True


def choose_max_color_class(partitions, vertices: List[Vertex]):
    best = 3
    for k, vs in partitions.items():
        if len(vs) > best:
            best = len(vs)
            bestk = k

    return bestk


def choose_min_color_class(partitions, vertices: List[Vertex]):
    best = int(1e9)
    for k, vs in partitions.items():
        if 4 <= len(vs) < best:
            best = len(vs)
            bestk = k

    return bestk


def choose_color_class(partitions, vertices: List[Vertex]):
    for k, vs in partitions.items():
        if len(vs) >= 4:
            return k

def choose_most_connected_color_class(partitions, vertices: List[Vertex]):
    color = [0]*len(vertices)
    best = 0,0,0


    for k, vs in partitions.items():
        for v in vs:
            color[v] = k

    for k, vs in partitions.items():
        if len(vs) == 0:
            continue
        cnt = set()
        for n in vertices[vs[0]].neighbours_labels:
            cnt.add(color[n])
        if best[1] < 4 or (len(cnt) > best[0] or (len(cnt) == best[0] and len(vs) > best[1])):
            best = len(cnt), len(vs), k

    return best[2]


class BranchingRules(Enum):
    MIN = choose_min_color_class
    FIRST = choose_color_class
    MAX = choose_max_color_class
    MOST_CONNECT = choose_most_connected_color_class


def prune_twins_isomorphism(g: Graph):
    twin = {}
    twin_id = 0
    for v1 in g.vertices:
        for v2 in g.vertices:
            if v1 == v2:
                continue
            nbh1 = set(v1.neighbors)
            nbh2 = set(v2.neighbors)
            if v2 in nbh1:
                nbh1.remove(v2)
                nbh2.remove(v1)

            if nbh1 == nbh2:
                if v1 in twin:
                    twin[v2] = twin[v1]
                elif v2 in twin:
                    twin[v1] = twin[v2]
                else:
                    twin[v1] = twin_id
                    twin[v2] = twin_id
                    twin_id += 1
    first_occurrence_passed = set()
    for t in twin.keys():
        if t not in twin:
            continue

        if twin[t] not in first_occurrence_passed:
            first_occurrence_passed.add(twin[t])
            t.color_num = 1
        else:
            g.remove_vertex(t)


def old_count_isomorphisms(g: Graph, h: Graph, gh: Graph, di: List, branching_rules: BranchingRules):
    n = len(g.vertices)
    m = len(h.vertices)

    coloring = [0] * (n + m)
    i = 1
    for x, y in di:
        coloring[x] = coloring[y] = i
        i += 1
    partitions = refine(gh, coloring)

    if unbalanced(partitions, len(g.vertices)):
        return 0
    if discrete_coloring(partitions):
        return 1
    color_class = branching_rules(partitions, gh.vertices)

    for v in partitions[color_class]:
        if v < n:
            x = v
            break

    ans = 0

    for y in partitions[color_class]:
        if y >= n:
            di.append((x, y))
            ans += old_count_isomorphisms(g, h, gh, di, branching_rules)
            di.pop()

    return ans


def is_ismorphic(g: Graph, h: Graph, branching_rules: BranchingRules):
    MAX = 4096*65535
    threading.stack_size(MAX)
    found = [False]
    thread = threading.Thread(target=is_isomorphic_thread, args=(g, h, branching_rules, found))
    thread.start()
    thread.join()
    return found[0]

def is_isomorphic_thread(g: Graph, h: Graph, branching_rules: BranchingRules, found):
    gh = g + h
    n = len(g.vertices)
    m = len(h.vertices)
    coloring = [0] * (n + m)
    next_col = 1


    def helper():

        nonlocal found, gh, branching_rules, n, coloring, next_col
        partitions = fast_refine(gh, coloring)

        if unbalanced(partitions, n):
            return
        if discrete_coloring(partitions):
            found[0] = True
            return

        color_class = branching_rules(partitions, gh.vertices)
        x:int = 0
        for v in partitions[color_class]:
            if v < n:
                x = v
                break


        for y in partitions[color_class]:
            if y >= n:
                coloring[x] = coloring[y] = next_col
                next_col += 1
                helper()
                next_col -= 1
                coloring[x] = coloring[y] = 0
                if found[0]:
                    return
    helper()









