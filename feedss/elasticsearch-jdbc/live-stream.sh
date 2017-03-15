#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
bin=${DIR}/../bin
lib=${DIR}/../lib

echo '
{
    "type" : "jdbc",
    "jdbc" : {
        "url" : "jdbc:mysql://mysql:3306/live",
        "user" : "live",
        "password" : "yksysjtsws",
	"sql" : [{"statement":"select *, \"live\" as _index, \"stream\" as _type, id as _id, latitude as \"location.lat\", longitude as \"location.lon\" from stream where updated > ?","parameter":["$metrics.lastexecutionstart"]}],
	"statefile" : "live.stream.statefile.json",
	"schedule" : "0 0-59 0-23 ? * *",
        "metrics": {
		"enabled" : true
	},
        "elasticsearch" : {
             "cluster" : "elasticsearch",
             "host" : "elasticsearch",
             "port" : 9300
        },
        "index" : "live",
        "type" : "stream",
        "index_settings" : {
            "index" : {
                "number_of_shards" : 1
            }
        },
        "type_mapping": {
            "stream" : {
                "properties" : {
                    "location" : {
                        "type" : "geo_point"
                    }
                }
            }
        }
    }
}
' | java \
    -cp "${lib}/*" \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.JDBCImporter
