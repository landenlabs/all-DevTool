#!/bin/csh -f

#
#   Zip backup onto Dropbox, optionally provide comment
#
# Ex:
#   ./make_backup.csh 
#   ./make_backup.csh "the reason for backup, code changes, notes, etc"
# 

set dir=~/Dropbox/android/backups/_all
set fname=devTool
set file=`date "+${fname}_%Y-%m-%d-%H-%M"`
set zipFile=${file}.zip
set txtFile=${file}.txt

find . -name \.DS_Store -print -exec rm {} +
zip -r $dir/$zipFile . -x .git\* .idea\* captures\* .gradle\* gradle/\* \*build/\* \*backup/\* \*apk  

touch $dir/$txtFile
echo "$file $*" >> $dir/$txtFile

echo 
echo "--- Backups ---"
ls -alrt $dir/${fname}*

echo "--- Comments ---"
cat $dir/$txtFile
