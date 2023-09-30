#from graph import *
from fast_color_refinement import fast_refine
from graph_io import load_graph, write_dot

from graph import Graph, Edge
from isomorphismproject.fast_automorphism import count_tree_automorphisms


def test1():
    g = Graph(directed = False, n=7)
    g.add_edge(Edge(g.vertices[0], g.vertices[1]))
    g.add_edge(Edge(g.vertices[1], g.vertices[2]))
    g.add_edge(Edge(g.vertices[1], g.vertices[3]))
    g.add_edge(Edge(g.vertices[0], g.vertices[4]))
    g.add_edge(Edge(g.vertices[4], g.vertices[5]))
    assert g.is_tree()
    g.add_edge(Edge(g.vertices[4], g.vertices[6]))
    print(f'automorphisms: {count_tree_automorphisms(g)}') 
    with open('graph.dot', 'w') as f:
        write_dot(g, f)
    
def test2():
    with open(f'SampleGraphSetBranching/cubes9.grl') as file:
        graph = load_graph(file)
        assert not graph.is_tree()
        
    for i in range(1,4):
        file_name = f'SampleGraphSetBranching/bigtrees{i}.grl'
        print(file_name)
        with open(file_name) as file:
            trees, _ = load_graph(file, read_list=True)
            for i, tree in enumerate(trees):
                assert tree.is_tree()
                print(f'{i}: {count_tree_automorphisms(tree)}')
            print()

def test3():
    g = Graph(directed = False, n=7)
    g.add_edge(Edge(g.vertices[0], g.vertices[1]))
    g.add_edge(Edge(g.vertices[1], g.vertices[2]))
    g.add_edge(Edge(g.vertices[3], g.vertices[4]))
    g.add_edge(Edge(g.vertices[4], g.vertices[5]))
    with open('graph.dot', 'w') as f:
        write_dot(g, f)
    print(g.connected_components())


if __name__ == '__main__':
    test3()