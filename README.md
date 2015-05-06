# multicloud-deploy

Ansible project to create and maintain a [Jenkins](https://jenkins-ci.org/)
service that will be used to continuous deploy our [Tsuru](https://tsuru.io/) [environment](https://github.com/alphagov/tsuru-terraform).

This project uses the [Jenkins Job DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) to define a job
as code and store that job using a version management system. 

No changes should be made to the jenkins server manually as they will not
persist if the virtual instance is ever destroyed and re-created.

## Requirements
 
* `ansible`

## Fetching Ansible Galaxy playbook dependencies

Use the [ansible-galaxy](http://docs.ansible.com/galaxy.html#advanced-control-over-role-requirements-files) command to install third-party playbooks:

`ansible-galaxy install -r requirements.yml`

## Testing

A [vagrant](https://www.vagrantup.com/) file has been provided for local testing it can be brought up by running:

`vagrant up --provision`

There is also a helper [Makefile](https://www.gnu.org/software/make/manual/make.html#Introduction) in the base directory of this project 
that will automatically bring up the environment by running:

`make` and browsing to `http://127.0.0.1:8080`


## Deploying

`ansible-playbook -i inventory.<PROVIDER_NAME> site.yml` 

Where:

<PROVIDER_NAME> is: aws or gce

## Known bugs

* When editing dsl/config.xml and re-running Ansible it looks like the template job changes but not the Terraform job.
