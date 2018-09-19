set title "histogram of gaps with bin size=20"
set style histogram clustered
set style fill solid 1.0
plot 'section3_part3.dat' using 1:2 smooth freq with boxes
