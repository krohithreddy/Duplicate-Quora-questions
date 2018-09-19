set xlabel "number of terms in the dictionary(M)"
set ylabel "number of tokens(T)"
set title "log M vs log T"
plot "section3_part1.dat" using 1:2 title 'log M vs log T' w linespoints
