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

# tree-sitter,tree-sitter-python,v0.21.0,python
# tree-sitter,tree-sitter-php,v0.22.8,php
# tree-sitter,tree-sitter-cpp,v0.22.3,cpp
# tree-sitter,tree-sitter-ruby,v0.21.0,ruby
# tree-sitter,tree-sitter-c,v0.21.4,c
# tree-sitter,tree-sitter-scala,v0.22.5,scala
# tree-sitter,tree-sitter-go,v0.21.2,go
# tree-sitter,tree-sitter-rust,v0.21.2,rust
# tree-sitter,tree-sitter-javascript,v0.21.4,javascript
# tree-sitter,tree-sitter-html,v0.20.4,html
# tree-sitter,tree-sitter-css,v0.21.1,css
# tree-sitter,tree-sitter-julia,v0.22.6,julia
# tree-sitter,tree-sitter-typescript,v0.21.2,typescript
# tree-sitter,tree-sitter-jsdoc,v0.21.0,jsdoc
# tree-sitter,tree-sitter-regex,v1.0.0,regex
# tree-sitter,tree-sitter-ocaml,v0.22.0,ocaml
# tree-sitter,tree-sitter-c-scharp,v0.21.3,csharp
# tree-sitter,tree-sitter-bash,v0.21.0,bash
# tree-sitter-grammars,tree-sitter-markdown,v0.2.3,markdown
# tree-sitter-grammars,tree-sitter-xml,v0.6.4,xml
# tree-sitter-grammars,tree-sitter-lua,v0.1.0,lua

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
    mkdir -p $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64
    ZIGFILES=$REPOPATH/src/parser.c
    if [ -f $REPOPATH/src/scanner.c ]; then
      ZIGFILES="$ZIGFILES $REPOPATH/src/scanner.c"
    fi
    zig cc -g0 -O2 -shared -target x86_64-linux-gnu -std=c11 -I $REPOPATH/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Linux/x86_64/libtree-sitter-$LANGUAGEID.so $ZIGFILES
    zig cc -g0 -O2 -shared -target x86_64-windows -std=c11 -I $REPOPATH/src -o $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/tree-sitter-$LANGUAGEID.dll $ZIGFILES
    rm $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/*.pdb
    rm $TARGETMODULEPATH/src/main/resources/org/springframework/shell/treesitter/Windows/x86_64/*.lib
done;

