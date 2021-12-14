javac Host.java Router.java Controller.java

sleep 10

start java Host 0 0 sender 50 20

start java Host 1 1 receiver

start java Router 0 0 1

start java Router 1 1 2

start java Router 2 2 3
start java Router 3 3 0
start java Controller host 0 1 router 0 1 2 3 lan 0 1 2 3
