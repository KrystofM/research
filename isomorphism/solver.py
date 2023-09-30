from fast_automorphism import fast_automorphism
from graph import *
from old_color_refinement import get_possible_isomorphisms
from individualization_refinement import is_ismorphic, prune_twins_isomorphism, BranchingRules



def get_isomorphisms(graphs: List[Graph], branching_rules: BranchingRules):
    possibly_iso = get_possible_isomorphisms(graphs)

    isomorphisms = []
    for s in possibly_iso:
        for i in range(len(s)):
            curr = graphs[s[i]]
            prune_twins_isomorphism(curr)
        computed = set()
        for i in range(len(s)):
            if i in computed:
                continue
            for j in range(i + 1, len(s)):
                if j in computed:
                    continue
                if is_ismorphic(graphs[s[i]], graphs[s[j]], branching_rules):
                    found = False
                    for index in range(len(isomorphisms)):
                        if s[i] in isomorphisms[index] or s[j] in isomorphisms[index]:
                            isomorphisms[index].add(s[i])
                            isomorphisms[index].add(s[j])
                            found = True
                    if not found:
                        isomorphisms.append({s[i], s[j]})

    for e in range(len(graphs)):
        found = False
        for i in range(len(isomorphisms)):
            if e in isomorphisms[i]:
                found = True
                break
        if not found:
            isomorphisms.append({e})

    return isomorphisms


def get_isomorphism_count(graphs: List[Graph], branching_rules:BranchingRules):
    possibly_iso = get_possible_isomorphisms(graphs)
    isomorphisms = []



    for s in possibly_iso:
        computed = set()
        for i in range(len(s)):
            if i in computed:
                continue
            for j in range(i + 1, len(s)):
                if j in computed:
                    continue
                #cnt = count_isomorphisms(graphs[s[i]], graphs[s[j]],graphs[s[i]] + graphs[s[j]], [], branching_rules)
                if is_ismorphic(graphs[s[i]], graphs[s[j]], branching_rules):
                    computed.add(j)
                    found = False
                    for index in range(len(isomorphisms)):
                        if s[i] in isomorphisms[index][0] or s[j] in isomorphisms[index][0]:
                            isomorphisms[index][0].add(s[i])
                            isomorphisms[index][0].add(s[j])
                            found = True
                    if not found:
                        isomorphisms.append(({s[i], s[j]}, fast_automorphism(graphs[s[i]], branching_rules)))

    for e in range(len(graphs)):
        found = False
        for i in range(len(isomorphisms)):
            if e in isomorphisms[i][0]:
                found = True
                break
        if not found:
            isomorphisms.append(({e}, fast_automorphism(graphs[e], branching_rules)))

    return isomorphisms


