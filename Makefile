all: clean-roles ansible-galaxy vagrant-up

clean-roles:
	rm -rf -- roles/*

ansible-galaxy:
	ansible-galaxy install -r requirements.yml

vagrant-up:
	vagrant up --provision
