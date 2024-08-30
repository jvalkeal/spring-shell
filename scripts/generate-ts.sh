#!/bin/bash

find_basedir() {
  local basedir=$(cd -P -- "$(dirname -- "$0")" && cd .. && pwd -P)
  echo "${basedir}"
}

dojextract='false'
dozig='false'

print_usage() {
  echo "Usage: generate-ts.sh [-j] [-z]"
}

while getopts 'jz' flag; do
  case "${flag}" in
    j) dojextract='true' ;;
    z) dozig='true' ;;
    *) print_usage
       exit 1 ;;
  esac
done

TMPDIR=/tmp/spring-shell-ts
rm -fr $TMPDIR

GHOWNER=tree-sitter
GHREPO=tree-sitter
TAG=v0.23.0
REPOPATH=$TMPDIR/$GHOWNER/$GHREPO
TARGETMODULEPATH=spring-shell-treesitter

git clone --depth 1 -b $TAG https://github.com/$GHOWNER/$GHREPO.git $REPOPATH
mkdir -p $TARGETMODULEPATH/src/ts
cp $REPOPATH/lib/include/tree_sitter/api.h $TARGETMODULEPATH/src/ts/

if [ "$dojextract" == "true" ]; then
  jextract --header-class-name TreeSitter --output $TARGETMODULEPATH/src/main/java -t org.springframework.shell.treesitter.ts $TARGETMODULEPATH/src/ts/api.h
fi

if [ "$dozig" == "true" ]; then
  mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64
  mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64
  mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Mac/x86_64
  mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Mac/arm64
  ZIGFILES=$REPOPATH/lib/src/lib.c

  zig cc -g0 -O2 -shared -target x86_64-linux-gnu -std=c11 -I $REPOPATH/lib/include -I $REPOPATH/lib/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64/libtree-sitter.so $ZIGFILES
  zig cc -g0 -O2 -shared -target x86_64-windows -std=c11 -I $REPOPATH/lib/include -I $REPOPATH/lib/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/tree-sitter.dll $ZIGFILES
  rm $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/*.pdb
  rm $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/*.lib
  zig cc -g0 -O2 -shared -target x86_64-macos -std=c11 -I $REPOPATH/lib/include -I $REPOPATH/lib/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Mac/x86_64/libtree-sitter.jnilib $ZIGFILES
  zig cc -g0 -O2 -shared -target aarch64-macos -std=c11 -I $REPOPATH/lib/include -I $REPOPATH/lib/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Mac/arm64/libtree-sitter.jnilib $ZIGFILES
fi
