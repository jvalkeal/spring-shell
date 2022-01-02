__start_<name>()
{
    local cur prev words cword split
    declare -A flaghash 2>/dev/null || :
    declare -A aliashash 2>/dev/null || :
    if declare -F _init_completion >/dev/null 2>&1; then
        _init_completion -s || return
    else
        __<name>_init_completion -n "=" || return
    fi
    local c=0
    local flag_parsing_disabled=
    local flags=()
    local two_word_flags=()
    local local_nonpersistent_flags=()
    local flags_with_completion=()
    local flags_completion=()
    local commands=("<name>")
    local command_aliases=()
    local must_have_one_flag=()
    local must_have_one_noun=()
    local has_completion_function=""
    local last_command=""
    local nouns=()
    local noun_aliases=()
    __<name>_handle_word
}

if [[ $(type -t compopt) = "builtin" ]]; then
    complete -o default -F __start_<name> <name>
else
    complete -o default -o nospace -F __start_<name> <name>
fi
