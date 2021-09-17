for i in {1..6}; do
  nc localhost 4000 <"input/client$i.txt" &
done
