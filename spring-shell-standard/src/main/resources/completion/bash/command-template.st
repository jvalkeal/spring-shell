<subcommands:{c |

_<name>_<c.parts:{p | <p>}; separator="_">()
{
    last_command="<name>_<c.parts:{p | <p>}; separator="_">"

    command_aliases=()

    commands=()
    <c.mains:{cc | commands+=("<cc>")}; separator="\n">

    flags=()
    two_word_flags=()
    <c.twowordflags:{cc | two_word_flags+=("<cc>")}; separator="\n">
    local_nonpersistent_flags=()
    flags_with_completion=()
    flags_completion=()

    must_have_one_flag=()
    must_have_one_noun=()
    noun_aliases=()
\}

}; separator="\n">
