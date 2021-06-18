# Harmonious Simplification

This project provides code developed in context of research into the simplification of sets of isolines in a harmonious way. 
That is, it aims to reduce its number of vertices (possibly introducing new locations as well),
while keeping the isolines similar to their original geometry and to each other.

It builds on the GeometryCore (https://github.com/tue-alga/GeometryCore) and was developed under version v1.1.0.

This version of the code supports only polygonal lines as isolines and was used in the experiments for:
    Van Goethem et al, Harmonious Simplification of Isolines, Proc. GIScience 2021.
The default settings correspond to the described algorithm. 
Though the implementation in places uses simpler techniques than would be efficient in terms of run time.