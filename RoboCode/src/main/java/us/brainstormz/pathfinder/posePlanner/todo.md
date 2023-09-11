# Pose Planner 
This is the code to find paths.


TODO:
- improve obstruction collision detection
  - line poly intersect formula, instead of firstIntersect()
- clean up
  - use coordinate wherever possible 
  - make astar barebones. i.e. no extra functionality inside of that class
- add ability to use existing nearby points as neighbors
- add hitbox
  - eligable neighbors must not have any obstacles withing *x*
  - nearest point on poly to point (https://gis.stackexchange.com/questions/104161/how-can-i-find-closest-point-on-a-polygon-from-a-point)
- add mecanum kenimatics
- add interpolation
  - Splines?
