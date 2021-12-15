rm -f lan?  hout?  hin?
rm -f rout?
java host 0 0 sender 20 20&
java router 0 0 1 &
java router 1 1 2 &
java controller host 0 1 router 0 1 lan 0 1 2&
