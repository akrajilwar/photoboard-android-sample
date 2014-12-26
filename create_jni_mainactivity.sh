#!/bin/sh

pushd .
cd app/src/main
javah -d jni -classpath /Users/allieus/tools/android-sdk/platforms/android-21/android.jar:/Users/allieus/tools/android-sdk/extras/android/support/v7/appcompat/libs/android-support-v7-appcompat.jar:/Users/allieus/tools/android-sdk/extras/android/support/v4/android-support-v4.jar:../../../app/build/intermediates/classes/debug  com.sunkist.photoboard.MainActivity
popd
tree app/src/main/jni
