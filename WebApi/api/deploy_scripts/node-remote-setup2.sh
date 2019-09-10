scp spring.service psuser@40.91.195.63:~/
scp build/libs/api-1.0.0-SNAPSHOT.jar psuser@40.91.195.63:~/
scp deploy_scripts/node-local-setup2.sh psuser@40.91.195.63:~/
ssh psuser@40.91.195.63 "sudo sh node-local-setup2.sh"
