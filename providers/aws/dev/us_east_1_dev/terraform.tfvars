terragrunt = {

  # iam_role = "arn:aws:iam::804208728578:role/CIPolicy"
  # Configure Terragrunt to automatically store tfstate files in an S3 bucket
  remote_state {
    backend = "s3"

    config {
      encrypt        = true
      bucket         = "eks-tf-us-east-1-dev"
      key            = "${path_relative_to_include()}/terraform.tfstate"
      region         = "us-east-1"
      dynamodb_table = "eks-tfstate"
    }

    s3_bucket_tags = {
      Owner = "rupesh"
      Project = "eks_poc"
      Terraform = true
    }

    dynamotable_tags = {
      Owner = "rupesh"
      Project = "eks_poc"
      Terraform = true
    }
  }

  # Configure root level variables that all resources can inherit
  terraform {
    extra_arguments "bucket" {
      commands = [
        "${get_terraform_commands_that_need_vars()}",
      ]

      optional_var_files = [
        "${get_tfvars_dir()}/${find_in_parent_folders("account.tfvars", "ignore")}",
      ]
    }
  }
}

