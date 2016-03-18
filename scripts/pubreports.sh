#!/bin/sh
cd $HOME
git clone -b gh-pages "https://$GITHUB_TOKEN@github.com/konikvranik/worksaldo.git" gh-pages || exit 1
echo Copying lint
cp -r $TRAVIS_BUILD_DIR/android/build/outputs/lint-results* gh-pages
STAT=$?
echo Copying report
cp -r $TRAVIS_BUILD_DIR/android/build/outputs/report* gh-pages
[ $? -gt 0 -a $STAT -gt 0 ] && exit 1
echo CD to gh-pages
cd $HOME/gh-pages || exit 1
echo config git
git config user.email builds@travis-ci.org
git config user.name "Trsvis CI"
echo Adding lint
git add lint*
STAT=$?
echo Adding report
git add report*
[ $? -gt 0 -a $STAT -gt 0 ] && exit 1
echo Commiting
git commit -m "travis => build reports"
echo Pushing
git push
