.PHONY: all \
	vagrant clean-roles ansible-galaxy \
	vagrant-up aws aws-provision site \
	import-gpg-keys recrypt

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
	ansible-playbook -i localhost, aws-provision.yml -v

site: 
	ansible-playbook -i ec2.py site.yml -v

import-gpg-keys:
	$(foreach var, \
		$(shell cat gpg.recipients | awk -F: '{print $$1}'), \
		gpg --list-public-key $(var) || gpg --keyserver hkp://keyserver.ubuntu.com --search-keys $(var);)

recrypt: import-gpg-keys
	ansible-vault decrypt group_vars/all/vault && pwgen -cynC1 15 | \
		gpg --batch --yes --trust-model always -e -o vault_passphrase.gpg \
			$(shell cat gpg.recipients | awk -F: {'printf "-r "$$1" "'}) && \
		ansible-vault encrypt group_vars/all/vault

