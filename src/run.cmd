javac host.java router.java controller.java

sleep 10

start java host 0 0 sender 50 20

start java host 1 1 receiver

start java router 0 0 1

start java router 1 1 2

start java router 2 2 3
start java router 3 3 0
start java controller host 0 1 router 0 1 2 3 lan 0 1 2 3
