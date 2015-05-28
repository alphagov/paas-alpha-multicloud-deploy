job {
  name 'terraform-deploy'
  description('Deploy and automatically update Terraform environments upon repository changes')
  parameters {
    choiceParam("action", ["apply", "destroy"],
                "Select if you want to apply changes to environments or destroy them.")
  }
  scm {
    git {
      remote {
	url('https://github.com/alphagov/tsuru-terraform.git')
      }
      branch('master')
      createTag(false)
    }
  }
  triggers {
    scm("H/5 * * * *")
  }
  wrappers {
    colorizeOutput()
  }
  publishers {
    mailer("the-multi-cloud-paas-team@digital.cabinet-office.gov.uk", false, true)
  }
  steps {
    shell('''#!/bin/bash
set -x
home=`pwd`
environment_name="ci"

# Set-up ssh keys and gce credentials
mkdir -p gce/ssh aws/ssh
cp ~/.ssh/* gce/ssh
cp ~/.ssh/* aws/ssh
cp ~/.account.json gce/account.json

# Terraform
set -e
[[ ${action} == "destroy" ]] && extraopts="-force"

# GCE
cd gce
terraform ${action} -var env=${environment_name} -input=false ${extraopts}

# AWS
cd ../aws
. ~/.aws_credentials
terraform ${action} -var env=${environment_name} -input=false ${extraopts}
''')
  }
}
