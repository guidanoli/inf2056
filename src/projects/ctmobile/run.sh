#!/usr/bin/env bash
set -euo pipefail

[[ $# -ge 2 ]] || (echo "Usage: $0 N_MH N_MSS [WT]" && exit 1)
n_mh=$1
n_mss=$2
wt=${3:-20}
max_rounds=2000

rm -rf logs
tmp=`mktemp`
echo "Storing temporary data in $tmp"
printf "["
for i in {1..100}
do
    java -Xmx1000m -cp "binaries/bin:binaries/jdom.jar" sinalgo.Run \
        -batch \
        -project ctmobile \
        -gen "$n_mh"  ctmobile:MobileHost           Random RandomWayPoint \
        -gen "$n_mss" ctmobile:MobileSupportStation Grid2D NoMobility \
        -overwrite "RandomWayPoint/WaitingTime/lambda=$wt" \
        -rounds "$max_rounds" \
        2>/dev/null 1>/dev/null
    printf '='
    log=`find logs -type f -name 'ctmobile.log' | sort -r | tail -n1`
    if ! [[ -z "$log" ]]
    then
        grep CONSENSUS "$log" >> "$tmp" || true
    fi
    rm -rf logs
done
printf '] '
awk '/CONSENSUS/ { cnt+=$NF } END { print cnt/NR }' "$tmp"
printf 'N = '
cat "$tmp" | wc -l
rm "$tmp"
