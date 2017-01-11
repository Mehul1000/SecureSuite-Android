CS Security  
-----------  

Security Dependencies  
---------------------  

CS uses the Guardian Project SQLCipher
CS also uses Java's standard security packages


Data in Place  
-------------  

1. The CS SQLCipher database uses a volume 32 character password, randomly generated using Math.random()
2. It is encrypted and stored in the Apps private data area
3. The encryption key is generated from a static application key and the user account email address

4. The user can replace the 32 character password (future)


Data in Motion  
--------------  

1. CS uses a self signed security certificate for https communications
2. The certificate cannot be validated by the browser, hence it generates nasty warnings


Vulnerabilities  
---------------  

1. Math.random() can be predicted, it is used to create the initial database password
2. The static encryption key can be derived if the user account email is captured
3. The static encryption key can be seen when reverse engineering the application
4. Certain memory locations hold sensitive information


Security Improvements  
---------------------  

1. Obscure the static enceryption key making reverse engineering more difficult
2. Clear memory holding sensitive information after use


