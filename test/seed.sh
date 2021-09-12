mkdir input

for i in {1..250000}
do
number=`jot -r 1 0 999999999`
printf "%09d\n" $number >> input/client1.txt
printf "%09d\n" $number >> input/client2.txt
printf "%09d\n" $number >> input/client3.txt
printf "%09d\n" $number >> input/client4.txt
printf "%09d\n" $number >> input/client5.txt
done
