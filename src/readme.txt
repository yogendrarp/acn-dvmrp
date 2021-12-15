Submitted By : Yogendra Prabhu
FALL 2021
CS 6390.002
NetId: yrp200001

The code is written on Java and have used open JDK 17. The code has been tested on net01.utdallas.edu which has jdk 1.8.
unizp the file ACN_DVMRP_YRP200001_F21.zip using below command

unzip ACN_DVMRP_YRP200001_F21.zip

Please compile the code using below command

javac Host.java Router.java Controller.java ReadWithLocks.java WriteWithLocks.java

Once done the object can be invoked as ex:

java Host 0 0 sender 50 20

Do not use the run.cmd or cleanup.cmd, they are to test on Windows machines only.

to use them make the sh files executable by below command
chmod +x *.sh
invoke using ./test1.sh
I have attached the updated test files shared by Dr. Cobb to use java pretag. Please compile the code before invoking.


Note : the class files are named in lowercase(not a java standard), so to match with test files(host, router).