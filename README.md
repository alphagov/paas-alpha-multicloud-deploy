# multicloud-deploy

Ansible project to create and maintain a [Jenkins](https://jenkins-ci.org/)
service that will be used to continuous deploy our [Tsuru](https://tsuru.io/) [environment](https://github.com/alphagov/tsuru-terraform).

This project uses the [Jenkins Job DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) to define a job
as code and store that job using a version management system. 

No changes should be made to the jenkins server manually as they will not
persist if the virtual instance is ever destroyed and re-created.

## Requirements
 
* `ansible`

## Testing

A [vagrant](https://www.vagrantup.com/) file has been provided for local testing it can be brought up by running:

`vagrant up --provision`

## Deploying

`ansible-playbook -i inventory.<PROVIDER_NAME> site.yml` 

Where:

<PROVIDER_NAME> is: aws or gce
