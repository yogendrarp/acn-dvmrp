rm -f lan?  hout?  hin?
rm -f rout?
java host 0 0 sender 50 20&
java host 1 1 receiver &
java host 2 2 receiver &
java router 0 0 1 &
java router 1 1 2 &
java router 2 2 3 &
java router 3 3 0 &
java controller host 0 1 2 router 0 1 2 3 lan 0 1 2 3&
