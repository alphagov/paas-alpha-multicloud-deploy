# -*- mode: ruby -*-
# vi: set ft=ruby :

MEMORY_DEFAULT = 448

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.hostname = "jenkins"

  config.vm.provider "docker" do |d, override|
    d.image = 'guilhem/vagrant-ubuntu'
    d.has_ssh = true

    # This is needed if you have non-Docker provisioners in the Vagrantfile.
    override.vm.box = nil

    # Ensure Vagrant knows the SSH port. See
    # https://github.com/mitchellh/vagrant/issues/3772.
    override.ssh.port = 22

    # Map port 443 from within jenkins container to 8443 on localhost
    d.ports = [ "8433:443" ]
  end

  config.vm.provider :virtualbox do |v|
    v.memory = MEMORY_DEFAULT
  end

  config.vm.provider :vmware_fusion do |v|
    v.vmx["memsize"] = MEMORY_DEFAULT
  end

  config.vm.network "forwarded_port", guest: 443, host: 8443, auto_correct: true

  config.vm.provision :shell, inline: "apt-get purge -qq -y --auto-remove chef puppet"
  config.vm.provision :ansible do |ansible|
    ansible.extra_vars = { vagrant: true }
    ansible.groups = { "jenkins-master" => ["default"] }
    ansible.playbook = "site.yml"
  end
end
