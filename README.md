# multicloud-deploy

Ansible project to create and maintain a [Jenkins](https://jenkins-ci.org/)
service that will be used to continuous deploy our [Cloudfoundry environment](https://github.com/alphagov/cf-terraform)

This project uses the [Jenkins Job DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) to define a job
as code and store that job using a version management system.

This project implements the use of an [ansible dynamic inventory](https://aws.amazon.com/blogs/apn/getting-started-with-ansible-and-dynamic-amazon-ec2-inventory-management/)
Script and configuration file used for dynamic inventory on aws (ec2.py and ec2.ini) are part of [ansible plugins](https://github.com/ansible/ansible/tree/devel/plugins/inventory)

No changes should be made to the jenkins server manually as they will not
persist if the virtual instance is ever destroyed and re-created.

The Jenkins server is configured to use [GitHub authorization](https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin) [strategy](https://wiki.jenkins-ci.org/display/JENKINS/Role+Strategy+Plugin), where you can define user access levels based on which GitHub organizations they are members of.

[Openconnect VPN client](http://www.infradead.org/openconnect/) is installed to allow Jenkins access our internal GitHub.

## Requirements

* `ansible`

* Python things (you may wish to use [virtualenv](https://virtualenv.pypa.io/en/latest/)):
```
pip install -Ur requirements.txt
```

## Fetching Ansible Galaxy playbook dependencies

Use the [ansible-galaxy](http://docs.ansible.com/galaxy.html#advanced-control-over-role-requirements-files) command to install third-party playbooks:

`ansible-galaxy install -r requirements.yml`

## Testing

A [vagrant](https://www.vagrantup.com/) file has been provided for local testing it can be brought up by running:

`vagrant up --provision`

There is also a helper [Makefile](https://www.gnu.org/software/make/manual/make.html#Introduction) in the base directory of this project
that will automatically bring up the environment by running:

`make vagrant` and browsing to `https://127.0.0.1:8443`

The `vagrant` provisioning run does not configure the following items:

* `Openconnect VPN client` - since this would make the vagrant instance unreachable,
* [GitHub authentication strategy](https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin) - since this would make the jenkins site unconfigurable.

## Preparation

For deployment on aws, you must have the following environment variables set:

* [AWS_ACCESS_KEY_ID](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-environment)
* [AWS_SECRET_ACCESS_KEY](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-environment)

For GitHub integration you will also need to:
* [register](https://github.com/settings/applications/new) a new developer application in your GitHub account
* set GitHub application [callback URL](https://developer.github.com/guides/basics-of-authentication/#registering-your-app) to `https://<my.jenkins.server>/securityRealm/finishLogin`
* configure `client_id` and `client_secret` with the tokens you get after registering the GitHub application
* in case you are not using `GitHub.com`, also configure `github_hostname`, `github_web_uri` and `github_api_uri`
* if your custom server is using self signed certificate, you can define `github_cert` variable to specify your certificate, e.g.:

```yaml
---
github_cert: |
  -----BEGIN CERTIFICATE-----
  < ssl certificate content >
  -----END CERTIFICATE-----
```

You can add your own groups, as well as override default ones by adjusting the `permissions` variable. You can define user to role mapping in `roles`, e.g.:

```yaml
---
permissions:
  admins:
    - hudson.scm.SCM.Tag
    - hudson.model.Run.Delete
    - hudson.model.View.Read
    - ...
  mygroup:
    - hudson.model.View.Create
    - hudson.model.View.Read
    - hudson.model.View.Delete

roles:
  admins:
    - janedoe
  mygroup:
    - johndoe
```

After GitHub authentication strategy is enabled, you will also need to define `jenkins_admin_user` and `jenkins_api_token`. This will enable further updates of Jenkins and job configuration via ansible. User's [api_token](https://wiki.jenkins-ci.org/display/JENKINS/Authenticating+scripted+clients) is available in user configuration.

This repository is using `ansible-vault` to secure sensitive information - If you already know the password you do not need to recreate the 'vault' file.

Encrypt your vault file using `ansible-vault encrypt group_vars/all/vault`

As an example your `vault` could contain the following variables that you may wish to keep secret:

```
vpn_url: "https://my.ssl.vpn.uri"
vpn_user: "my.vpn.user"
vpn_password: "my.vpn.password"

github_web_uri: "https://github.com"
github_api_uri: "https://github.com/api/v3"
client_id: "my.github.client.id"
client_secret: "my.github.client.secret"
github_hostname: "github.com"

jenkins_admin_user: "janedoe"
jenkins_api_token: "my.jenkins.api.token"

roles:
  admins:
    - janedoe
  github_orgs:
    - my-github-org

keystore_password: "my.java.keystore.password"
github_cert: |
  -----BEGIN CERTIFICATE-----
  -----END CERTIFICATE-----

jenkins_gpg_secure_key: |
    -----BEGIN PGP PRIVATE KEY BLOCK-----
    Version: GnuPG v1

    -----END PGP PRIVATE KEY BLOCK-----  

```

### Manual steps

You will need to generate a `pgp key pair` for the `jenkins` user account e.g.:

```
gpg --gen-key
```

Get your KEYID from your keyring:

```
gpg --list-secret-keys | grep sec
```

This will probably be pre-fixed with 2048R/ and look something like 93B1CD02

Send your public key to pgp key server :

```
gpg --keyserver pgp.mit.edu --send-keys KEYID
```

Export your private key to an ascii format file:

```
gpg --export-secret-key -a "User Name" > private.key
```

Paste the contents of the `private.key` into the vault as the value for `jenkins_gpg_secure_key` (see above)

Edit the `group_vars/all/globals.yml` file and set `gpg_public_key_id: KEYID`


## Email notifications

Jenkins server will have email notifications enabled upon deploy. The SMTP server and administrator's name and email address (which will be used as 'from' when receiving notification mails from jenkins) can be configured overriding these global variables:
```
jenkins_mail_name: "Jenkins CI"
jenkins_mail: "itsme@jenkins.com"
smtp_server: "aspmx.l.google.com"
```

## Credentials

Some of the jobs we deploy require our AWS credentials along with the ssh keys. We deploy these to standard locations (jenkins home and ~/.ssh). The variables are optional - credentials will only get deployed when you define:
* `aws_credentials` - AWS credentials for your jobs in a form of shell include. You can include various formats.
* `deployer_key:`- deployer key with elements: `name:` - name of the key and `key:` - the key itself

For example:
```
aws_credentials: |
  export AWS_ACCESS_KEY_ID="mykey"
  export AWS_SECRET_ACCESS_KEY="mysecret"

deployer_key:
  name: "my-deployer"
  key: |
    -----BEGIN RSA PRIVATE KEY-----
	yourkeyhere
    -----END RSA PRIVATE KEY-----
```

## DNS

### AWS

You can define `public_eip` variable to set the external IP of the server to the [Elastic IP](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html) address you have allocated previously. If you use AWS [Route53](http://aws.amazon.com/route53/) as a DNS provider you can define `r53_zone` and `dns_name` variables. A `{{dns_name}}.{{r53_zone}}` DNS record will be created/updated for the Jenkins server and set to its external IP.

## Deployment

`make aws`


## Known bugs/issues

* At this moment, only the 'aws' platform is supported
* You will need to log in to jenkins to obtain the `jenkins_api_token` and add it to your ansible `globals.yml` or `vault` file to enable subsequent jenkins and job configuration

## Dashboard

This repository also contains a `dashboard.html` web page which can be used as an alternative to Fourth Wall dashboard.

It combines Fourth Wall and Jenkins Buld Monitor View Plugin to display two dashboards side by side.

In order to use it, just copy this file locally to the computer running the dashboard, and open the file in a browser, passing required query string parameters:

- token (Github token)
- gist (ID of a Gist listing all repositories we are interested in)

Optional parameters:

- JENKINS_URL (defaults to `https://deploy.tools.paas.alphagov.co.uk`)
- FOURTH_WALL_URL (defaults to `https://alphagov.github.io/fourth-wall`)
- LEFT_WIDTH (defaults to `50`)
- RIGHT_WIDTH (defaults to `50`)

The dashboard simply displays two iframes side by side.

Alternatively, if you don't want to copy the file locally, you can just use raw Github URL, something like:

```
https://cdn.rawgit.com/alphagov/multicloud-deploy/master/dashboard.html?token=XXXX&gist=XXXXX
```
