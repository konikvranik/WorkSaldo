#!/bin/sh
cd $HOME
git clone -b gh-pages "https://$GITHUB_TOKEN@github.com/konikvranik/worksaldo.git" gh-pages || exit 1
echo Copying lint
cp -r $TRAVIS_BUILD_DIR/android/build/outputs/lint-results*
STAT=$?
echo Copying report
cp -r $TRAVIS_BUILD_DIR/android/build/outputs/report* gh-pages
[ $? > 0 -a $STAT > 0 ] && exit 1
cd $HOME/gh-pages || exit 1
git config user.email builds@travis-ci.org
git config user.name "Trsvis CI"
echo Adding lint
git add lint*
STAT=$?
echo Adding report
git add report*
[ $? > 0 -a $STAT > 0 ] && exit 1
echo Commiting
git commit -m "travis => build reports"
echo Pushing
git push
