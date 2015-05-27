job {
  name 'ansible-deploy-aws'
  description('Run ansible against Amazon EC2')
  parameters {
    choiceParam("DEPLOY_ENV", ["ci"],
            "Select which environment you wish to run ansible against")
  }
  scm {
    git {
      remote {
	url('https://github.com/alphagov/tsuru-ansible.git')
      }
      branch('master')
      createTag(false)
    }
  }
  triggers {
    scm("H/5 * * * *")
    upstream('terraform-deploy','SUCCESS')
  }
  wrappers {
    colorizeOutput()
  }
  publishers {
    mailer("the-multi-cloud-paas-team@digital.cabinet-office.gov.uk", false, true)
  }
  steps {
    shell('''#!/bin/bash
# Disable output buffering to give realtime data
export PYTHONUNBUFFERED=1

[[ -f /usr/bin/figlet ]] && figlet Assigning SSH private key
eval $(ssh-agent) && ssh-add ~/.ssh/insecure-deployer 

# Setting up trap to clean up ssh-agent process in the event of any failure
set -e
trap "kill ${SSH_AGENT_PID}" ERR

[[ -f /usr/bin/figlet ]] && figlet Sourcing EC2 credentials
. ~/.aws_credentials
export AWS_ACCESS_KEY_ID=${aws_access_key}
export AWS_SECRET_ACCESS_KEY=${aws_secret_key}
echo done...


[[ -f /usr/bin/figlet ]] && figlet Setting up virtualenv
virtualenv .venv
. .venv/bin/activate

[[ -f /usr/bin/figlet ]] && figlet Installing python dependencies
pip install -Ur requirements.txt


[[ -f /usr/bin/figlet ]] \
  && figlet Refreshing ec2 inventory cache
./ec2.py --refresh-cache > /dev/null 2>&1 && echo done...

[[ -f /usr/bin/figlet ]] \
  && figlet Configure ansible to use ${DEPLOY_ENV} environment
make check-env-var render-ssh-config clean-roles ansible-galaxy 

[[ -f /usr/bin/figlet ]] \
  && figlet Running ansible against ${DEPLOY_ENV} environment
ansible-playbook -i ec2.py \
  --vault-password-file ~/.vault_pass.txt \
  -e "deploy_env=${DEPLOY_ENV}" \
  -e "@platform-aws.yml" \
  site-aws.yml


kill ${SSH_AGENT_PID}
''')
  }
}
