#!/bin/sh

TRAJETS="00351
00354
00360
00455
01303
02301
02403
02501
04401
07301
10301
14302
14304
18401
22302
24501
27301
27303
28401
29301
29307
31301
34301
36301
36501
39402
40401
41301
42301
44301
44302
44303
45301
45302
47301
50401
51301
52401
53302
54402
54403
54404
55401
55402
56302
57301
57302
57401
57501
59302
59305
59402
59502
60304
60306
60308
60401
61301
61302
61401
62302
62304
63302
65302
67306
67307
67501
68302
71301
71302
71401
71402
71501
72301
72303
76301
77301
77303
77306
77404
77405
77501
79302
80301
80302
81302
82302
83301
83305
83307
84303
84402
85304
85401
88304
88402
89401
91401
95401
2a501"

for UU in $TRAJETS ; do
    mv uu_$UU.zip ../pret_a_envoi
done

echo "UU deplac�es..."
