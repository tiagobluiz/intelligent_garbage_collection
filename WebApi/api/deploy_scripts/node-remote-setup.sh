scp spring.service dbuser@40.91.197.48:~/
scp build/libs/api-1.0.0-SNAPSHOT.jar dbuser@40.91.197.48:~/
scp deploy_scripts/node-local-setup2.sh dbuser@40.91.197.48:~/
ssh dbuser@40.91.197.48 "sudo sh node-local-setup.sh"
