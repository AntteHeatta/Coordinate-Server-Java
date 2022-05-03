# Coordinate-Server-Java
HTTPS Server written in Java to receive and send coordinates
<br>
This is a school project, and written on a given template.
<br>
<br>

Instructions:
<br>
1. Install Curl-tool<br>
2. Create a certificate using command prompt with the command: keytool -genkey -alias alias -keyalg RSA -keystore keystore.jks -keysize 2048<br>
3. Change the filepath in Server.java (Line 97) to match where your certificate is<br>
4. Change the "Keystore.jks password here" to your password used in the creation of certificate (Line 94 on Server.java and include ")<br><br>

I suggest to use DB Reader to access the database. It makes it easy to see what is inside the database<br>


<br>
Curl-commands<br>
NOTE: You will need to have corresponding .json files in the same folder where you are running your Curl-commands<br>
Also, the " and ' might cause problems. Test which is suitable to your system.<br><br>
To register the user:<br>
Windows: curl -k -d "@user.json" https://localhost:8001/registration -H 'Content-Type: application/json’<br>
Linux/macOS: curl -k -d "@./user.json" https://localhost:8001/registration -H 'Content-Type: application/json’<br><br>
To send coordinate to the server:<br>
curl -k -d "@coordinates.json" https://localhost:8001/coordinates -H 'Content-Type: application/json’ -u johndoe:password<br><br>
To request the coordinates from the server: curl -k https://localhost:8001/coordinates -H 'Content-Type: application/json’ -u johndoe:password<br><br>

Credits to course teachers for the templates and help:<br>
Markus Kelanti<br>
Nicklas Stafford<br>
Jarkko Suominen<br>
Nirnaya Tripathi<br>
