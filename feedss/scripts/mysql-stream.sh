#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
bin=${DIR}/../bin
lib=${DIR}/../lib

echo '
{
    "type" : "jdbc",
    "jdbc" : {
        "url" : "jdbc:mysql://127.0.0.1:3306/contentcenter",
        "user" : "root",
        "password" : "jklfds56",
	"sql" : [{"statement":"select *, \"contentcenter\" as _index, \"stream\" as _type, id as _id, latitude as \"location.lat\", longitude as \"location.lon\" from stream where updated > ?","parameter":["$metrics.lastexecutionstart"]}],
	"statefile" : "contentcenter.stream.statefile.json",
	"schedule" : "0 0-59 0-23 ? * *",
        "metrics": {
		"enabled" : true
	},
        "elasticsearch" : {
             "cluster" : "elasticsearch",
             "host" : "127.0.0.1",
             "port" : 9300
        },
        "index" : "contentcenter",
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
' | /data/soft/jdk/bin/java \
    -cp "${lib}/*" \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.JDBCImporter
