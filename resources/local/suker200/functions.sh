#!/bin/sh
export PATH=$PATH:~/.local/bin:/tmp/bin/

set -e pipefail

getOpsType() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f1)
}

getCommitAction() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f2)
}

getProjectName() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f3)
}

getCommitMessage() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD))
}

getCommitID() {
	echo $(git rev-parse HEAD)
}

prepare() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)

	curl https://bootstrap.pypa.io/get-pip.py | python2.7 - --user
	~/.local/bin/pip2 install --user -r requirements.txt

	# curl -L https://github.com/kubernetes/kops/releases/download/1.9.0/kops-linux-amd64 -o /tmp/bin/kops && chmod +x /tmp/bin/kops

	
}

prepareTerraform() {
	curl -L https://releases.hashicorp.com/terraform/0.11.7/terraform_0.11.7_linux_amd64.zip -o /tmp/terraform.zip && \
		 cd /tmp/ && unzip terraform.zip && cp terraform bin/ && chmod +x bin/terraform
}

runKops() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)
	echo ${_ACTION}

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}
	# Load ENV file generate from kops_generator.py
	# CLUSTER_NAME=xxx
	# KOPS_VERSION=xxx
	. ./projects/${_PROJECT}/ENV

	

	if [ "${_ACTION}" = "init" ]
	then

		echo $KOPS_STATE_STORE

		echo $CLUSTER_NAME

		kops create -f projects/${_PROJECT}/kops/${KOPS_FILE:-kops_cluster.yaml} --state=${KOPS_STATE_STORE}

		kops create secret --name=${CLUSTER_NAME} sshpublickey admin -i projects/example/id_rsa.pub --state=${KOPS_STATE_STORE}

		kops update cluster --name=${CLUSTER_NAME} --yes --out=projects/${_PROJECT}/kops/ --target=terraform --state=${KOPS_STATE_STORE}

	elif [ "${_ACTION}" = "replace" ]
	then
		kops replace -f projects/${_PROJECT}/kops/${KOPS_FILE:-kops_cluster.yaml} --state=${KOPS_STATE_STORE}
		kops update cluster --name=${CLUSTER_NAME} --yes --out=projects/${_PROJECT}/kops/ --target=terraform  --state=${KOPS_STATE_STORE}
	else
		echo "${_ACTION} is not support action type"
		exit 1
	fi
}

runTerraform() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)
	echo ${_ACTION}

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}

	if [ "${_ACTION}" = "plan" ]
	then
		cd projects/${_PROJECT}/kops
		terraform plan
	elif [ "${_ACTION}" = "apply" ]
		cd projects/${_PROJECT}/kops
		terraform apply 
	else
		echo "${_ACTION} is not support action type"
		exit 1
	fi
}