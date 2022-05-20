#!/usr/bin/env bash
set -e
[[ $# -ge 1 ]] || (echo "Usage: $0 N" && exit 1)
nnodes=$1
nrounds=1000

for psc in 0.2 0.5 0.8
do
    printf "psc=$psc ["
    rm -rf logs
    for i in {1..100}
    do
        java -Xmx1000m -cp "binaries/bin:binaries/jdom.jar" sinalgo.Run \
            -batch \
            -project sanders \
            -gen "$nnodes" sanders:SandersNode Grid2D sanders:FullGraph \
            -overwrite "Sanders/prequest=$psc" \
            -rounds "$nrounds" 2>/dev/null 1>/dev/null
        printf '='
    done
    printf '] '
    find logs -type f -name 'sanders.log' | \
        xargs -rn1 python3 processLog.py --log | \
        awk -v "N=$nnodes" '{ cnt+=$NF } END { print cnt/(NR*N) }'
done
