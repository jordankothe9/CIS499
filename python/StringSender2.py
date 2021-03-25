import socket

# Create a socket object 
s = socket.socket()

# Define the port on which you want to connect 
port = 1619

# connect to the server on local computer 
s.connect(('localhost', port)) 

# receive data from the server 
print (s.recv(4069).decode() )
s.send("{\n "VehicleType": "Sedan",\n"LicensePlate": "QHY682",\n"LicensePlateConf": 0.886,\n"Make": "Mercedes-Benz",\n"Model": "E-Class",\n"Color": "black",\n"State": "SC",\n"Status": "in",\n"TimeStamp": "1614638840553.029"\n}".encode() )
# close the connection 
s.close()
