terragrunt = {
  terraform {
    source = "https://github.com/rupeshnemade/eks_terraform.git"
  }

  # Include all settings from the root terraform.tfvars dev
  include = {
    path = "${find_in_parent_folders()}"
  }
}
