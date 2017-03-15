#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
bin=${DIR}/../bin
lib=${DIR}/../lib

echo '
{
    "type" : "jdbc",
    "jdbc" : {
        "url" : "jdbc:mysql://127.0.0.1:3306/live",
        "statefile" : "live.profile.statefile.json",
        "schedule" : "0 0-59 0-23 ? * *",
        "user" : "root",
        "password" : "jklfds56",
        "sql" : [{
                "statement": "select *, id as _id, id from profile where updated > ? ",
                "parameter": ["$metrics.lastexecutionstart"]}
            ],
        "index" : "live",
        "type" : "profile",
        "metrics": {
            "enabled" : true
        },
        "elasticsearch" : {
             "cluster" : "elasticsearch",
             "host" : "127.0.0.1",
             "port" : 9300 
        }   
    }
}
' | java \
    -cp "${lib}/*" \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.JDBCImporter
