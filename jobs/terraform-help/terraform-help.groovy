job {
  name 'terraform-help'
  scm {
    git('https://github.com/alphagov/tsuru-terraform.git','master')
  }
  steps {
    shell('terraform --help || true')
  }
}
