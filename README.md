# multicloud-deploy

Ansible project to create and maintain a [Jenkins](https://jenkins-ci.org/)
service that will be used to continuous deploy our [Tsuru](https://tsuru.io/) [environment](https://github.com/alphagov/tsuru-terraform).

This project uses the [Jenkins Job DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) to define a job
as code and store that job using a version management system. 

This project implements the use of an [ansible dynamic inventory](https://aws.amazon.com/blogs/apn/getting-started-with-ansible-and-dynamic-amazon-ec2-inventory-management/) 
Script and configuration file used for dynamic inventory on aws (ec2.py and ec2.ini) are part of [ansible plugins](https://github.com/ansible/ansible/tree/devel/plugins/inventory)

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

`make vagrant` and browsing to `http://127.0.0.1:8080`

## Preparation

For deployment on aws, you must have the following environment variables set:

* [AWS_ACCESS_KEY_ID](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-environment)
* [AWS_SECRET_ACCESS_KEY](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-environment)

## Deployment

`ansible-playbook -i localhost, <PROVIDER_NAME>-provision.yml -v`

`ansible-playbook -i ec2.py site.yml -v`

Or:

`make <PROVIDER_NAME>`

Where:

<PROVIDER_NAME> is: aws or gce

## Known bugs/issues

* At this moment, only the 'aws' platform is supported
