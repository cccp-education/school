import random

Alphabet = []
for item in ['AZERTYUIOPQSDFGHJKLMWXCVBN'][0]:
    Alphabet.append(item)


def ReciproquePythagore(m, n=2):  # m est le nombre d'exercices et 2*n le nombre d'items dans chaque exo
    L = []
    bons = [[3, 4, 5], [5, 12, 13], [7, 24, 25], [9, 40, 41], [11, 60, 61]]
    for loop in range(m):
        print("\\begin{exo} \\astuce\\begin{enumerate}")
        for loop in range(n):
            random.shuffle(Alphabet)
            L2 = [0, 1, 2]
            random.shuffle(L2)
            a, b, c = L2[0], L2[1], L2[2]
            test = random.randint(0, 1)
            if test == 0:
                longueur1 = random.randint(2, 99)
                longueur2 = random.randint(2, 99)
                longueur3 = random.randint(2, 99)
            else:
                random.shuffle(bons)
                m = random.randint(1, 5)  # Sert à mutliplier les bons triangles rectangles
                longueur1, longueur2, longueur3 = m * bons[0][0], m * bons[0][1], m * bons[0][2]
            print("\\item Considérons le triangle $", Alphabet[0], Alphabet[1], Alphabet[2], "$ avec $", end="")
            print(Alphabet[a], Alphabet[c], "=", longueur1, "$,$", Alphabet[a], Alphabet[b], "=", longueur2, "$ et $",
                  Alphabet[c], Alphabet[b], "=", longueur3, "$.")
            print("Déterminer si le triangle $", Alphabet[0], Alphabet[1], Alphabet[2], "$ est un triangle rectangle.")
            if longueur1 ** 2 + longueur2 ** 2 == longueur3 ** 2:
                L.append("Oui")
            else:
                L.append("Non")
        print("\\end{enumerate}\\end{exo}")
    print("\\begin{multicols}{4}")
    print(L)
    for z in range(m):
        print(" \\begin{cor}\\astuce\\begin{enumerate}")
        for j in range(n):
            print(L[j + z])
        print("\\end{enumerate}\\end{cor}")
    print("\\end{multicols}")


if __name__ == '__main__':
    ReciproquePythagore(3, 2)
