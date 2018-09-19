set xlabel "log_1_0 y_i"
set ylabel "log_1_0 i"
set title "log_1_0 y_i vs log_1_0 i"
plot "section3_part2.dat" using 1:2 title 'log_1_0 y_i vs log_1_0 i' w linespoints
