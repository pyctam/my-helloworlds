#!/bin/bash

if [ ! -f "$1" ]; then
    echo "$1 file not found!"
fi

#ffmpeg -i "$1" -vf deshake "x-$1"
#exit 1

echo "====: starting 1st pass"
#ffmpeg -i "$1" -vf vidstabdetect=stepsize=4:mincontrast=0:result=transforms.trf -f null -
#ffmpeg -i "$1" -vf vidstabdetect=stepsize=4:mincontrast=0:result=transforms.trf "stabilized-$1"

echo "====: starting 2nd pass"
ffmpeg -i "$1" -vf vidstabtransform=smoothing=30:interpol=bicubic:input=transforms.trf,unsharp,fade=t=in:st=0:d=4,fade=t=out:st=60:d=4 -tune film -preset veryslow -crf 8 -x264opts fast_pskip=0 -c:a pcm_s16le "final-$1"

echo "====: cleaning up after stabilization"
#rm -v transforms.trf
mv -v "final-$1" "$1.stabilized"
