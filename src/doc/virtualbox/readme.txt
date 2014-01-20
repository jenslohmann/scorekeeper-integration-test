On guest ubuntu:
jenkins@jenkins:~$ sudo ufw disable
jenkins@jenkins:~$ sudo apt-get install git
jenkins@jenkins:~$ sudo apt-get install openjdk-7-jdk

In /etc/default/tomcat7:
CATALINA_OPTS="-DJENKINS_HOME=/var/lib/tomcat7/webapps/jenkins/ -Duser.home=/home/jenkins/tomcat7Home"

jenkins@jenkins:~$ mkdir tomcat7Home
jenkins@jenkins:~$ sudo chown tomcat7.tomcat7 tomcat7Home/
jenkins@jenkins:~$ mkdir tomcat7Jenkins
jenkins@jenkins:~$ sudo chown tomcat7.tomcat7 tomcat7Jenkins/

jenkins@jenkins:/usr/share/tomcat7$ sudo ln -s ~jenkins/tomcat7Jenkins/ .jenkins


(In VirtualBox, setup Network1 as Host only, Network2 as NAT)
/etc/network/interfaces:
[blah...]
# The primary network interface
auto eth0
iface eth0 inet static
	address	192.168.56.20
	netmask	255.255.255.0
	network 192.168.56.0
	broadcast 192.168.56.255

auto eth1
iface eth1 inet dhcp
