#!/bin/sh

if [ $# -lt 2 ]
then
    echo "Usage <command> <path> [options]"
    exit 1
fi

class_for_command() # in $1 command name, out $2 class
{
    case $1 in
	Chgrp|chgrp)
	    class=Chgrp
	    ;;

	Chmod|chmod)
	    class=Chmod
	    ;;

	Chown|chown)
	    class=Chown
	    ;;

	Ls|ls)
	    class=Ls
	    ;;

	Lstag|lstag)
	    class=Lstag
	    ;;

	Mkdir|mkdir)
	    class=Mkdir
	    ;;

	Readtag|readtag)
	    class=Readtag
	    ;;

	Writetag|writetag)
	    class=Writetag
	    ;;

        Setfacl|setfacl)
            class=Setfacl
            ;;

        Getfacl|getfacl)
            class=Getfacl
            ;;

	Writedata|writedata)
	    class=Writedata
	    ;;

        Checksum|checksum)
            class=Checksum
            ;;

	*)
	    echo "Unknown command $1.  Available commands are:"
            echo "    chgrp chmod chown ls lstag mkdir readtag writetag writedata"
            echo "    setfacl getfacl checksum"
            exit 1
	    ;;
    esac

    cmd=$2=org.dcache.chimera.cli.$class
    eval $cmd
}

class_for_command "$1" class
shift

@DCACHE_LOAD_CONFIG@

dbpass=$(getProperty chimera.db.password)

CLASSPATH="$(getProperty dcache.paths.classpath)" \
    ${JAVA} $(getProperty dcache.java.options) \
    -Dlog=${DCACHE_LOG:-warn} \
    ${class}  $(getProperty chimera.db.driver) \
    $(getProperty chimera.db.url) \
    $(getProperty chimera.db.dialect) \
    $(getProperty chimera.db.user) \
    ${dbpass:-""} \
    "$@"
