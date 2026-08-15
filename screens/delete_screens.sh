#!/usr/bin/env bash
# Deletes the images pulled down from LanDenLabs.com into this screens/ directory.
# NOT executed automatically -- run manually with: bash screens/delete_screens.sh
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

rm -fv \
  dev_stuff.png \
  text.jpg \
  text-detail1.jpg \
  screen-portrait.jpg \
  theme-menu.jpg \
  package-user.jpg \
  package-libs.jpg \
  package-cache.jpg \
  package-pref.jpg \
  package-pref2.jpg \
  package-pref3.jpg \
  iconattr1.jpg \
  iconattr-detail.jpg \
  icondraw1.jpg \
  icondraw-detail.jpg \
  miscattr.jpg
