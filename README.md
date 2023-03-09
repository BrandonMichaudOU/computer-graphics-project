# computer-graphics-project  

This is our Computer Graphics project for the Spring of 2023 at the University of Oklahoma. The purpose of this project is to learn OpenGL, help students understand graph algorithms, and practice working on a long term team project.  

# How to Run  
In terminal, navigate to the ou-cs-cg directory.  run "./gradlew installDist"  
Then navigate to the ou-cs-cg/build/install directory, and run "./cg-project"

# Controls  
Click a node to set the starting node for the algorithms. The node will turn blue to indicate this.  
Pressing B will initiate a Bredth First Search (BFS) from this node.  
Pressing Q will reset any paths the graph has.  
Pressing C will delete the graph.  
Clicking outside a node will create a new node.  
Shift clicking a node will designate it as an endpoint of an edge, and then clicking a different node will create the new edge.  