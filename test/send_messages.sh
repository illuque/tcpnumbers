for i in {1..10}; do
  nc localhost 4000 <"input/client$i.txt" &
done
