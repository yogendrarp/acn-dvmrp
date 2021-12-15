rm -f lan?  hout?  hin?
rm -f rout?
java router 0 0 1 &
java router 1 1 2 &
java router 2 2 3 &
java router 3 3 0 &
java controller host router 0 1 2 3 lan 0 1 2 3&
