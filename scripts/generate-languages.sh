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
    git clone --depth 1 -b $TAG https://github.com/$GHOWNER/$GHREPO.git $TMPDIR/$GHOWNER/$GHREPO
    npx hygen init tslanguage --language $LANGUAGEID
    mkdir -p spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/ts
    cp $TMPDIR/$GHOWNER/$GHREPO/bindings/c/tree-sitter-$LANGUAGEID.h spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/ts/
    mkdir -p spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/main/resources/org/springframework/shell/treesitter/queries/$LANGUAGEID
    cp $TMPDIR/$GHOWNER/$GHREPO/queries/highlights.scm spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/main/resources/org/springframework/shell/treesitter/queries/$LANGUAGEID/
    # spring-shell-treesitter-language/spring-shell-treesitter-language-json/src/main/resources/org/springframework/shell/treesitter/queries/json/highlights.scm
    jextract --header-class-name TreeSitter$LANGUAGENAME --output spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/main/java -t org.springframework.shell.treesitter.language.$LANGUAGEID.ts spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/ts/tree-sitter-$LANGUAGEID.h
    mkdir -p spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64
    ZIGFILES=$TMPDIR/$GHOWNER/$GHREPO/src/parser.c
    if [ -f $TMPDIR/$GHOWNER/$GHREPO/src/scanner.c ]; then
      ZIGFILES="$ZIGFILES $TMPDIR/$GHOWNER/$GHREPO/src/scanner.c"
    fi
    zig cc -g0 -O2 -shared -target x86_64-linux-gnu -std=c11 -I $TMPDIR/$GHOWNER/$GHREPO/src -o spring-shell-treesitter-language/spring-shell-treesitter-language-$LANGUAGEID/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64/libtree-sitter-$LANGUAGEID.so $ZIGFILES
done;

