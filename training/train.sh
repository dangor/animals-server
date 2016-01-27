#!/usr/bin/env bash

filename="${1-animalia_data.csv}"
host="${2-localhost}"
port="${3-8080}"
rel=(concept has eats lives isa)

while read -r line
do
    # split on commas
    IFS=',' read -ra arr <<< "$line"
    if [[ ${arr[0]} == "concept" ]]; then
        # store relationships
        rel=$arr
    else
        subject=${arr[0]}
        arrLen=${#arr[@]}
        for (( i=1; i<${arrLen}; i++ ));
        do
            # split on colons
            objString=${arr[$i]}
            IFS=':' read -ra objArr <<< "$objString"
            relString=${rel[$i]}
            for X in "${objArr[@]}";
            do
                Y=$(echo $X | tr -d '\r')
                json="{ \"subject\": \"$subject\", \"rel\": \"$relString\", \"object\": \"$Y\" }"
                echo $json
                response=$(curl -H "Content-Type: application/json" -X POST -d "$json" $host:$port/animals/facts 2> /dev/null)
                echo $response
                if [[ "${response}" != *"id"* ]]; then
                    echo "Failed curl with $json and got $response"
                    exit 1
                fi
            done
        done
    fi
done < "$filename"

echo "Done training. Here are some sample query results..."

echo
echo "How many animals have fins?"
curl -X GET "$host:$port/animals/which?s=animal&r=has&o=fin"

echo
echo "Which animals eat berries?"
curl -X GET "$host:$port/animals/which?s=animal&r=eats&o=berries"

echo
echo "Which animals eat mammals?"
curl -X GET "$host:$port/animals/which?s=animal&r=eats&o=mammal"

echo
echo "Which bears have scales?"
curl -X GET "$host:$port/animals/which?s=bear&r=has&o=scale"

echo
echo "How many mammals live in the ocean?"
curl -X GET "$host:$port/animals/which?s=mammal&r=lives&o=ocean"

echo
exit 0;
