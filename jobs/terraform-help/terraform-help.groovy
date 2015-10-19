job('terraform-help') {
  scm {
    git {
      remote {
	url('https://github.com/alphagov/tsuru-terraform.git')
      }
      branch('master')
      createTag(false)
    }
  }
  steps {
    shell('terraform --help || true')
  }
}
