# *-* coding:utf-8 *-*
import numpy as np
import matplotlib.pyplot as plt
import points as pt
points = pt.points

print(points)

readX = False
readY = False
x = ""
y = ""
xs = []
ys = []
for i in points:
    if i=="{":
        readX =True
    elif i==" ":
        readX = False
        xs.append(float(x))
        x = ""
        readY = True
    elif i=="}":
        readY = False
        ys.append(float(y))
        y = ""
    elif readX:
        x+=i
    elif readY:
        y+=i



X = np.array(xs)
Y = np.array(ys)

plt.scatter(X, Y, marker='.', color="b")
countAnnotate = 0
for i in range(len(xs)):
    if countAnnotate ==100:
        countAnnotate = 0
        plt.annotate(str(i), (X[i], Y[i]))
    countAnnotate+=1

plt.gca().invert_yaxis()
    
plt.show()