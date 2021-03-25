import socket

# Create a socket object 
s = socket.socket()

# Define the port on which you want to connect 
port = 1619

# connect to the server on local computer 
s.connect(('localhost', port)) 

# receive data from the server 
print (s.recv(4069).decode() )
s.send("{\n this is\n}".encode() )
# close the connection 
s.close()
