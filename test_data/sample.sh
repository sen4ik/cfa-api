#!/bin/bash
sudo apt update
sudo apt upgrade -y
sudo apt install -y findutils sudo gnupg bash htop nano vim net-tools curl wget rsync openssh-server ca-certificates tmux locate git tree dirmngr unzip apt-transport-https ca-certificates gnupg2 software-properties-common
# update db for locate 
sudo updatedb
