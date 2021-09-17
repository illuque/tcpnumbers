mkdir input

for i in {1..6}; do
  file="input/client$i.txt"

  # clear file
  rm $file

  # fill files with random numbers
  jot -r -w '%09d' 500000 0 999999999 >> $file
done
