import time
from enum import Enum
import sys
import os
from os import listdir

from fast_automorphism import fast_automorphism
from fast_color_refinement import fast_refine
from graph_io import load_graph, write_dot
from individualization_refinement import BranchingRules
from solver import get_isomorphisms, get_isomorphism_count

FOLDER = 'input/'


class TestSet(Enum):
    SMALL = ['cubes3', 'cubes4', 'torus24', 'trees11', 'trees36', 'wheeljoin14']
    MEDIUM = ['bigtrees1', 'cographs1', 'cubes5', 'cubes6', 'products72', 'torus72', 'wheeljoin19', 'wheeljoin25',
              'wheelstar12']
    LARGE = ['bigtrees2', 'bigtrees3', 'modulesC', 'trees90', 'wheeljoin33', 'wheelstar15', 'wheelstar16']
    TOO_LARGE = ['cubes7', 'cubes8', 'cubes9', 'modulesD', 'products216', 'torus144']
    TREES = ['trees11', 'trees36','trees90', 'bigtrees1', 'bigtrees2', 'bigtrees3']
    ALL = SMALL + MEDIUM + LARGE
    BRANCHING_TEST_SET = ['cubes4', 'cubes5','products72', 'wheeljoin19', 'wheeljoin33', 'modulesC']

    CUSTOM = ['counter_example_2']

def read(folder, file):
    with open(folder + file) as f:
        graphs = load_graph(f, read_list=True)[0]
    return graphs


def test_set(testing_set: TestSet, branching_rules: BranchingRules, count: bool):
    sys.setrecursionlimit(10000)
    total = time.time()
    for f in testing_set.value:
        print(f'{FOLDER}{f}.grl')
        t = test_file(f, branching_rules, count)
        print(f"Time: {round(t, 2)} seconds\n")
    print(f"Total time: {round(time.time() - total, 2)} seconds")


def test_file(file, branching_rules: BranchingRules, count: bool, folder=FOLDER):
    graphs = read(folder, f"{file}.grl")
    t = time.time()

    # for i in range(len(graphs)):
    #     with open(f'output/{file}_G{i}.dot', 'w') as f:
    #         write_dot(graphs[i], f)

    if count:
        iso_cnt = get_isomorphism_count(graphs, branching_rules)
        t = time.time() - t
        for s,cnt in iso_cnt:
            print(list(s), cnt)
    else:
        iso = get_isomorphisms(graphs, branching_rules)
        t = time.time() - t
        for set in iso:
            print(list(set))

    return t

def test_refinement(folder = 'SampleGraphsFastColorRefinement/'):
    total = 0
    files = sorted(os.listdir(folder), key= lambda x: (len(x), x))
    for f in files:

        print(f'{folder}{f}')
        graph = read(folder, f)[0]
        start = time.time()
        fast_refine(graph, [0]*len(graph.vertices))
        t = time.time() - start
        total+=t
        print(f"Time: {round(t, 4)} seconds\n")
    print(f"Total time(without reading): {round(total, 2)} seconds")

def test_testzip_format(branching_rules, folder ='test/'):
    start = time.time()
    for f in listdir(folder):
        print(f)
        file_time = time.time()
        count = f.find("Aut") != -1
        gi = f.find("GI") != -1
        if f[-3:] == '.gr':
            g = read(folder, f)[0]
            print(fast_automorphism(g, branching_rules=branching_rules))
        else:
            if not gi:
                for g in read(folder, f):
                    print(fast_automorphism(g, branching_rules=branching_rules))
            else:
                test_file(f[:-4], branching_rules, count, folder)

        print(f"File time: {round(time.time() - file_time, 3)} seconds")
    print(f"Total time: {round(time.time() - start, 3)} seconds")
def main():
    sys.setrecursionlimit(100000000)
    test_testzip_format(BranchingRules.MAX, folder=FOLDER)  # put / after folder name

if __name__ == "__main__":
    try:
        os.makedirs("output")
    except FileExistsError:
        # directory already exists
        pass
    main()
