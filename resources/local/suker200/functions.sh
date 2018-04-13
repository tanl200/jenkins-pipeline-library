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

prepareKops() {
	curl https://bootstrap.pypa.io/get-pip.py | python2.7 - --user
	~/.local/bin/pip2 install --user -r requirements.txt

	# curl -L https://github.com/kubernetes/kops/releases/download/1.9.0/kops-linux-amd64 -o /tmp/bin/kops && chmod +x /tmp/bin/kops
}

runKops() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)
	echo ${_ACTION}

	# Load ENV file generate from kops_generator.py
	# . ./projects/${_PROJECT}/ENV
	# CLUSTER_NAME=xxx
	# KOPS_VERSION=xxx

	if [ "${_ACTION}" = "init" ]
	then
		python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}

		. ./projects/${_PROJECT}/ENV

		kops create -f projects/${_PROJECT}/${KOPS_FILE:-kops_cluster.yaml}

		# kops update cluster --name=${CLUSTER_NAME} --yes --out=. --target=terraform 
	elif [ "${_ACTION}" = "replace" ]
	then
		kops replace -f projects/${_PROJECT}/${KOPS_FILE:-kops_cluster.yaml}
		kops update cluster --name=${CLUSTER_NAME} --yes --out=. --target=terraform
	else
		exit 1
	fi
}