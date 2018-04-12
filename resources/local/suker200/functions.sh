#!/bin/sh

getCommitMessageAction() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f1)
}

getCommitMessage() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD))
}

getCommitID() {
	echo $(git rev-parse HEAD)
}
