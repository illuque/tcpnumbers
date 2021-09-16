for i in {1..5}; do
  nc localhost 4000 <"input/client$i.txt" &
done
