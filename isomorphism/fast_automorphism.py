import math
import operator
import threading
from functools import reduce
from typing import List

from basicpermutationgroup import Reduce, Orbit, FindNonTrivialOrbit, Stabilizer
from fast_color_refinement import fast_refine
from graph import Graph, Edge
from individualization_refinement import BranchingRules, unbalanced, discrete_coloring
from permv2 import permutation


def calculate_order(generators):
    non_trivial_orbit_el = FindNonTrivialOrbit(generators)
    size = 1
    while non_trivial_orbit_el is not None:
        size *= len(Orbit(generators, non_trivial_orbit_el))
        generators = Stabilizer(generators, non_trivial_orbit_el)
        non_trivial_orbit_el = FindNonTrivialOrbit(generators)
    return size

def is_member(generators, perm:permutation):
    if perm.istrivial():
        return True
    non_trivial_orbit_el = FindNonTrivialOrbit(generators)
    if non_trivial_orbit_el is None:
        return False
    orbit, transversal= Orbit(generators, non_trivial_orbit_el, returntransversal=True)
    b = perm[non_trivial_orbit_el]

    for i in range(len(orbit)):
        if orbit[i] == b:
            return is_member(Stabilizer(generators, non_trivial_orbit_el), (transversal[i]**-1)*perm)

    return False

def fast_automorphism(g: Graph, branching_rules: BranchingRules):
    if g.is_tree():
        return count_tree_automorphisms(g)
    MAX = 4096*65535
    threading.stack_size(MAX)
    count = [0]
    thread = threading.Thread(target=fast_automorphism_thread, args=(g, branching_rules, count))
    thread.start()
    thread.join()
    return count[0]


def fast_automorphism_thread(g: Graph, branching_rules: BranchingRules, count):
    n = len(g.vertices)
    gg = g + g
    color = [0] * (2*n)
    generating_set = []
    next_col = 1
    def helper():
        nonlocal gg, n, branching_rules, next_col, generating_set
        partitions = fast_refine(gg, color)

        if unbalanced(partitions, len(g.vertices)):
            return False
        if discrete_coloring(partitions):
            perm = [0]*n
            for i,v in partitions.items():
                perm[min(v)] = max(v) - n
            perm = permutation(n, mapping=perm)
            generating_set.append(perm)
            generating_set = Reduce(generating_set)
            # if not is_member(generating_set, perm):
            #     generating_set.append(perm)

            return True
        color_class = branching_rules(partitions, gg.vertices)
        x = 0
        for v in partitions[color_class]:
            if v < n:
                x = v
                break

        x_to_x = (x + n) in partitions[color_class]
        trivial = True
        for i in range(n):
            if color[i] != color[i + n]:
                trivial = False

        # start with mapping x to x if possible
        if x_to_x:
            color[x] = color[x + n] = next_col
            next_col += 1
            found = helper()
            next_col -= 1
            color[x] = color[x + n] = 0
            if found and not trivial:
                return True

        for y in partitions[color_class]:
            if y >= n and y != x + n:
                color[x] = color[y] = next_col
                next_col += 1
                found = helper()
                next_col -= 1
                color[x] = color[y] = 0
                if found and not trivial:
                    return True
        return False
    helper()
    count[0] = calculate_order(generating_set)
    return count[0]

def membership_test():
    generators = [permutation(6, cycles=[[0,1,2],[4,5]]), permutation(6, cycles=[[2,3]])]

def order_test():
    order = calculate_order([permutation(6, cycles=[[0,1,2],[4,5]]), permutation(6, cycles=[[2,3]])])
    print(order)

def automorphism_test():
    g = Graph(False, 7)
    for (u,v) in [(1,0), (2,0), (3,0), (4,0), (5,0), (6,0), (1,3), (3,2), (4,6), (5,6)]:
        g.add_edge(Edge(g.vertices[u], g.vertices[v]))

    size = fast_automorphism(g, branching_rules=BranchingRules.MAX)
    print(size)

def main():
    automorphism_test()

if __name__ == '__main__':
    main()
    

def count_tree_automorphisms(tree: 'Graph') -> int:
    def count_possible_paths(current_vertex, previous_vertex):
        representatives = {}
        cardinalities = {}
        for n in current_vertex.neighbors:
            if n is previous_vertex:
                continue
            
            if n.color not in representatives:
                representatives[n.color] = n
                cardinalities[n.color] = 1
            else:
                cardinalities[n.color] += 1
        if len(representatives) == 0:
            return 1
        subtree_automorphisms = {color: math.factorial(cardinality) * count_possible_paths(representatives[color], current_vertex)**cardinality for color, cardinality in cardinalities.items()}

        return reduce(operator.mul, subtree_automorphisms.values())

    # assign color to each vertex
    coloring = fast_refine(tree)
    for color, vertex_indices in coloring.items():
        for i in vertex_indices:
            tree.vertices[i].color = color
    
    start_vertex = tree.vertices[0]
    start_possibilities = len(coloring[start_vertex.color])
    return start_possibilities * count_possible_paths(start_vertex, None)


