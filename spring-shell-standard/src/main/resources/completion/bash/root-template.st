_<name>_root_command()
{
    last_command="<name>"

    command_aliases=()

    commands=()
    <topcommands:{c | commands+=("<c.last>")}; separator="\n">

    flags=()
    two_word_flags=()
    local_nonpersistent_flags=()
    flags_with_completion=()
    flags_completion=()

    must_have_one_flag=()
    must_have_one_noun=()
    noun_aliases=()
}
