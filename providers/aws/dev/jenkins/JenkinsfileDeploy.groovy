pipeline {
     agent {
        node {
            label 'aws-Jenkins-slave'
        }
    }
	
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    
    environment {
        ENV = 'dev'
        REGION = "us-east-1"
        projectName = 'EKS_poc'
        urlPrefix = "https://github.com/rupeshnemade"
        projectUrl = "${urlPrefix}/eks-deployment.git"
    }

    stages {

        stage('EKS Deployment') {
            stages {

                stage('Checkout') {
                    steps {
                        git(
                                url: projectUrl,
                                credentialsId: "cloudbees_ci",
                                branch: 'master'
                        )

                        updateGitlabCommitStatus(state: 'running')

                    }
                }
                stage('Terraform - Plan') {
                    steps {
                        ansiColor('xterm') {
                            retry(3) {
                                sh 'make terraform-plan'
                            }
                        }
                    }
                }
                stage('Terraform - Apply') {
                    steps {
                        ansiColor('xterm') {
                            retry(3) {
                                sh 'make terraform-apply'
                            }
                        }
                    }
                }
            }
            post {
                always {
                    cleanWs()
                    echo "Send notifications for result: ${currentBuild.result}"
                }
            }
        }

        stage('Grant Tiller access to create resources in kube-system namespace'){
       
            steps {
                /* --kubeconfig helps kubectl on which EKS cluster to execute the commands from Jenkins machine*/
                sh "kubectl create serviceaccount tiller --namespace=kube-system --kubeconfig $HOME/.kube/kubeconfig_eks_test"
                sh "kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller --kubeconfig $HOME/.kube/kubeconfig_eks_test"
                sh "sleep 10"
                sh "helm init"
                sh '''#!/bin/bash
                        kubectl patch deployment tiller-deploy --namespace=kube-system --patch='{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}' --kubeconfig $HOME/.kube/kubeconfig_eks_test
                '''

            }
        }

    }
    post {
        success {
            updateGitlabCommitStatus(name: env.JOB_NAME, state: 'success')
        }
        failure {
            updateGitlabCommitStatus(name: env.JOB_NAME, state: 'failed')
        }
        unstable {
            updateGitlabCommitStatus(name: env.JOB_NAME, state: 'failed')
        }
        always {
            echo "Send notifications for result: ${currentBuild.result}"
        }
    }
}