#!/usr/bin/env bash
set -euo pipefail

[[ $# -ge 2 ]] || (echo "Usage: $0 N_MH N_MSS" && exit 1)
n_mh=$1
n_mss=$2

rm -rf logs
tmp=`mktemp`
echo "Storing temporary data in $tmp"
printf "["
for i in {1..100}
do
    java -Xmx1000m -cp "binaries/bin:binaries/jdom.jar" sinalgo.Run \
        -batch \
        -project ctmobile \
        -gen "$n_mh"  ctmobile:MobileHost           Random RandomDirection \
        -gen "$n_mss" ctmobile:MobileSupportStation Grid2D NoMobility \
        2>/dev/null 1>/dev/null
    printf '='
    log=`find logs -type f -name 'ctmobile.log'`
    grep -q CONSENSUS "$log" || (printf "\nDid not converge\n" && exit 1)
    awk '/CONSENSUS/ { print $NF }' "$log" >> "$tmp"
    rm -rf logs
done
printf '] '
awk '{ cnt+=$NF } END { print cnt/NR }' "$tmp"
