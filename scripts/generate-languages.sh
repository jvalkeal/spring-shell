#!/bin/bash

find_basedir() {
  local basedir=$(cd -P -- "$(dirname -- "$0")" && cd .. && pwd -P)
  echo "${basedir}"
}

TMPDIR=/tmp/ddd/tmlanguages


rm -fr $TMPDIR


VALUES='
tree-sitter,tree-sitter-java,v0.21.0,java
tree-sitter,tree-sitter-json,v0.21.0,json
tree-sitter-grammars,tree-sitter-yaml,v0.6.1,yaml
'

export PROJECTBASEDIR=$(find_basedir)

for VALUE in $VALUES;
  do
    GHOWNER=$(echo $VALUE | cut -f1 -d,)
    GHREPO=$(echo $VALUE | cut -f2 -d,)
    TAG=$(echo $VALUE | cut -f3 -d,)
    LANGUAGEID=$(echo $VALUE | cut -f4 -d,)
    LANGUAGENAME="${LANGUAGEID^}"
    REPOPATH=$TMPDIR/$GHOWNER/$GHREPO
    TARGETMODULEPATH=spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID
    git clone --depth 1 -b $TAG https://github.com/$GHOWNER/$GHREPO.git $REPOPATH
    npx hygen init tslanguage --language $LANGUAGEID
    mkdir -p $TARGETMODULEPATH/src/ts
    cp $REPOPATH/bindings/c/tree-sitter-$LANGUAGEID.h $TARGETMODULEPATH/src/ts/
    mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/queries/$LANGUAGEID
    cp $REPOPATH/queries/highlights.scm $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/queries/$LANGUAGEID/
    jextract --header-class-name TreeSitter$LANGUAGENAME --output $TARGETMODULEPATH/src/main/java -t org.springframework.shell.treesitter.language.$LANGUAGEID.ts $TARGETMODULEPATH/src/ts/tree-sitter-$LANGUAGEID.h
    mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64
    ZIGFILES=$REPOPATH/src/parser.c
    if [ -f $REPOPATH/src/scanner.c ]; then
      ZIGFILES="$ZIGFILES $REPOPATH/src/scanner.c"
    fi
    zig cc -g0 -O2 -shared -target x86_64-linux-gnu -std=c11 -I $REPOPATH/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64/libtree-sitter-$LANGUAGEID.so $ZIGFILES
done;

