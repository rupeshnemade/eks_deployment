terraform-plan:
	terragrunt plan-all --terragrunt-source-update  --terragrunt-non-interactive --terragrunt-working-dir providers/aws/$(ENV)/us_east_1_$(ENV)/

terraform-apply:
	terragrunt apply-all --terragrunt-source-update --terragrunt-non-interactive --terragrunt-working-dir providers/aws/$(ENV)/us_east_1_$(ENV)/