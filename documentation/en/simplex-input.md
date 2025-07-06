# The inputs for the simplex method

If you took a stroll through the code, you may have encountered a "*SimplexForm*", which is an object representing the inputs for a simplex problem.

This input format has been found and adapted from the online course ["*Integer and linear programming*" on coursera](https://www.coursera.org/learn/linear-programming-and-approximation-algorithms), prensented by [S. Sankaranarayanan](https://home.cs.colorado.edu/~srirams/) and [S. D. Ruben](https://www.colorado.edu/lab/automation/people/dr-shalom-d-ruben), from [the university of Colorado, Boulder](https://www.colorado.edu/). [Presentation video](https://youtu.be/1ZPaTI5e128?si=qfZOySvS2rlx3IQa) of the course.  

The [unit test files](/tests/unitTests/) are under this standardized form, and are good exemples.

A standardized input file looks like this:  
```
m n
B1 B2 ... Bm
N1 N2 ... Nn
b1 b2 ... bm
a11 a12 ... a1n
...
am1 am2 ... amn
z0 c1 ... cn
```

Which, in his mathematical form, looks like this:  
![Mathematical form of the inputs](../images/simplex-input.png)
> There is a typo on `a11xN1` on the line before the last. It should be `am1xN1`.

> [!NOTE]
> This is not the usual standard form for a linear optimisation problem.
> But we can deduce it from these entries, without much transformations, if we ignore the `B` and `N` indices lines.

> [!CAUTION]
> When reading the file, every characters except `0123456789.-`, and with space and line break, will immediatly raise an error.
> Furthermore, every bad representation of a number (two minus signs, multiple subsequent dots, leading 0...) will also raise an immediate error.
>
> Lastly, floatting numbers have their interger and decimal parts separated with a dot.

1. `m` is an **integer**, greater or equal than 1, which give the number of **constraints**. It also gives, by definition, the number of basic variables.
2. `n` is an **integer**, greater or equal than 1, which give the number of **variables**. It also gives, by definition, the number of coefficients per constraint, and the number of coefficients of the ojective function `z`.
3. `Bi` is an **integer**, greater or equal than 1, which is the asic variable `i`. It **must** have `m` number of `Bi`. If there is more, they are ignored. If there is less, an error is raised.
4. `Ni` is an **integer**, greater or equal than 1, which is non basic variable `i`. It **must** have `n` number of `Ni`. If there is more, they are ignored. If there is less, an error is raised.
5. `bi` is a **float**, which is the right hand side constant of the  constraint `i`, in the standard form. It **must** have `m` number of `bi`. If there is more, they are ignored. If there is less, an error is raised.
6. `aij` is a **float**, which is the coefficient `j` of the constraint `i`. It **must** have `m` number of lines, otherwise an error is raised. It **must** have `n` number of `aij` par ligne. If there is more, they are ignored. If there is less, an error is raised.
7. The last ligne **is read after the last coefficient line**, and is made of `n+1` values:
   1. `z` is a **float**, which is constant added to the objective function. It **must** be present, even if it's a 0.
   2. `ci` is a **float**, which is the coefficient `i` of the objective function. It **must** have `n` number of `ci`. If there is more, they are ignored. If there is less, an error is raised.
