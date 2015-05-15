all: 
	@echo "Please provide a target: vagrant|aws"

vagrant: clean-roles ansible-galaxy vagrant-up

clean-roles:
	rm -rf -- roles/*

ansible-galaxy:
	ansible-galaxy install -r requirements.yml --force

vagrant-up:
	vagrant up --provision

aws: aws-provision ansible-galaxy site

aws-provision: 
	ansible-playbook -i localhost, aws-provision.yml -v --ask-vault-pass

site: 
	ansible-playbook -i ec2.py site.yml -v --ask-vault-pass
